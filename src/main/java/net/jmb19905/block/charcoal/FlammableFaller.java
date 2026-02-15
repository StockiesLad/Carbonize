package net.jmb19905.block.charcoal;

import net.jmb19905.mixin.IBlockSetType;
import net.jmb19905.mixin.IWoodType;
import net.minecraft.block.*;
import net.minecraft.client.util.ParticleUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

@SuppressWarnings("deprecation")
public interface FlammableFaller extends LandingBlock {
    BlockSetType BURNT_SET_TYPE = IBlockSetType.register(new BlockSetType("burnt"));
    BlockSetType EMBER_SET_TYPE = IBlockSetType.register(new BlockSetType("ember"));
    WoodType BURNT_WOOD_TYPE = IWoodType.register(new WoodType("burnt", BURNT_SET_TYPE));
    WoodType EMBER_WOOD_TYPE = IWoodType.register(new WoodType("ember", EMBER_SET_TYPE));

    Block block();

    default void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleBlockTick(pos, block(), this.getFallDelay());
    }

    default void getStateForNeighborUpdate(BlockState state, WorldAccess world, BlockPos pos) {
        world.scheduleBlockTick(pos, block(), this.getFallDelay());
    }

    default void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!FallingBlock.canFallThrough(world.getBlockState(pos.down())) || pos.getY() < world.getBottomY()) {
            return;
        }

        boolean burning = false;
        for (Direction dir : Direction.values()) {
            BlockState fireState = world.getBlockState(pos.offset(dir));
            if (fireState.isIn(BlockTags.FIRE)) {
                if (dir == Direction.UP || (fireState.contains(ConnectingBlock.FACING_PROPERTIES.get(dir.getOpposite())) && fireState.get(ConnectingBlock.FACING_PROPERTIES.get(dir.getOpposite())))) {
                    burning = true;
                    break;
                }
            }
        }

        FallingBlockEntity fallingBlockEntity = FallingBlockEntity.spawnFromBlock(world, pos, state);
        fallingBlockEntity.setOnFire(burning);
        this.configureFallingBlockEntity(fallingBlockEntity);
    }

    default void configureFallingBlockEntity(FallingBlockEntity entity) {
        entity.setHurtEntities(1.0f, 10);
    }

    default int getFallDelay() {
        return 2;
    }

    static boolean canFallThrough(BlockState state) {
        return state.isAir() || state.isIn(BlockTags.FIRE) || state.isLiquid() || state.isReplaceable();
    }

    default void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (random.nextInt(16) == 0) {
            BlockPos blockPos = pos.down();
            if (canFallThrough(world.getBlockState(blockPos))) {
                ParticleUtil.spawnParticle(world, pos, random, new BlockStateParticleEffect(ParticleTypes.FALLING_DUST, state));
            }
        }

    }

    default int getColor(BlockState state, BlockView world, BlockPos pos) {
        return -16777216;
    }

    @Override
    default void onLanding(World world, BlockPos pos, BlockState fallingBlockState, BlockState currentStateInPos, FallingBlockEntity fallingBlockEntity) {
        if (fallingBlockEntity.isOnFire()) {
            world.setBlockState(pos.up(), Blocks.FIRE.getDefaultState());
        }
    }

    @Override
    default void onDestroyedOnLanding(World world, BlockPos pos, FallingBlockEntity fallingBlockEntity) {
        if (fallingBlockEntity.isOnFire()) {
            world.setBlockState(pos.up(), Blocks.FIRE.getDefaultState());
        }
    }

    @Override
    default DamageSource getDamageSource(Entity attacker) {
        if (attacker instanceof FallingBlockEntity entity && entity.isOnFire()) {
            return attacker.getDamageSources().onFire();
        }
        return LandingBlock.super.getDamageSource(attacker);
    }
}
