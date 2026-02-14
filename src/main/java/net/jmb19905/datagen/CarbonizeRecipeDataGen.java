package net.jmb19905.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.jmb19905.api.FireType;
import net.jmb19905.block.charcoal.BurningSet;
import net.jmb19905.core.CarbonizeCommon;
import net.jmb19905.core.CarbonizeConstants;
import net.jmb19905.multiblock.CharcoalPitMultiblock;
import net.jmb19905.recipe.BurnRecipe;
import net.jmb19905.recipe.TagOrBlockPredicate;
import net.minecraft.block.Block;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class CarbonizeRecipeDataGen extends FabricRecipeProvider {

    public CarbonizeRecipeDataGen(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(Consumer<RecipeJsonProvider> exporter) {
        offerReversibleCompactingRecipes(exporter, RecipeCategory.MISC, Items.CHARCOAL, RecipeCategory.MISC, CarbonizeCommon.CHARCOAL_SET.charcoalBlock);
        offerReversibleCompactingRecipes(exporter, RecipeCategory.MISC, Items.STICK, RecipeCategory.MISC, CarbonizeCommon.WOOD_STACK);

        /* Carbonize Recipe (Huge) */ {
            Map<String, List<Block>> woodTypes = new HashMap<>();

            BurningSet.iterateBlocks(((set, block) -> {
                var id = Registries.BLOCK.getId(block);
                var name = id.getPath();

                List.of("stacks", "logs", "planks", "stairs", "slabs", "fences", "fence_gates").forEach(woodType -> {
                    var fullType = set.fireType + "_" + woodType;
                    woodTypes.putIfAbsent(fullType, new ArrayList<>());
                    var array = woodTypes.get(fullType);
                    if (name.contains(woodType))
                        array.add(block);
                });
            }));

            woodTypes.forEach((woodType, blocks) -> {
                var burnTime = CharcoalPitMultiblock.SINGLE_BURN_TIME;
                if (woodType.contains("log"))
                    burnTime = 300;
                else if (woodType.contains("stack"))
                    burnTime = 400;
                else if (woodType.contains("stairs"))
                    burnTime = 100;
                else if (woodType.contains("slab"))
                    burnTime = 150;

                Map<String, Block> burningStage = new HashMap<>();
                blocks.forEach(block -> {
                    var stringId = Registries.BLOCK.getId(block).getPath();
                    List.of("charcoal", "ember", "soot").forEach(type -> {
                        if (stringId.contains(type))
                            burningStage.put(type, block);
                    });
                });

                var charcoalVariant = Registries.BLOCK.getId(burningStage.get("charcoal"));

                var woodBlockType = charcoalVariant.getPath().replace("charcoal_", "");

                Map<String, TagKey<Block>> type2Tag = new HashMap<>();

                type2Tag.put("stacks", CarbonizeCommon.WOODEN_STACKS);
                type2Tag.put("logs", BlockTags.LOGS);
                type2Tag.put("planks", BlockTags.PLANKS);
                type2Tag.put("stairs", BlockTags.WOODEN_STAIRS);
                type2Tag.put("slabs", BlockTags.WOODEN_SLABS);
                type2Tag.put("fences", BlockTags.WOODEN_FENCES);
                type2Tag.put("fence_gates", BlockTags.FENCE_GATES);

                var woodTypeSplit = woodType.split("_");
                var fireType = woodTypeSplit[0];

                exporter.accept(new BurnRecipe(
                        new Identifier(CarbonizeConstants.MOD_ID, charcoalVariant.getPath()),
                        FireType.find(fireType).orElseThrow(),
                        burnTime,
                        new TagOrBlockPredicate("#" + type2Tag.get(woodBlockType).id()),
                        burningStage.get("ember"),
                        burningStage.get("soot"),
                        burningStage.get("charcoal")
                ).asJsonProvider());
            });
        }
    }
}
