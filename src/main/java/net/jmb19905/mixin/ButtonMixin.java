package net.jmb19905.mixin;

import net.jmb19905.util.BlockSetTypeUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockSetType;
import net.minecraft.block.ButtonBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.jmb19905.Carbonize.CONFIG;
import static net.jmb19905.block.GenericFireBlock.*;
import static net.jmb19905.charcoal_pit.FireType.DEFAULT_FIRE_TYPE;
import static net.jmb19905.charcoal_pit.FireType.SOUL_FIRE_TYPE;

@Mixin(ButtonBlock.class)
public class ButtonMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void addFlammability(AbstractBlock.Settings settings, BlockSetType type, int pressTicks, boolean wooden, CallbackInfo ci) {
        registerEarly(() -> {
            if (!CONFIG.moreBurnableBlocks()) return;
            if (BlockSetTypeUtil.isNether(type))
                SOUL_FIRE_TYPE.carbonize$registerFlammableBlock((ButtonBlock)(Object)this, 5, 5);
            else if (!BlockSetTypeUtil.isStable(type))
                DEFAULT_FIRE_TYPE.carbonize$registerFlammableBlock((ButtonBlock)(Object)this, 5, 5);
        });
    }
}
