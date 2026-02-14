package net.jmb19905.block.ember;

import net.jmb19905.api.FireType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.function.Supplier;


@SuppressWarnings("deprecation")
public class EmberPillarBlock extends PillarBlock implements BurningBlock {
    private final Supplier<FireType> fireType;

    public EmberPillarBlock(Settings settings, Supplier<FireType> fireType) {
        super(settings);
        this.fireType = fireType;
        BurningBlock.super.addDefaultStates();
    }

    @Override
    public FireType getFireType() {
        return fireType.get();
    }

    @Override
    public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        BurningBlock.super.appendProperties(builder);
        super.appendProperties(builder);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        BurningBlock.super.onBlockAdded(state, world, pos, oldState, notify);
        super.onBlockAdded(state, world, pos, oldState, notify);
    }

    @Override
    public void scheduledTick(BlockState thisState, ServerWorld world, BlockPos thisPos, Random random) {
        BurningBlock.super.scheduledTick(thisState, world, thisPos, random);
        super.scheduledTick(thisState, world, thisPos, random);
    }
    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        BurningBlock.super.randomDisplayTick(state, world, pos, random);
        super.randomDisplayTick(state, world, pos, random);
    }
}
