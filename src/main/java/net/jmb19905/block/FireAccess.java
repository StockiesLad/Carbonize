package net.jmb19905.block;

import com.google.common.collect.ImmutableMap;
import net.jmb19905.core.CarbonCore;
import net.jmb19905.mixin.IFireBlock;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.state.StateManager;
import net.minecraft.util.shape.VoxelShape;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.minecraft.block.FireBlock.*;

/**
 * Privileged access to fire mutation logic.
 *
 * <p><strong>WARNING:</strong> This interface exposes internal fire mechanics.
 * Improper use may cause desyncs, broken block states, or mod incompatibility.
 *
 * <p>This interface is intended for:
 * <ul>
 *   <li>FireBlock implementations</li>
 *   <li>Internal registries</li>
 *   <li>Compatibility layers</li>
 * </ul>
 *
 * <p>Consumers should prefer {@link FireView} whenever possible.
 */
public interface FireAccess extends FireCapability {
    void carbonize$appendProperties(StateManager.Builder<Block, BlockState> builder);

    default BlockState carbonize$getDefaultState(StateManager<Block, BlockState> stateManager) {
        return stateManager.getDefaultState()
                .with(AGE, 0)
                .with(NORTH, false)
                .with(EAST, false)
                .with(SOUTH, false)
                .with(WEST, false)
                .with(UP, false);
    }

    @SuppressWarnings("DataFlowIssue")
    default Map<BlockState, VoxelShape> carbonize$getVoxelShapes(StateManager<Block, BlockState> stateManager) {
        return ImmutableMap.copyOf(stateManager
                .getStates()
                .stream()
                .filter((state) -> state.get(AGE) == 0)
                .collect(Collectors.toMap(Function.identity(), IFireBlock::carbonize$getShapeForState))
        );
    }

    static Optional<FireAccess> tryGet(AbstractFireBlock fireBlock, boolean suppressWarnings) {
        if (!suppressWarnings && !(fireBlock instanceof FireBlock))
            CarbonCore.LOGGER.warn("FireAccess requested from a non-mutable fire." +
                    "\nThis is an advanced operation and may break compatibility.");
        return fireBlock instanceof FireAccess fireAccess ? Optional.of(fireAccess) : Optional.empty();
    }
}
