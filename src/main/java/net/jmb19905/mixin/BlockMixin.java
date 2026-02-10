package net.jmb19905.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.jmb19905.block.ModularFireBlock;
import net.jmb19905.block.Unregisterable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.DefaultedRegistry;
import net.minecraft.registry.entry.RegistryEntry.Reference;
import net.minecraft.state.StateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin extends AbstractBlockMixin {
    @Shadow
    protected void setDefaultState(BlockState state) {
    }

    @Shadow
    public StateManager<Block, BlockState> getStateManager() {
        return null;
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/DefaultedRegistry;createEntry(Ljava/lang/Object;)Lnet/minecraft/registry/entry/RegistryEntry$Reference;"))
    private <T> Reference<T> discardWhenUnregistered(DefaultedRegistry<T> instance, T block, Operation<Reference<T>> operation) {
        if (block instanceof Unregisterable unregisterable && !unregisterable.shouldRegister())
            return null;
        else return operation.call(instance, block);
    }

    @Inject(method = "appendProperties", at = @At("HEAD"), cancellable = true)
    protected void override$appendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
    }

    @Inject(method = "toString", at = @At("HEAD"), cancellable = true)
    public void fixString(CallbackInfoReturnable<String> cir) {
        if ((Block)(Object) this instanceof ModularFireBlock fireBlock && !fireBlock.shouldRegister())
            cir.setReturnValue(fireBlock.getType().asBlock().toString());
    }
}
