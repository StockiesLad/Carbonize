package net.jmb19905.block;

import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import static net.jmb19905.charcoal_pit.FireType.SOUL_FIRE_TYPE;
import static net.minecraft.block.Blocks.*;

/**
 * This class uses {@link net.jmb19905.mixin.FireMixin FireMixin} to remove hardcoded fire behaviour and make it generalisable;
 * <p> meaning, use this only to copy fire block, don't expect freedom to configure everything.</p>
 * <p>Extend this as any normal class to make a custom fire block at your peril.</p>
 * @see net.jmb19905.mixin.SoulFireMixin SoulFireMixin
 * @implNote Do not implement FireView. Use {@link GenericFireBlock#asFireView()}.
 */
public class GenericFireBlock extends FireBlock implements Unregisterable {
    private static final Queue<Runnable> TASKS = new ArrayDeque<>();

    public static final Supplier<AbstractFireBlock> DEAULT_PARENT_SUPPLIER = () -> (AbstractFireBlock) FIRE;
    public static final BiPredicate<BlockState, TagKey<Block>>  DEFAULT_PLACEMENT_CONDITIONS = AbstractBlockState::isIn;
    public static final BiFunction<BlockView, BlockPos, BlockState> DEFAULT_PLACEMENT_STATE = AbstractFireBlock::getState;

    public static final float DEFAULT_SPREAD_CHANCE = 0.25F;
    public static final int DEFAULT_SPREAD_FACTOR = 400;

    private final boolean shouldRegister;
    public final String serialId;
    public final Supplier<AbstractFireBlock> parentSupplier;
    public final BiPredicate<BlockState, TagKey<Block>> checkPlacementConditions;
    public final BiFunction<BlockView, BlockPos, BlockState> placementState;
    public final float spreadChance;
    public final int spreadFactor;

    public GenericFireBlock(
            boolean shouldRegister,
            Settings settings,
            String serialId,
            Supplier<AbstractFireBlock> parentSupplier,
            BiPredicate<BlockState, TagKey<Block>> checkPlacementConditions,
            BiFunction<BlockView, BlockPos, BlockState> getPlacementState,
            float spreadChance,
            int spreadFactor
    ) {
        super(settings);
        this.shouldRegister = shouldRegister;
        this.serialId = serialId;
        this.parentSupplier = parentSupplier;
        this.checkPlacementConditions = checkPlacementConditions;
        this.placementState = getPlacementState;
        this.spreadChance = spreadChance;
        this.spreadFactor = spreadFactor;
    }

    public GenericFireBlock(Settings settings) {
        this(true, settings, "default_fire", DEAULT_PARENT_SUPPLIER, DEFAULT_PLACEMENT_CONDITIONS, DEFAULT_PLACEMENT_STATE, DEFAULT_SPREAD_CHANCE, DEFAULT_SPREAD_FACTOR);
    }

    public FireView asFireView() {
        return (FireView) this;
    }

    @Override
    public boolean shouldRegister() {
        return shouldRegister;
    }

    public static void registerEarly(Runnable runnable) {
        TASKS.add(runnable);
    }

    public static void registerDefaultFlammables() {
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(WARPED_PLANKS, 5, 20);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(CRIMSON_PLANKS, 5, 20);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(WARPED_SLAB, 5, 20);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(CRIMSON_SLAB, 5, 20);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(WARPED_FENCE_GATE, 5, 20);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(CRIMSON_FENCE_GATE, 5, 20);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(WARPED_FENCE, 5, 20);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(CRIMSON_FENCE, 5, 20);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(WARPED_STAIRS, 5, 20);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(CRIMSON_STAIRS, 5, 20);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(WARPED_STEM, 5, 5);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(CRIMSON_STEM, 5, 5);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(WARPED_HYPHAE, 5, 5);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(CRIMSON_HYPHAE, 5, 5);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(STRIPPED_WARPED_STEM, 5, 5);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(STRIPPED_CRIMSON_STEM, 5, 5);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(STRIPPED_WARPED_HYPHAE, 5, 5);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(STRIPPED_CRIMSON_HYPHAE, 5, 5);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(WARPED_WART_BLOCK, 30, 60);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(NETHER_WART_BLOCK, 30, 60);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(SHROOMLIGHT, 30, 60);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(WARPED_ROOTS, 60, 100);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(CRIMSON_ROOTS, 60, 100);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(WARPED_FUNGUS, 60, 100);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(CRIMSON_FUNGUS, 60, 100);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(WEEPING_VINES, 15, 100);
        SOUL_FIRE_TYPE.carbonize$registerFlammableBlock(TWISTING_VINES, 15, 100);

        while (!TASKS.isEmpty())
            TASKS.poll().run();
    }
}
