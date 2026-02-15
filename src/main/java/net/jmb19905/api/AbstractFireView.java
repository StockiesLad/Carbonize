package net.jmb19905.api;

import net.jmb19905.block.charring.CharringWoodBlock;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.function.Consumer;

/**
 * An interface that retrieves important data from fire blocks.
 * <p>
 *     {@link #getTickSpeedFactor()} influences charcoal pit individual burnTimes... stored energy
 * </p>
 * <p>
 *     {@link #getMaxTemperature()} determines charcoal pit ignition time... heat
 * </p>
 * <p>
 *     {@link #getReflectivity()} determines charcoal pit burnTime drop off... expansion
 * </p>
 */
public interface AbstractFireView {
    String getSerialId();
    boolean canPlace(BlockView view, BlockPos pos);
    void ifCapability(Consumer<FireCapability> consumer);

    AbstractFireBlock asFireBlock();
    CharringWoodBlock asCharringBlock();
    DefaultParticleType asFlameParticle();

    boolean isBaseInfiniburn(BlockView view, BlockPos pos);
    boolean isBlockFlammable(BlockState state);
    int getBlockSpreadChance(BlockState state);
    int getBlockBurnChance(BlockState state);

    int getMaxTemperature();
    int getReflectivity();
    double getTickSpeedFactor();
    int getDeltaTemperature();
    double getEmissivity();
}
