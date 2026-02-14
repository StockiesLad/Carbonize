package net.jmb19905.core;

import net.jmb19905.block.ember.EmberBlock;
import net.minecraft.registry.tag.BlockTags;

import java.util.ArrayList;
import java.util.List;

public class CarbonizeServer {
    public static final List<EmberBlock.Tag2Block> EMBER_BLOCK_RELATIONS;

    static {
        EMBER_BLOCK_RELATIONS = new ArrayList<>();
        EMBER_BLOCK_RELATIONS.add(new EmberBlock.Tag2Block(BlockTags.PLANKS, CarbonizeCommon.CHARCOAL_SET.emberPlanks));
    }
}
