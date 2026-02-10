package net.jmb19905.block;

import net.jmb19905.charcoal_pit.FireType;
import net.jmb19905.util.ObjectHolder;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Supplier;

import static net.jmb19905.charcoal_pit.FireType.DEFAULT_FIRE_TYPE;
import static net.jmb19905.charcoal_pit.FireType.SOUL_FIRE_TYPE;
import static net.minecraft.block.Blocks.*;

/**
 * This class uses {@link net.jmb19905.mixin.FireMixin FireMixin} to remove hardcoded fire behaviour and make it generalisable;
 * <p> meaning, use this only to copy fire block, don't expect freedom to configure everything.</p>
 * <p>Extend this as any normal class to make a custom fire block at your peril.</p>
 * @see net.jmb19905.mixin.SoulFireMixin SoulFireMixin
 * @implNote Do not implement FireView. Use {@link ModularFireBlock#access(AbstractFireBlock)}.
 */
public class ModularFireBlock extends FireBlock implements Unregisterable {
    private static final Queue<Runnable> TASKS = new ArrayDeque<>();

    public static final Supplier<FireType> DEAULT_PARENT_SUPPLIER = () -> DEFAULT_FIRE_TYPE;

    private final boolean shouldRegister;
    private final Supplier<FireType> parentSupplier;

    public ModularFireBlock(
            boolean shouldRegister,
            Settings settings,
            Supplier<FireType> parentSupplier
    ) {
        super(settings);
        this.shouldRegister = shouldRegister;
        this.parentSupplier = parentSupplier;
    }

    public ModularFireBlock(Settings settings) {
        this(true, settings, DEAULT_PARENT_SUPPLIER);
    }

    /**
     * @apiNote Do not use this internally within {@link FireBlock}, use {@link #parentView()} instead.
     */
    public FireAccess access(AbstractFireBlock fireBlock) {
        if (!fireBlock.equals(getType().asBlock()))
            throw new IllegalCallerException("Internals were accessed from a non-parent object.");
        return FireAccess.tryGet(this, false).orElseThrow();
    }

    public FireView parentView() {
        return parentSupplier.get().asFireView();
    }

    public FireType getType() {
        return parentSupplier.get();
    }

    @Override
    public boolean shouldRegister() {
        return shouldRegister;
    }

    public static void registerEarly(Runnable runnable) {
        TASKS.add(runnable);
    }

    public static void registerDefaultFlammables() {
        SOUL_FIRE_TYPE.ifCapability(capability -> {
            capability.registerFlammable(WARPED_PLANKS, 5, 20);
            capability.registerFlammable(CRIMSON_PLANKS, 5, 20);
            capability.registerFlammable(WARPED_SLAB, 5, 20);
            capability.registerFlammable(CRIMSON_SLAB, 5, 20);
            capability.registerFlammable(WARPED_FENCE_GATE, 5, 20);
            capability.registerFlammable(CRIMSON_FENCE_GATE, 5, 20);
            capability.registerFlammable(WARPED_FENCE, 5, 20);
            capability.registerFlammable(CRIMSON_FENCE, 5, 20);
            capability.registerFlammable(WARPED_STAIRS, 5, 20);
            capability.registerFlammable(CRIMSON_STAIRS, 5, 20);
            capability.registerFlammable(WARPED_STEM, 5, 5);
            capability.registerFlammable(CRIMSON_STEM, 5, 5);
            capability.registerFlammable(WARPED_HYPHAE, 5, 5);
            capability.registerFlammable(CRIMSON_HYPHAE, 5, 5);
            capability.registerFlammable(STRIPPED_WARPED_STEM, 5, 5);
            capability.registerFlammable(STRIPPED_CRIMSON_STEM, 5, 5);
            capability.registerFlammable(STRIPPED_WARPED_HYPHAE, 5, 5);
            capability.registerFlammable(STRIPPED_CRIMSON_HYPHAE, 5, 5);
            capability.registerFlammable(WARPED_WART_BLOCK, 30, 60);
            capability.registerFlammable(NETHER_WART_BLOCK, 30, 60);
            capability.registerFlammable(SHROOMLIGHT, 30, 60);
            capability.registerFlammable(WARPED_ROOTS, 60, 100);
            capability.registerFlammable(CRIMSON_ROOTS, 60, 100);
            capability.registerFlammable(WARPED_FUNGUS, 60, 100);
            capability.registerFlammable(CRIMSON_FUNGUS, 60, 100);
            capability.registerFlammable(WEEPING_VINES, 15, 100);
            capability.registerFlammable(TWISTING_VINES, 15, 100);
        });

        while (!TASKS.isEmpty())
            TASKS.poll().run();
    }

    public static BlockState findAppropriateFire(BlockView view, BlockPos pos, @Nullable FireType parent) {
        ObjectHolder<FireType> fireState = new ObjectHolder<>(parent != null ? parent : FireType.DEFAULT_FIRE_TYPE);
        if (parent == null || !parent.canPlace(view, pos))
            for (FireType fireType : FireType.getAllTypes()) {
                if (fireType.canPlace(view, pos)) {
                    fireState.setValue(fireType);
                    break;
                }
            }
        else for (FireType fireType : FireType.getAllTypes()) {
            if (fireType.isBaseInfiniburn(view, pos)) {
                fireState.setValue(fireType);
                break;
            }
        }
        return fireState.getValue().asFireView() instanceof FireCapability capability ? capability.findAppropriateState(view, pos) : fireState.getValue().asBlock().getDefaultState();
    }
}
