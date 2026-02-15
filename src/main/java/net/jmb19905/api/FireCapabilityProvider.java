package net.jmb19905.api;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface FireCapabilityProvider extends FireCapability {
    FireCapability getCapability();

    @Override
    default FireType asFireType() {
        return getCapability().asFireType();
    }

    @Override
    default boolean isBlockFlammable(BlockState state) {
        return getCapability().isBlockFlammable(state);
    }

    @Override
    default int getBlockSpreadChance(BlockState state) {
        return getCapability().getBlockSpreadChance(state);
    }

    @Override
    default int getBlockBurnChance(BlockState state) {
        return getCapability().getBlockBurnChance(state);
    }

    @Override
    default BlockState findAppropriateState(BlockView view, BlockPos pos) {
        return getCapability().findAppropriateState(view, pos);
    }


    @Override
    default void registerFlammable(Block block, int burnChance, int spreadChance) {
        getCapability().registerFlammable(block, burnChance, spreadChance);
    }

    @Override
    default void registerFlammable(TagKey<Block> tag, int burnChance, int spreadChance) {
        getCapability().registerFlammable(tag, burnChance, spreadChance);
    }
}
