package net.jmb19905.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Block.class)
public interface IBlock {
    @Invoker("setDefaultState") void carbonize$setDefaultState(BlockState state);
    @Accessor("stateManager") StateManager<Block, BlockState> carbonize$getStateManager();
}
