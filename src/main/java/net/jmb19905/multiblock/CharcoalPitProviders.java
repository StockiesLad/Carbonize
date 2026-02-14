package net.jmb19905.multiblock;

import net.jmb19905.multiblock.CharcoalPitMultiblock.Info;
import net.jmb19905.util.ObjectHolder;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class CharcoalPitProviders {
    private static final Map<Predicate<World>, Function<Info, CharcoalPitMultiblock>> CONDITIONAL_FACTORY = new HashMap<>();

    /**
     * Please for the love of god, check the predicates to ensure no overlap.
     * @param customCondition Usually in the form of dimension-discrimination.
     * @param multiblockFactory A factory that creates Multiblocks.
     */
    public static void addMultiBlock(Predicate<World> customCondition, Function<Info, CharcoalPitMultiblock> multiblockFactory) {
        CONDITIONAL_FACTORY.put(customCondition, multiblockFactory);
    }

    public static Optional<CharcoalPitMultiblock> createMultiBlock(World world, Info info) {
        ObjectHolder<CharcoalPitMultiblock> multiblock = new ObjectHolder<>(null);
        CONDITIONAL_FACTORY.forEach((customCondition, multiblockFactory) -> {
            if (!customCondition.test(world)) return;
            if (multiblock.isLocked()) throw new IllegalArgumentException("Too many outputs for multiblock creation. There should only be one.");
            multiblock.setValue(multiblockFactory.apply(info)).lock();
        });
        return Optional.of(multiblock.getValue());
    }
}
