package net.jmb19905.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface FireView {
    void carbonize$registerFlammableBlock(Block block, int burnChance, int spreadChance);
    boolean carbonize$isFlammable(BlockState state);
    int carbonize$getSpreadChance(BlockState state);
    int carbonize$getBurnChance(BlockState state);
    String carbonize$getSerialId();

    void carbonize$appendProperties(StateManager.Builder<Block, BlockState> builder);
    BlockState carbonize$getStateForPosition(BlockView world, BlockPos pos);

}
