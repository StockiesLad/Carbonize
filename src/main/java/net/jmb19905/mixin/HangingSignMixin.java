package net.jmb19905.mixin;

import net.jmb19905.util.WoodTypeUtil;
import net.minecraft.block.HangingSignBlock;
import net.minecraft.block.WoodType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.jmb19905.block.ModularFireBlock.Settings;
import static net.jmb19905.block.ModularFireBlock.registerEarly;
import static net.jmb19905.charcoal_pit.FireType.DEFAULT_FIRE_TYPE;
import static net.jmb19905.charcoal_pit.FireType.SOUL_FIRE_TYPE;
import static net.jmb19905.core.CarbonCore.CONFIG;

@Mixin(HangingSignBlock.class)
public class HangingSignMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void addFlammability(Settings settings, WoodType type, CallbackInfo ci) {
        registerEarly(() -> {
            if (!CONFIG.moreBurnableBlocks()) return;
            if (WoodTypeUtil.isNether(type))
                SOUL_FIRE_TYPE.ifCapability(c -> c.registerFlammable((HangingSignBlock)(Object)this, 5, 5));
            else if (!WoodTypeUtil.isStable(type))
                DEFAULT_FIRE_TYPE.ifCapability(c -> c.registerFlammable((HangingSignBlock)(Object)this, 5, 5));
        });
    }
}
