package net.jmb19905.charcoal_pit;

import net.jmb19905.block.FireCapability;
import net.jmb19905.block.FireView;
import net.jmb19905.block.ModularFireBlock;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.*;
import java.util.function.Consumer;

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
 *
 * @see ModularFireBlock GenericFireBlock
 */
public final class FireType implements FireView {
    private static final Map<String, FireType> FIRE_TYPES = new HashMap<>();
    public static final FireType DEFAULT_FIRE_TYPE = new FireType("default_fire", (FireBlock) Blocks.FIRE);
    public static final FireType SOUL_FIRE_TYPE = new FireType("soul_fire", (SoulFireBlock) Blocks.SOUL_FIRE);
    private final String serialId;
    private final AbstractFireBlock fireBlock;


    public FireType(String serialId, AbstractFireBlock fireBlock) {
        this.serialId = serialId;
        this.fireBlock = fireBlock;
        FIRE_TYPES.put(serialId, this);

        if (!(fireBlock instanceof FireView))
            throw new IllegalArgumentException("Fire types are expected to be retrofitted with FireView. See JavaDoc of FireType for more info.");
        if (!asBlock().equals(asFireView().asBlock()))
            throw new IllegalStateException("The provided fire block doesn't match it's FireView#asBlock implementation.");
    }

    public FireView asFireView() {
        return (FireView) fireBlock;
    }

    @Override
    public String getSerialId() {
        return serialId;
    }

    @Override
    public AbstractFireBlock asBlock() {
        return fireBlock;
    }

    @Override
    public FireType asFireType() {
        return this;
    }

    @Override
    public boolean isBlockFlammable(BlockState state) {
        return asFireView().isBlockFlammable(state);
    }

    @Override
    public int getBlockSpreadChance(BlockState state) {
        return asFireView().getBlockSpreadChance(state);
    }

    @Override
    public int getBlockBurnChance(BlockState state) {
        return asFireView().getBlockBurnChance(state);
    }

    @Override
    public float getGlobalSpreadChance() {
        return asFireView().getGlobalSpreadChance();
    }

    @Override
    public int getGlobalSpreadFactor() {
        return asFireView().getGlobalSpreadFactor();
    }

    @Override
    public double getLifeSpeedModifier() {
        return asFireView().getLifeSpeedModifier();
    }

    @Override
    public boolean isBaseInfiniburn(BlockView view, BlockPos pos) {
        return asFireView().isBaseInfiniburn(view, pos);
    }

    @Override
    public void ifCapability(Consumer<FireCapability> consumer) {
        asFireView().ifCapability(consumer);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FireType type && type.fireBlock.equals(this.fireBlock) && type.serialId.equals(this.serialId);
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
