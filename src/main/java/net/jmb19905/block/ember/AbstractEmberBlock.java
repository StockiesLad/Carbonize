package net.jmb19905.block.ember;

import net.jmb19905.api.FireType;
import net.jmb19905.core.CarbonizeCommon;
import net.jmb19905.mixin.IBlock;
import net.jmb19905.recipe.BurnRecipe;
import net.jmb19905.util.BlockHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
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

import java.util.function.Supplier;

import static net.jmb19905.block.ember.AbstractEmberBlock.Stage.*;
import static net.minecraft.state.property.Properties.AGE_15;

/**
 * TODO: make spread based on firetype and block flammability settings (automatic)
 * TODO: make small chance to turn into charring wood.
 * TODO: do smoldering, charring and sooting textures
 * TODO: add partial flammability for soul & charcoal.
 * TODO: add hot charcoal & soot just after burning (make it release smoke)
 * TODO: smoldering foliage
 */
public interface AbstractEmberBlock {
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
        iBlock().carbonize$setDefaultState(block().getDefaultState()
                .with(DORMANT, false)
                .with(STAGE, SMOLDERING)
                .with(AGE_15, 0)
        );
    }

    default void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(STAGE, DORMANT, AGE_15);
    }

    default void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleBlockTick(pos, block(), (int) getFireType().getTickSpeedFactor() * (10 + world.random.nextInt(10)));
    }

    default void scheduledTick(BlockState thisState, ServerWorld world, BlockPos thisPos, Random random) {
        world.scheduleBlockTick(thisPos, block(), (int) getFireType().getTickSpeedFactor() * (10 + random.nextInt(10)));

        if (thisState.get(DORMANT)) return;

        for (var direction : Direction.shuffle(random)) {
            var sidePos = thisPos.offset(direction);
            var sideState = world.getBlockState(sidePos);
            if (tryIgnite(sideState, thisState, thisPos, sidePos, world, random)) return;
        }

        tryProceedWithStage(thisState, world, thisPos, random);
    }

    default boolean tryIgnite(BlockState sideState, BlockState thisState, BlockPos thisPos, BlockPos sidePos, ServerWorld world, Random random) {
        if (random.nextInt(100) < getFireType().getEmissivity()) return false;

        if (sideState.isAir()) {
            var age = thisState.get(AGE_15);
            var temp = getFireType().getDeltaTemperature();
            temp =  temp + (int)getFireType().getEmissivity() * age;

            if (thisState.get(STAGE) == BURNING && random.nextInt(temp * 150 / Math.max(1, age)) < getFireType().getReflectivity() * age/150) {
                getFireType().ifCapability(capability ->
                        world.setBlockState(sidePos, capability.findAppropriateState(world, sidePos)));
                return true;
            }
        }

        if (sideState.getBlock() instanceof TntBlock) {
            TntBlock.primeTnt(world, sidePos);
            return true;
        }

        if (sideState.getBlock() instanceof AbstractEmberBlock emberBlock) {
            emberBlock.tryProceedWithStage(sideState, world, sidePos, random);
            return false;
        }

        if (thisState.get(STAGE) == BURNING) {
            for (BurnRecipe burnRecipe : world.getServer().getRecipeManager().listAllOfType(CarbonizeCommon.BURN_RECIPE_TYPE)) {
                if (burnRecipe.isInput(sideState, getFireType())) {
                    world.setBlockState(sidePos, BlockHelper.transferState(burnRecipe.burnBlock().getDefaultState(), sideState));
                    return true;
                }
            }
        }

        return false;
    }

    default void tryProceedWithStage(BlockState thisState, ServerWorld world, BlockPos thisPos, Random random) {
        var stage = thisState.get(STAGE);
        Supplier<Stage> appropriateStage = () -> getAppropriateStageForAirExposure(world, thisPos);

        if (random.nextInt(getFireType().getDeltaTemperature()) < getFireType().getReflectivity() / 3) {
            if (tryNextPhase(stage, SMOLDERING, () -> BURNING, thisState, thisPos, world)) return;
            if (tryNextPhase(stage, BURNING, appropriateStage, thisState, thisPos, world)) return;

            if (thisState.get(AGE_15) < 15) {
                world.setBlockState(thisPos, thisState.with(AGE_15, thisState.get(AGE_15) + 1));
                return;
            }
        }

        if (stage == CHARRING && appropriateStage.get() == SOOTING) {
            world.setBlockState(thisPos, thisState
                    .with(STAGE, SOOTING)
                    .with(AGE_15, Math.max(thisState.get(AGE_15) - 3, 0))
            );

            return;
        }

        if ((stage == SOOTING || stage == CHARRING) && thisState.get(AGE_15) >= 15) {
            for (BurnRecipe burnRecipe : world.getServer().getRecipeManager().listAllOfType(CarbonizeCommon.BURN_RECIPE_TYPE)) {
                if (burnRecipe.burnBlock().equals(block())) {
                    var newState = BlockHelper.transferState((stage == SOOTING ? burnRecipe.failResult() :
                                    burnRecipe.successResult()).getDefaultState(), thisState);

                    if (stage == CHARRING)
                        if (random.nextInt(3) == 0) world.setBlockState(thisPos, newState);
                        else world.breakBlock(thisPos, false);
                    else world.setBlockState(thisPos, newState);

                    for (Direction direction : Direction.values()) {
                        var sidePos = thisPos.offset(direction);
                        var sideState = world.getBlockState(sidePos);

                        if (sideState.getBlock() == getFireType().asFireBlock())
                            world.removeBlock(sidePos, false);
                    }

                    return;
                }
            }
        }
    }

    default Stage getAppropriateStageForAirExposure(ServerWorld world, BlockPos thisPos) {
        var exposedSurfaces = 0;
        for (Direction value : Direction.values()) {
            var sidePos = thisPos.offset(value);
            var sideState = world.getBlockState(sidePos);
            if (!sideState.isFullCube(world, sidePos))
                exposedSurfaces++;
        }

        return exposedSurfaces == 0 ? CHARRING : SOOTING;
    }

    // Takes in a supplier to avoid unnecessary checking.
    default boolean tryNextPhase(Stage stage, Stage current, Supplier<Stage> next, BlockState thisState, BlockPos thisPos, World world) {
        if (stage == current) {
            if (thisState.get(AGE_15) >= 15)
                world.setBlockState(thisPos, thisState.with(STAGE, next.get()).with(AGE_15, 0));
            else world.setBlockState(thisPos, thisState.with(AGE_15, thisState.get(AGE_15) + 1));
            return true;
        } else return false;
    }

    default void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        entity.damage(world.getDamageSources().inFire(),
                 Math.abs(getFireType().getMaxTemperature()) / 400F);
    }

    default void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(state.get(STAGE).equals(BURNING) ? 48 : 72) == 0)
            world.playSound((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 0.25F + random.nextFloat() / 2, random.nextFloat() * 0.7f + 0.3f, false);

        double x = (double) pos.getX() + random.nextDouble();
        double y = (double) pos.getY() + random.nextDouble();
        double z = (double) pos.getZ() + random.nextDouble();

        if (state.get(STAGE) == SMOLDERING) {
            if (random.nextFloat() > 0.95f) {
                world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y + 1, z, 0.0, 0.07, 0.0);
            } else {
                world.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0.0, 0.0, 0.0);
            }
        } else {
            world.addParticle(getFireType().asFlameParticle(), x, y, z, - 0.01 + random.nextFloat() / 50, random.nextFloat() / 50, - 0.01 + random.nextFloat() / 50);

            if (world.getBlockState(pos.up()).isAir()) return;

            x = (double)pos.getX() + random.nextDouble();
            z = (double)pos.getZ() + random.nextDouble();
            world.addParticle(getFireType().asFlameParticle(), x, y, z, - 0.01 + random.nextFloat() / 50, random.nextFloat() / 50, - 0.01 + random.nextFloat() / 50);
        }
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
