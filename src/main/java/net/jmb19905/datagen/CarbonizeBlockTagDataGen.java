package net.jmb19905.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.jmb19905.Carbonize;
import net.jmb19905.charcoal_pit.CharcoalPitInit;
import net.jmb19905.core.CharcoalSet;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

public class CarbonizeBlockTagDataGen extends FabricTagProvider.BlockTagProvider {
    public CarbonizeBlockTagDataGen(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        var charcoalBlocks = getOrCreateTagBuilder(Carbonize.CHARCOAL_BLOCKS);
        CharcoalSet.iterateBlocks((set,block) -> {
            if (block == set.charcoalBlock) return;
            charcoalBlocks.add(block);
        });
        var charcoalPileValidFuel = getOrCreateTagBuilder(Carbonize.CHARCOAL_PILE_VALID_FUEL);
        charcoalPileValidFuel.forceAddTag(BlockTags.LOGS);
        charcoalPileValidFuel.forceAddTag(BlockTags.PLANKS);
        charcoalPileValidFuel.forceAddTag(Carbonize.WOODEN_STACKS);

        var charringBlocks = getOrCreateTagBuilder(Carbonize.CHARRING_BLOCKS);
        charringBlocks.add(CharcoalPitInit.CHARRING_WOOD);
        charringBlocks.add(CharcoalPitInit.SOUL_CHARRING_WOOD);

        var woodenStacks = getOrCreateTagBuilder(Carbonize.WOODEN_STACKS);
        woodenStacks.add(Carbonize.WOOD_STACK);

        var axeMineable = getOrCreateTagBuilder(BlockTags.AXE_MINEABLE);
        axeMineable.setReplace(false);
        axeMineable.forceAddTag(Carbonize.WOODEN_STACKS);
        axeMineable.forceAddTag(Carbonize.CHARRING_BLOCKS);
        axeMineable.forceAddTag(Carbonize.CHARCOAL_BLOCKS);

        var pickaxeMineable = getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE);
        pickaxeMineable.setReplace(false);
        CharcoalSet.iterateSets(set -> pickaxeMineable.add(set.charcoalBlock));

        var shovelMineable = getOrCreateTagBuilder(BlockTags.SHOVEL_MINEABLE);
        shovelMineable.setReplace(false);
        shovelMineable.add(Carbonize.ASH_BLOCK);
        shovelMineable.add(Carbonize.ASH_LAYER);

        var fences = getOrCreateTagBuilder(BlockTags.FENCES);
        fences.setReplace(false);
        CharcoalSet.iterateSets(set -> fences.add(set.charcoalFence));
    }
}
