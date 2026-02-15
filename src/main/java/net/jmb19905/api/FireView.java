package net.jmb19905.api;

import net.jmb19905.block.charring.CharringWoodBlock;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

import java.util.function.Consumer;

public interface FireView extends AbstractFireView {
    FireType asFireType();

    @Override
    default AbstractFireBlock asFireBlock() {
        return asFireType().asFireBlock();
    }

    @Override
    default CharringWoodBlock asCharringBlock() {
        return asFireType().asCharringBlock();
    }

    @Override
    default String getSerialId() {
        return asFireType().getSerialId();
    }

    @Override
    default boolean canPlace(BlockView view, BlockPos pos) {
        return isBaseInfiniburn(view, pos) || isBaseInfiniburn(view, pos);
    }

    @Override
    default void ifCapability(Consumer<FireCapability> consumer) {
        if (this instanceof FireCapability capability)
            consumer.accept(capability);
    }
}
