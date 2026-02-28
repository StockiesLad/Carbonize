package net.jmb19905.core;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class CarbonizeServer {
    //TODO: add entries
    public static final TagKey<Block> BURNING_LOGS = TagKey.of(RegistryKeys.BLOCK, new Identifier(CarbonizeConstants.MOD_ID, "burning_logs"));
}
