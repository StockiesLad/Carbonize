package net.jmb19905.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.util.shape.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FireBlock.class)
public interface IFireBlock {
    @Invoker(value = "getShapeForState") static VoxelShape carbonize$getShapeForState(BlockState state) {return null;}
}
