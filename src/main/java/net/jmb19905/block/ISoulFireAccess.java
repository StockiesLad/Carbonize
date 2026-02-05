package net.jmb19905.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface ISoulFireAccess {
    void carbonize$registerFlammableBlock(Block block, int burnChance, int spreadChance);
    boolean carbonize$isFlammable(BlockState state);
    BlockState carbonize$getStateForPosition(BlockView world, BlockPos pos);


}
