package net.jmb19905.mixin;

import net.jmb19905.block.ModularFireBlock;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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

    @Inject(method = "getState", at = @At("HEAD"), cancellable = true)
    private static void getAppropriateState(BlockView view, BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        cir.setReturnValue(ModularFireBlock.findAppropriateFire(view, pos, null));
        cir.cancel();
    }
}
