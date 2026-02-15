package net.jmb19905.api;

import net.jmb19905.block.charring.CharringWoodBlock;
import net.jmb19905.block.fire.ModularFireBlock;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.SoulFireBlock;

import java.util.*;
import java.util.function.Supplier;

import static net.jmb19905.core.CarbonizeCommon.CHARCOAL_SET;
import static net.jmb19905.core.CarbonizeCommon.SOUL_CHARCOAL_SET;

/**
 * Represents a type of fire.
 *
 * <p>
 *     A fire, whether passive like vanilla Soul Fire or active like vanilla Fire, must present the necessary data
 *     that a charcoal pit can adapt its behaviour for.
 * </p>
 * <p>
 *     This data includes (but is not limited to) blocks that are flammable to one type of fire,
 *     the burn values of the fire itself but also values pertaining to the flammability of a block, too.
 * </p>
 * <p>
 *     Such data is required to chose which charcoal pit for type of flammability. If one happens to be flammable
 *     to more than one, we would also want the fire speed to scale the speed of the given charcoal pit.
 *     A more niche example includes implicit charcoal pits. In an instance one block is universally flammable, it can
 *     be discriminated by their "infiniburn" block (Soulsand for Soul Fire and Netherrack for normal Fire)
 * </p>
 * @apiNote serialIds are separated to avoid registry issues.
 * @see ModularFireBlock GenericFireBlock
 */
public final class FireType implements FireViewProvider {
    private static final Map<String, FireType> FIRE_TYPES = new HashMap<>();
    public static final FireType DEFAULT_FIRE_TYPE = new FireType("default_fire", () -> (FireBlock) Blocks.FIRE, () -> (CharringWoodBlock) CHARCOAL_SET.charringWood);
    public static final FireType SOUL_FIRE_TYPE = new FireType("soul_fire", () -> (SoulFireBlock) Blocks.SOUL_FIRE, () -> (CharringWoodBlock) SOUL_CHARCOAL_SET.charringWood);
    private final String serialId;
    private final Supplier<AbstractFireBlock> fireBlock;
    private final Supplier<CharringWoodBlock> charringBlock;

    public FireType(String serialId, Supplier<AbstractFireBlock> fireBlock, Supplier<CharringWoodBlock> charringBlock) {
        this.serialId = serialId;
        this.fireBlock = fireBlock;
        this.charringBlock = charringBlock;
        FIRE_TYPES.put(serialId, this);
    }

    @Override
    public String getSerialId() {
        return serialId;
    }

    @Override
    public AbstractFireBlock asFireBlock() {
        return fireBlock.get();
    }

    @Override
    public CharringWoodBlock asCharringBlock() {
        return charringBlock.get();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FireType type &&
                type.fireBlock.equals(this.fireBlock) &&
                type.serialId.equals(this.serialId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serialId, fireBlock);
    }

    @Override
    public String toString() {
        return "FireType[" +
                "serialId=" + serialId + ", " +
                "fireBlock=" + fireBlock + ']';
    }

    public static void verify() {
        for (FireType fireType : getAllTypes()) {
            if (!(fireType.asFireBlock() instanceof FireView))
                throw new IllegalArgumentException("Fire types are expected to be retrofitted with FireView. See JavaDoc of FireType for more info.");
            if (!fireType.asFireBlock().equals(fireType.asFireView().asFireBlock()))
                throw new IllegalStateException("The provided fire block doesn't match it's FireView#asBlock implementation.");
        }
    }

    public static Collection<FireType> getAllTypes() {
        return FIRE_TYPES.values();
    }

    public static Optional<FireType> find(String serialId) {
        return Optional.of(FIRE_TYPES.get(serialId));
    }

    public static Optional<FireType> find(FireView view) {
        return find(view.getSerialId());
    }

}
