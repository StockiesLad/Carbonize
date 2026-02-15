package net.jmb19905.api;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.function.Consumer;

public interface FireViewProvider extends AbstractFireView {
    default FireView asFireView() {
        return (FireView) asFireBlock();
    }

    @Override
    default boolean isBlockFlammable(BlockState state) {
        return asFireView().isBlockFlammable(state);
    }

    @Override
    default int getBlockSpreadChance(BlockState state) {
        return asFireView().getBlockSpreadChance(state);
    }

    @Override
    default int getBlockBurnChance(BlockState state) {
        return asFireView().getBlockBurnChance(state);
    }

    @Override
    default int getDeltaTemperature() {
        return asFireView().getDeltaTemperature();
    }

    @Override
    default int getGlobalSpreadFactor() {
        return asFireView().getGlobalSpreadFactor();
    }

    @Override
    default double getTickSpeedModifier() {
        return asFireView().getTickSpeedModifier();
    }

    @Override
    default boolean isBaseInfiniburn(BlockView view, BlockPos pos) {
        return asFireView().isBaseInfiniburn(view, pos);
    }

    @Override
    default void ifCapability(Consumer<FireCapability> consumer) {
        asFireView().ifCapability(consumer);
    }

    @Override
    default boolean canPlace(BlockView view, BlockPos pos) {
        return asFireView().canPlace(view, pos);
    }
}
