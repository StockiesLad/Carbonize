package net.jmb19905.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface FireView {
    boolean carbonize$isFlammable(BlockState state);
    int carbonize$getSpreadChance(BlockState state);
    void carbonize$registerFlammableBlock(Block block, int burnChance, int spreadChance);

    void carbonize$appendProperties(StateManager.Builder<Block, BlockState> builder);
    BlockState carbonize$getStateForPosition(BlockView world, BlockPos pos);

}
