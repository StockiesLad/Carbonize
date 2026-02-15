package net.jmb19905.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.jmb19905.block.BurningSet;
import net.jmb19905.core.CarbonizeCommon;
import net.minecraft.registry.Registries;

public class CarbonizeLanguageDataGen extends FabricLanguageProvider {

    protected CarbonizeLanguageDataGen(FabricDataOutput dataOutput) {
        super(dataOutput, "en_us");
    }

    @Override
    public void generateTranslations(TranslationBuilder translationBuilder) {
        translationBuilder.add(CarbonizeCommon.WOOD_STACK, "Wood Stack");
        BurningSet.iterateBlocks((set, block) -> {
            StringBuilder id = new StringBuilder(Registries.BLOCK.getId(block).getPath());
            var subStrings = id.toString().split("_");
            id = new StringBuilder();
            for (String subString : subStrings) {
                id.append(" ").append(subString.replaceFirst(subString.charAt(0) + "", (subString.charAt(0) + "").toUpperCase()));
            }
            translationBuilder.add(block, id.toString().trim());
        });

        translationBuilder.add(CarbonizeCommon.ASH_LAYER, "Ash Layer");
        translationBuilder.add(CarbonizeCommon.ASH_BLOCK, "Ash Block");
        translationBuilder.add(CarbonizeCommon.ASH, "Ash");

        translationBuilder.add("text.config.carbonize.option.moreBurnableBlocks", "More Flammable Blocks");
        translationBuilder.add("text.config.carbonize.option.burnableContainers", "Flammable Containers");
        translationBuilder.add("text.config.carbonize.option.charcoalPile", "Charcoal Pile");
        translationBuilder.add("text.config.carbonize.option.charcoalPileMinimumCount", "Charcoal Pile Minimum Count");
        translationBuilder.add("text.config.carbonize.option.burnCrafting", "Block Conversion by Burning");
        translationBuilder.add("text.config.carbonize.option.increasedFireSpreadRange", "Increased Fire Spread Range");
        translationBuilder.add("text.jade.carbonize.charring_wood.size", "Size: %d blocks");
        translationBuilder.add("text.jade.carbonize.charring_wood.stage", "Stage: %d");
        translationBuilder.add("text.jade.carbonize.charring_wood.remaining_burn_time", "Remaining Time: %ds");
        translationBuilder.add("config.jade.plugin_carbonize.plugin", "Enable Plugin");
        translationBuilder.add("config.jade.plugin_carbonize.show_size", "Show Size");
        translationBuilder.add("config.jade.plugin_carbonize.show_stage", "Show Stage");
        translationBuilder.add("config.jade.plugin_carbonize.show_remaining_burn_time", "Show Remaining Time");
        translationBuilder.add("itemGroup.carbonize.charcoals", "Charcoals");

    }
}
