package net.jmb19905.util;

import net.minecraft.block.BlockSetType;

import static net.minecraft.block.BlockSetType.*;

public class BlockSetTypeUtil {
    public static boolean isStable(BlockSetType type) {
        return type.equals(IRON) || type.equals(GOLD) || type.equals(POLISHED_BLACKSTONE) || type.equals(STONE);
    }

    public static boolean isNether(BlockSetType type) {
        return type.equals(CRIMSON) || type.equals(WARPED);
    }

    public static boolean isAether(BlockSetType type) {
        return false;
    }

    public static boolean isOverworld(BlockSetType type) {
        return !(isStable(type) || isNether(type) || isAether(type));
    }
}
