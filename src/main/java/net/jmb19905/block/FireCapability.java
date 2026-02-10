package net.jmb19905.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

/**
 * Intended for fire that can spread.
 */
public interface FireCapability extends FireView {
    void registerFlammable(Block block, int burnChance, int spreadChance);
    BlockState findAppropriateState(BlockView view, BlockPos pos);
}
