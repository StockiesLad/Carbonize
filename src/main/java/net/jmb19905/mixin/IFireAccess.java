package net.jmb19905.mixin;

import net.jmb19905.block.FireView;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FireBlock.class)
public interface IFireAccess extends FireView {
    @Override @Invoker(value = "isFlammable") boolean carbonize$isFlammable(BlockState state);
    @Override @Invoker(value = "registerFlammableBlock") void carbonize$registerFlammableBlock(Block block, int burnChance, int spreadChance);
    @Override @Invoker(value = "getSpreadChance") int carbonize$getSpreadChance(BlockState state);
    @Override @Invoker(value = "appendProperties") void carbonize$appendProperties(StateManager.Builder<Block, BlockState> builder);
    @Invoker(value = "getShapeForState") static VoxelShape carbonize$getShapeForState(BlockState state) {return null;}
    @Override @Invoker(value = "getStateForPosition") BlockState carbonize$getStateForPosition(BlockView world, BlockPos pos);
}
