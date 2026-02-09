package net.jmb19905.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.jmb19905.Carbonize;
import net.jmb19905.charcoal_pit.CharcoalPitInit;
import net.jmb19905.charcoal_pit.FireType;
import net.jmb19905.charcoal_pit.multiblock.CharcoalPitMultiblock;
import net.jmb19905.recipe.BurnRecipe;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.function.Consumer;

import static net.jmb19905.Carbonize.CHARCOAL_SET;
import static net.jmb19905.Carbonize.SOUL_CHARCOAL_SET;

public class CarbonizeRecipeDataGen extends FabricRecipeProvider {

    public CarbonizeRecipeDataGen(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(Consumer<RecipeJsonProvider> exporter) {
        offerReversibleCompactingRecipes(exporter, RecipeCategory.MISC, Items.CHARCOAL, RecipeCategory.MISC, Carbonize.CHARCOAL_SET.charcoalBlock);
        offerReversibleCompactingRecipes(exporter, RecipeCategory.MISC, Items.STICK, RecipeCategory.MISC, Carbonize.WOOD_STACK);

        List.of(Carbonize.CHARCOAL_SET, Carbonize.SOUL_CHARCOAL_SET).forEach(set -> set.getAll().forEach(block -> {
            if (block == CHARCOAL_SET.charcoalBlock) return;

            var burnTime = CharcoalPitMultiblock.SINGLE_BURN_TIME;
            if (block == CHARCOAL_SET.charcoalLog)
                burnTime = 250;
            else if (block == CHARCOAL_SET.charcoalStack)
                burnTime = 300;
            else if (block == CHARCOAL_SET.charcoalSlab)
                burnTime = 100;
            else if (block != CHARCOAL_SET.charcoalPlanks)
                burnTime = 150;

            var blockId = Registries.BLOCK.getId(block);
            var blockIdStr = blockId.getPath().replace(set.type, "");
            final String s = blockIdStr.charAt(blockIdStr.length() - 1) != 's' ? "s" : "";
            String inputPath = switch (blockIdStr) {
                case "charcoal_stairs", "charcoal_slab", "charcoal_stack" -> blockIdStr.replace("charcoal", "wooden") + s;
                default -> blockIdStr.replace("charcoal", "") + s;
            };

            var medium = CharcoalPitInit.CHARRING_WOOD;
            if (blockIdStr.contains("stack"))
                medium = CharcoalPitInit.CHARRING_STACK;

            FireType fireType = FireType.DEFAULT_FIRE_TYPE;
            if (set == SOUL_CHARCOAL_SET)
                fireType = FireType.SOUL_FIRE_TYPE;

            exporter.accept(new BurnRecipe(blockId, fireType, burnTime, TagKey.of(Registries.BLOCK.getKey(), new Identifier(inputPath.replace("_", ""))), medium, block).asJsonProvider());
        }));
    }
}
