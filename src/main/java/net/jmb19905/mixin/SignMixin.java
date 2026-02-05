package net.jmb19905.mixin;

import net.jmb19905.util.WoodTypeUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WoodType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.jmb19905.Carbonize.CONFIG;
import static net.jmb19905.block.GenericFireBlock.*;

@Mixin(SignBlock.class)
public class SignMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void addFlammability(AbstractBlock.Settings settings, WoodType type, CallbackInfo ci) {
        registerEarly(() -> {
            if (!CONFIG.moreBurnableBlocks()) return;
            if (WoodTypeUtil.isNether(type))
                getSoulFire().carbonize$registerFlammableBlock((SignBlock)(Object)this, 5, 5);
            else if (!WoodTypeUtil.isStable(type))
                getFire().carbonize$registerFlammableBlock((SignBlock)(Object)this, 5, 5);
        });
    }
}
