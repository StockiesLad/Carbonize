package net.jmb19905.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.jmb19905.core.CarbonizeCommon;
import net.jmb19905.block.charcoal.BurningSet;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

public class CarbonizeBlockTagDataGen extends FabricTagProvider.BlockTagProvider {
    public CarbonizeBlockTagDataGen(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        var charcoalBlocks = getOrCreateTagBuilder(CarbonizeCommon.CHARCOAL_BLOCKS);
        BurningSet.iterateBlocks((set, block) -> {
            if (block == set.charcoalBlock) return;
            charcoalBlocks.add(block);
        });
        var charcoalPileValidFuel = getOrCreateTagBuilder(CarbonizeCommon.CHARCOAL_PILE_VALID_FUEL);
        charcoalPileValidFuel.forceAddTag(BlockTags.LOGS);
        charcoalPileValidFuel.forceAddTag(BlockTags.PLANKS);
        charcoalPileValidFuel.forceAddTag(CarbonizeCommon.WOODEN_STACKS);

        var charringBlocks = getOrCreateTagBuilder(CarbonizeCommon.CHARRING_BLOCKS);
        BurningSet.iterateSets(set -> charringBlocks.add(set.charringWood));

        var woodenStacks = getOrCreateTagBuilder(CarbonizeCommon.WOODEN_STACKS);
        woodenStacks.add(CarbonizeCommon.WOOD_STACK);

        var axeMineable = getOrCreateTagBuilder(BlockTags.AXE_MINEABLE);
        axeMineable.setReplace(false);
        axeMineable.forceAddTag(CarbonizeCommon.WOODEN_STACKS);
        axeMineable.forceAddTag(CarbonizeCommon.CHARRING_BLOCKS);
        axeMineable.forceAddTag(CarbonizeCommon.CHARCOAL_BLOCKS);

        var pickaxeMineable = getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE);
        pickaxeMineable.setReplace(false);
        BurningSet.iterateSets(set -> pickaxeMineable.add(set.charcoalBlock));

        var shovelMineable = getOrCreateTagBuilder(BlockTags.SHOVEL_MINEABLE);
        shovelMineable.setReplace(false);
        shovelMineable.add(CarbonizeCommon.ASH_BLOCK);
        shovelMineable.add(CarbonizeCommon.ASH_LAYER);

        var fences = getOrCreateTagBuilder(BlockTags.FENCES);
        fences.setReplace(false);
        BurningSet.iterateSets(set -> fences.add(set.charcoalFence));
    }
}
