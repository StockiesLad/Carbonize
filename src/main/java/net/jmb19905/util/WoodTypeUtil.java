package net.jmb19905.util;

import net.minecraft.block.WoodType;

import static net.minecraft.block.WoodType.CRIMSON;

public class WoodTypeUtil {
    public static boolean isStable(WoodType type) {
        return false;
    }

    public static boolean isNether(WoodType type) {
        return type.equals(CRIMSON) || type.equals(WoodType.WARPED);
    }

    public static boolean isAether(WoodType type) {
        return false;
    }

    public static boolean isOverworld(WoodType type) {
        return !(isStable(type) || isNether(type) || isAether(type));
    }
}
