package net.jmb19905.api;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.function.Consumer;

/**
 * An interface that retrieves important data from fire blocks.
 * <p>
 *     {@link #getTickSpeedModifier()} influences charcoal pit individual burnTimes... stored energy
 * </p>
 * <p>
 *     {@link #getDeltaTemperature()} determines charcoal pit ignition time... heat
 * </p>
 * <p>
 *     {@link #getGlobalSpreadFactor()} determines charcoal pit burnTime drop off... expansion
 * </p>
 */
public interface FireView {
    FireType asFireType();
    AbstractFireBlock asBlock();
    boolean isBaseInfiniburn(BlockView view, BlockPos pos);
    boolean isBlockFlammable(BlockState state);
    int getBlockSpreadChance(BlockState state);
    int getBlockBurnChance(BlockState state);
    int getDeltaTemperature();
    int getGlobalSpreadFactor();
    double getTickSpeedModifier();

    default String getSerialId() {
        return asFireType().getSerialId();
    }

    default boolean canPlace(BlockView view, BlockPos pos) {
        return isBaseInfiniburn(view, pos) || isBaseInfiniburn(view, pos);
    }

    default void ifCapability(Consumer<FireCapability> consumer) {
        if (this instanceof FireCapability capability)
            consumer.accept(capability);
    }
}
