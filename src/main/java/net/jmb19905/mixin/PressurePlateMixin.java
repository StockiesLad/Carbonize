package net.jmb19905.mixin;

import net.jmb19905.util.BlockSetTypeUtil;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.PressurePlateBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.jmb19905.Carbonize.CONFIG;
import static net.jmb19905.block.GenericFireBlock.*;

@Mixin(PressurePlateBlock.class)
public class PressurePlateMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void addFlammability(PressurePlateBlock.ActivationRule rule, Settings settings, BlockSetType type, CallbackInfo ci) {
        registerEarly(() -> {
            if (!CONFIG.moreBurnableBlocks()) return;
            if (BlockSetTypeUtil.isNether(type))
                getSoulFire().carbonize$registerFlammableBlock((PressurePlateBlock)(Object)this, 5, 5);
            else if (!BlockSetTypeUtil.isStable(type))
                getFire().carbonize$registerFlammableBlock((PressurePlateBlock)(Object)this, 5, 5);
        });
    }
}
