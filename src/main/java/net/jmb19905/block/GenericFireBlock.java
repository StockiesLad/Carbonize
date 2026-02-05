package net.jmb19905.block;

import net.minecraft.block.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import static net.minecraft.block.Blocks.*;

/**
 * This class uses {@link net.jmb19905.mixin.FireMixin FireMixin} to remove hardcoded fire behaviour and make it generalisable.
 * <p>Extend this as any normal class to make a custom fire block at your peril.</p>
 * @see net.jmb19905.mixin.SoulFireMixin SoulFireMixin
 */
public class GenericFireBlock extends FireBlock implements Unregisterable {
    private static final Queue<Runnable> TASKS = new ArrayDeque<>();

    public static final Supplier<AbstractFireBlock> DEAULT_PARENT_SUPPLIER = () -> (AbstractFireBlock) FIRE;
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

    public static FireView getSoulFire() {
        return (FireView) SOUL_FIRE;
    }

    public static FireView getFire() {
        return (FireView) FIRE;
    }

    public static void registerEarly(Runnable runnable) {
        TASKS.add(runnable);
    }

    public static void registerDefaultFlammables() {
        register(WARPED_PLANKS, 5, 20);
        register(CRIMSON_PLANKS, 5, 20);
        register(WARPED_SLAB, 5, 20);
        register(CRIMSON_SLAB, 5, 20);
        register(WARPED_FENCE_GATE, 5, 20);
        register(CRIMSON_FENCE_GATE, 5, 20);
        register(WARPED_FENCE, 5, 20);
        register(CRIMSON_FENCE, 5, 20);
        register(WARPED_STAIRS, 5, 20);
        register(CRIMSON_STAIRS, 5, 20);
        register(WARPED_STEM, 5, 5);
        register(CRIMSON_STEM, 5, 5);
        register(WARPED_HYPHAE, 5, 5);
        register(CRIMSON_HYPHAE, 5, 5);
        register(STRIPPED_WARPED_STEM, 5, 5);
        register(STRIPPED_CRIMSON_STEM, 5, 5);
        register(STRIPPED_WARPED_HYPHAE, 5, 5);
        register(STRIPPED_CRIMSON_HYPHAE, 5, 5);
        register(WARPED_WART_BLOCK, 30, 60);
        register(NETHER_WART_BLOCK, 30, 60);
        register(SHROOMLIGHT, 30, 60);
        register(WARPED_ROOTS, 60, 100);
        register(CRIMSON_ROOTS, 60, 100);
        register(WARPED_FUNGUS, 60, 100);
        register(CRIMSON_FUNGUS, 60, 100);
        register(WEEPING_VINES, 15, 100);
        register(TWISTING_VINES, 15, 100);

        while (!TASKS.isEmpty())
            TASKS.poll().run();
    }

    private static void register(Block block, int burnChance, int spreadChance) {
        getSoulFire().carbonize$registerFlammableBlock(block, burnChance, spreadChance);
    }

    private static void register(Identifier blockId, int burnChance, int spreadChance) {
        register(Registries.BLOCK.get(blockId), burnChance, spreadChance);
    }
}
