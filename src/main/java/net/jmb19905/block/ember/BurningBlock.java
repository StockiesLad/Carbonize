package net.jmb19905.block.ember;

import net.jmb19905.api.FireType;
import net.jmb19905.core.CarbonizeCommon;
import net.jmb19905.mixin.IBlock;
import net.jmb19905.recipe.BurnRecipe;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import static net.jmb19905.block.ember.BurningBlock.Stage.*;

/**
 * TODO: make spread based on firetype and block flammability settings (automatic)
 * TODO: make small chance to turn into charring wood.
 */
public interface BurningBlock {
    EnumProperty<Stage> STAGE = EnumProperty.of("stage", Stage.class);
    BooleanProperty DORMANT = BooleanProperty.of("dormant");

    FireType getFireType();

    default Block block() {
        return (Block) this;
    }

    default IBlock iBlock() {
        return (IBlock) this;
    }

    default void addDefaultStates() {
        iBlock().carbonize$setDefaultState(block().getDefaultState().with(DORMANT, false).with(STAGE, SMOLDERING));
    }

    default void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(STAGE, DORMANT);
    }

    default void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleBlockTick(pos, block(), 1);
    }

    default void scheduledTick(BlockState thisState, ServerWorld world, BlockPos thisPos, Random random) {
        world.scheduleBlockTick(thisPos, block(), 5);
        var directions = Direction.values();
        var direction = directions[random.nextInt(directions.length)];
        var sidePos = thisPos.offset(direction);
        var sideState = world.getBlockState(sidePos);

        if (random.nextBoolean()) return;

        if (tryIgnite(sideState, thisState, thisPos, sidePos, world, random)) return;

        tryProceedWithStage(thisState, world, thisPos, random);
    }

    default boolean tryIgnite(BlockState sideState, BlockState thisState, BlockPos thisPos, BlockPos sidePos, ServerWorld world, Random random) {
        if (sideState.isAir() && random.nextBoolean()) {
            getFireType().ifCapability(capability ->
                    world.setBlockState(sidePos, capability.findAppropriateState(world, sidePos)));
            return true;
        }

        if (!sideState.isOf(block())) return false;

        if (thisState.get(STAGE) != SMOLDERING && sideState.get(STAGE) == SMOLDERING) {
            world.setBlockState(sidePos, sideState.with(STAGE, BURNING));
            return true;
        }

        for (BurnRecipe burnRecipe : world.getServer().getRecipeManager().listAllOfType(CarbonizeCommon.BURN_RECIPE_TYPE)) {
            if (burnRecipe.isInput(thisState, getFireType()))
                world.setBlockState(thisPos, burnRecipe.burnBlock().getDefaultState().with(STAGE, BURNING));
        }

        return false;
    }

    default void tryProceedWithStage(BlockState thisState, ServerWorld world, BlockPos thisPos, Random random) {
        var thisStage = thisState.get(STAGE);

        if (thisStage == SMOLDERING)
            if (random.nextInt(4) == 0)
                world.setBlockState(thisPos, thisState.with(STAGE, BURNING));

        if (thisStage == BURNING || thisStage == CHARRING) {
            var exposedSurfaces = 0;
            for (Direction value : Direction.values()) {
                var sidePos = thisPos.offset(value);
                var sideState = world.getBlockState(sidePos);
                if (sideState.isAir())
                    exposedSurfaces++;
            }

            world.setBlockState(thisPos, thisState.with(STAGE, exposedSurfaces < 3 ? CHARRING : SOOTING));
        }

        if (random.nextInt(100) == 0) {
            //TODO: fix block states
            for (BurnRecipe burnRecipe : world.getServer().getRecipeManager().listAllOfType(CarbonizeCommon.BURN_RECIPE_TYPE)) {
                if (burnRecipe.burnBlock().equals(block()))
                    world.setBlockState(thisPos, (thisStage == SOOTING ? burnRecipe.failResult() : burnRecipe.successResult()).getDefaultState());
            }
        }
    }

    default void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(state.get(STAGE).equals(BURNING) ? 48 : 72) == 0)
            world.playSound((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 0.25F + random.nextFloat() / 2, random.nextFloat() * 0.7f + 0.3f, false);
        if (state.get(STAGE) != BURNING && random.nextBoolean()) return;

        double x = (double) pos.getX() + random.nextDouble();
        double z = (double) pos.getZ() + random.nextDouble();
        world.addParticle(getFireType().asFlameParticle(), x, pos.getY() + random.nextDouble(), z, - 0.01 + random.nextFloat() / 50, random.nextFloat() / 50, - 0.01 + random.nextFloat() / 50);

        if (world.getBlockState(pos.up()).isAir()) return;

        x = (double)pos.getX() + random.nextDouble();
        z = (double)pos.getZ() + random.nextDouble();
        world.addParticle(getFireType().asFlameParticle(), x, pos.getY() + random.nextDouble(), z, - 0.01 + random.nextFloat() / 50, random.nextFloat() / 50, - 0.01 + random.nextFloat() / 50);
    }

    record Tag2Block(TagKey<Block> tag, Block block) {}

    enum Stage implements StringIdentifiable {
        SMOLDERING,
        BURNING,
        CHARRING,
        SOOTING;

        @Override
        public String asString() {
            return name().toLowerCase();
        }
    }
}
