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

    /**
     * Temperature of the fire.
     *
     * <p>
     *     The default value for normal fire is 1100 degrees Celsius.
     * </p>
     *
     * @see net.jmb19905.mixin.FireMixin FireMixin
     * @apiNote The temperatures can be negative. See {@link net.jmb19905.mixin.SoulFireMixin SoulFireMixin}
     * @return The maximum temperature
     */
    int getMaxTemperature();
    /**
     * Opposite of emissivity. It measures how much heat is stored.
     * @return The amount of heat preserved in a fire.
     */
    int getReflectivity();
    /**
     * The factor that multiplies against the ticking speed of a fire.
     * @return A constant tick speed factor.
     */
    double getTickSpeedFactor();
    /**
     * The difference of the fire to its environment.
     * @return Absolute value of the max temperature.
     */
    int getDeltaTemperature();
    /**
     * A value that measures how effective the fire is at emitting its heat.
     * @return Not limited to but ideally treated as a percentage.
     */
    double getEmissivity();
}
