package net.jmb19905.block.ember;

import net.jmb19905.api.FireType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;


@SuppressWarnings("deprecation")
public class EmberFenceBlock extends FenceBlock implements AbstractEmberBlock {
    private final FireType fireType;

    public EmberFenceBlock(Settings settings, FireType fireType) {
        super(settings);
        this.fireType = fireType;
        AbstractEmberBlock.super.addDefaultStates();
    }

    @Override
    public FireType getFireType() {
        return fireType;
    }

    @Override
    public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        AbstractEmberBlock.super.appendProperties(builder);
        super.appendProperties(builder);
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        AbstractEmberBlock.super.onSteppedOn(world, pos, state, entity);
        super.onSteppedOn(world, pos, state, entity);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        AbstractEmberBlock.super.onBlockAdded(state, world, pos, oldState, notify);
        super.onBlockAdded(state, world, pos, oldState, notify);
    }

    @Override
    public void scheduledTick(BlockState thisState, ServerWorld world, BlockPos thisPos, Random random) {
        AbstractEmberBlock.super.scheduledTick(thisState, world, thisPos, random);
        super.scheduledTick(thisState, world, thisPos, random);
    }
    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        AbstractEmberBlock.super.randomDisplayTick(state, world, pos, random);
        super.randomDisplayTick(state, world, pos, random);
    }
}
