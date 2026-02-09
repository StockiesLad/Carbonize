package net.jmb19905.charcoal_pit;

import net.jmb19905.block.FireView;
import net.minecraft.block.*;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.*;

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
 */
public record FireType(String serialId, AbstractFireBlock fireBlock) implements FireView {
    private static final Map<String, FireType> FIRE_TYPES = new HashMap<>();
    public static final FireType DEFAULT_FIRE_TYPE = new FireType("default_fire", (FireBlock) Blocks.FIRE);
    public static final FireType SOUL_FIRE_TYPE = new FireType("soul_fire", (SoulFireBlock) Blocks.SOUL_FIRE);

    public FireType(String serialId, AbstractFireBlock fireBlock) {
        this.serialId = serialId;
        this.fireBlock = fireBlock;
        if (!(fireBlock instanceof FireView))
            throw new IllegalArgumentException("Fire types are expected to be retrofitted with FireView. " +
                    "\n This is because it's burn behaviour influences charcoal pits. See JavaDoc of FireType for more info.");
        FIRE_TYPES.put(serialId, this);
    }

    public FireView asFireView() {
        return (FireView) fireBlock;
    }

    @Override
    public void carbonize$registerFlammableBlock(Block block, int burnChance, int spreadChance) {
        asFireView().carbonize$registerFlammableBlock(block, burnChance, spreadChance);
    }

    @Override
    public boolean carbonize$isFlammable(BlockState state) {
        return asFireView().carbonize$isFlammable(state);
    }

    @Override
    public int carbonize$getSpreadChance(BlockState state) {
        return asFireView().carbonize$getSpreadChance(state);
    }

    @Override
    public int carbonize$getBurnChance(BlockState state) {
        return asFireView().carbonize$getBurnChance(state);
    }

    @Override
    public String carbonize$getSerialId() {
        return asFireView().carbonize$getSerialId();
    }

    @Override
    public void carbonize$appendProperties(StateManager.Builder<Block, BlockState> builder) {
        asFireView().carbonize$appendProperties(builder);
    }

    @Override
    public BlockState carbonize$getStateForPosition(BlockView world, BlockPos pos) {
        return asFireView().carbonize$getStateForPosition(world, pos);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FireType(String id, AbstractFireBlock block) && block.equals(this.fireBlock) && id.equals(this.serialId);
    }

    public static Collection<FireType> getAllTypes() {
        return FIRE_TYPES.values();
    }

    public static Optional<FireType> find(String serialId) {
        return Optional.of(FIRE_TYPES.get(serialId));
    }
    
    public static Optional<FireType> find(FireView view) {
        return find(view.carbonize$getSerialId());
    }
}
