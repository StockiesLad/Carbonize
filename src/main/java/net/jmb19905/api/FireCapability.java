package net.jmb19905.api;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.function.Consumer;

/**
 * Intended for fire that can spread. Use this instead of {@link net.fabricmc.fabric.api.registry.FlammableBlockRegistry
 * FlammableBlockRegistry} via {@link FireType#ifCapability(Consumer)}
 */
public interface FireCapability extends FireView {
    BlockState findAppropriateState(BlockView view, BlockPos pos);

    void registerFlammable(Block block, int burnChance, int spreadChance);
    void registerFlammable(TagKey<Block> tag, int burnChance, int spreadChance);
}
