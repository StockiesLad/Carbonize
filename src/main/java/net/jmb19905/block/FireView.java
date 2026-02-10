package net.jmb19905.block;

import net.jmb19905.charcoal_pit.FireType;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.function.Consumer;

/**
 * An interface that retrieves important data from fire blocks.
 * <p>
 *     {@link #getLifeSpeedModifier()} determines charcoal pit individual burnTimes
 * </p>
 * <p>
 *     {@link #getGlobalSpreadChance()} determines charcoal pit ignition time
 * </p>
 * <p>
 *     {@link #getGlobalSpreadFactor()} determines charcoal pit burnTime drop off.
 * </p>
 */
public interface FireView {
    FireType asFireType();
    AbstractFireBlock asBlock();
    boolean isBaseInfiniburn(BlockView view, BlockPos pos);
    boolean isBlockFlammable(BlockState state);
    int getBlockSpreadChance(BlockState state);
    int getBlockBurnChance(BlockState state);
    float getGlobalSpreadChance();
    int getGlobalSpreadFactor();
    double getLifeSpeedModifier();

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
