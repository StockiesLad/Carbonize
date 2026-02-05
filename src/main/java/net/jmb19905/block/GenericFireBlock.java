package net.jmb19905.block;

import net.minecraft.block.*;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

/**
 * This class uses {@link net.jmb19905.mixin.FireMixin FireMixin} to remove hardcoded fire behaviour and make it generalisable.
 * <p>Extend this as any normal class to make a custom fire block at your peril.</p>
 * @see net.jmb19905.mixin.SoulFireMixin SoulFireMixin
 */
public class GenericFireBlock extends FireBlock implements Unregisterable {
    public static final Supplier<AbstractFireBlock> DEAULT_PARENT_SUPPLIER = () -> (AbstractFireBlock) Blocks.FIRE;
    public static final BiPredicate<BlockState, TagKey<Block>>  DEFAULT_PLACEMENT_CONDITIONS = AbstractBlockState::isIn;
    public static final BiFunction<BlockView, BlockPos, BlockState> DEFAULT_PLACEMENT_STATE = AbstractFireBlock::getState;

    public static final float DEFAULT_SPREAD_CHANCE = 0.25F;
    public static final int DEFAULT_SPREAD_FACTOR = 400;

    private final boolean shouldRegister;
    public final Supplier<AbstractFireBlock> parentSupplier;
    public final BiPredicate<BlockState, TagKey<Block>> checkPlacementConditions;
    public final BiFunction<BlockView, BlockPos, BlockState> placementState;
    public final float spreadChance;
    public final int spreadFactor;

    public GenericFireBlock(
            boolean shouldRegister,
            Settings settings,
            Supplier<AbstractFireBlock> parentSupplier,
            BiPredicate<BlockState, TagKey<Block>> checkPlacementConditions,
            BiFunction<BlockView, BlockPos, BlockState> getPlacementState,
            float spreadChance,
            int spreadFactor
    ) {
        super(settings);
        this.shouldRegister = shouldRegister;
        this.parentSupplier = parentSupplier;
        this.checkPlacementConditions = checkPlacementConditions;
        this.placementState = getPlacementState;
        this.spreadChance = spreadChance;
        this.spreadFactor = spreadFactor;
    }

    public GenericFireBlock(Settings settings) {
        this(true, settings, DEAULT_PARENT_SUPPLIER, DEFAULT_PLACEMENT_CONDITIONS, DEFAULT_PLACEMENT_STATE, DEFAULT_SPREAD_CHANCE, DEFAULT_SPREAD_FACTOR);
    }

    @Override
    public boolean shouldRegister() {
        return shouldRegister;
    }

    public static void registerDefaultFlammables() {
        var soulFireBlock = (ISoulFireAccess) Blocks.SOUL_FIRE;

        soulFireBlock.carbonize$registerFlammableBlock(Blocks.CRIMSON_PLANKS, 50, 50);
        soulFireBlock.carbonize$registerFlammableBlock(Blocks.CRIMSON_HYPHAE, 50, 50);
        soulFireBlock.carbonize$registerFlammableBlock(Blocks.WARPED_PLANKS, 50, 50);
    }
}
