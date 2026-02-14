package net.jmb19905.mixin;

import net.minecraft.block.WoodType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WoodType.class)
public interface IWoodType {
    @NotNull @Invoker("register") static WoodType register(WoodType woodType) {return null;}
}
