package net.jmb19905.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.jmb19905.block.ISoulFireAccess;
import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractFireBlock.class)
public class AbstractFireMixin extends BlockMixin {
    @Inject(method = "getOutlineShape", at = @At("HEAD"), cancellable = true)
    public void override$getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
    }

    @Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
    public void override$getPlacementState(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> cir) {
    }

    @Inject(method = "onBlockAdded", at = @At("HEAD"), cancellable = true)
    public void override$onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo ci) {
    }

    @Redirect(method = "getState", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FireBlock;getStateForPosition(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    private static BlockState discriminate(FireBlock fireBlock, BlockView world, BlockPos pos, @Local BlockState state) {
        if (((ISoulFireAccess)Blocks.SOUL_FIRE).carbonize$isFlammable(state))
            return ((ISoulFireAccess)Blocks.SOUL_FIRE).carbonize$getStateForPosition(world, pos);
        else return ((IFireAccess)fireBlock).carbonize$getStateForPosition(world, pos);
    }
}
