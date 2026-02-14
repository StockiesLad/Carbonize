package net.jmb19905.mixin;

import net.minecraft.block.BlockSetType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockSetType.class)
public interface IBlockSetType {
    @NotNull @Invoker("register") static BlockSetType register(BlockSetType blockSetType) {return null;}
}
