package net.jmb19905.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.jmb19905.Carbonize;

public class CarbonizeLanguageDataGen extends FabricLanguageProvider {

    protected CarbonizeLanguageDataGen(FabricDataOutput dataOutput) {
        super(dataOutput, "en_us");
    }

    @Override
    public void generateTranslations(TranslationBuilder translationBuilder) {
        translationBuilder.add(Carbonize.WOOD_STACK, "Wood Stack");
        translationBuilder.add(Carbonize.CHARCOAL_STACK, "Charcoal Stack");
        translationBuilder.add(Carbonize.CHARCOAL_LOG, "Charcoal Log");
        translationBuilder.add(Carbonize.CHARCOAL_PLANKS, "Charcoal Planks");
        translationBuilder.add(Carbonize.CHARCOAL_STAIRS, "Charcoal Stairs");
        translationBuilder.add(Carbonize.CHARCOAL_SLAB, "Charcoal Slab");
        translationBuilder.add(Carbonize.CHARCOAL_FENCE, "Charcoal Fence");
        translationBuilder.add(Carbonize.CHARCOAL_FENCE_GATE, "Charcoal Fence Gate");

        translationBuilder.add(Carbonize.CHARCOAL_BLOCK, "Charcoal Block");
        translationBuilder.add(Carbonize.ASH_LAYER, "Ash Layer");
        translationBuilder.add(Carbonize.ASH_BLOCK, "Ash Block");
        translationBuilder.add(Carbonize.ASH, "Ash");

        translationBuilder.add("text.config.carbonize.option.moreBurnableBlocks", "More Flammable Blocks");
        translationBuilder.add("text.config.carbonize.option.burnableContainers", "Flammable Containers");
        translationBuilder.add("text.config.carbonize.option.charcoalPile", "Charcoal Pile");
        translationBuilder.add("text.config.carbonize.option.charcoalPileMinimumCount", "Charcoal Pile Minimum Count");
        translationBuilder.add("text.config.carbonize.option.burnCrafting", "Block Conversion by Burning");
        translationBuilder.add("text.config.carbonize.option.increasedFireSpreadRange", "Increased Fire Spread Range");
    }
}
