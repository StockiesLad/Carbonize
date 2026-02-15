package net.jmb19905.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.jmb19905.api.FireType;
import net.jmb19905.block.BurningSet;
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

import java.util.*;
import java.util.function.Consumer;

public class CarbonizeRecipeDataGen extends FabricRecipeProvider {

    public CarbonizeRecipeDataGen(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(Consumer<RecipeJsonProvider> exporter) {
        offerReversibleCompactingRecipes(exporter, RecipeCategory.MISC, Items.CHARCOAL, RecipeCategory.MISC, CarbonizeCommon.CHARCOAL_SET.charcoalBlock);
        offerReversibleCompactingRecipes(exporter, RecipeCategory.MISC, Items.STICK, RecipeCategory.MISC, CarbonizeCommon.WOOD_STACK);

        //VERY BROKEN
        {

            Map<String, List<Block>> woodTypes = new HashMap<>();

            BurningSet.iterateBlocks(((set, block) -> {
                var path = Registries.BLOCK.getId(block).getPath();
                path = set.type.isEmpty() ? path : path.replace(set.type + "_", "");
                var preNameSplit = path.split("_");
                var name = path.replace(preNameSplit[0] + "_", "");
                var nameSplit = name.split("_");

                List.of("stack", "log", "plank", "stair", "slab", "fence", "fence_gate").forEach(woodType -> {
                    var fullType = set.getFireType().getSerialId() + "_" + woodType;
                    woodTypes.putIfAbsent(fullType, new ArrayList<>());
                    var array = woodTypes.get(fullType);

                    var woodTypeSplit = woodType.split("_");

                    if (nameSplit.length != woodTypeSplit.length)
                        return;

                    var matches = true;
                    for (int i = 0; i < nameSplit.length; i++) {
                        if (!nameSplit[i].contains(woodTypeSplit[i])) {
                            matches = false;
                            break;
                        }
                    }

                    if (matches) array.add(block);
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


                Map<String, TagKey<Block>> type2Tag = new HashMap<>();

                type2Tag.put("stack", CarbonizeCommon.WOODEN_STACKS);
                type2Tag.put("log", BlockTags.LOGS);
                type2Tag.put("plank", BlockTags.PLANKS);
                type2Tag.put("stair", BlockTags.WOODEN_STAIRS);
                type2Tag.put("slab", BlockTags.WOODEN_SLABS);
                type2Tag.put("fence", BlockTags.WOODEN_FENCES);
                type2Tag.put("fence_gate", BlockTags.FENCE_GATES);

                var woodTypeSplit = woodType.split("_");
                var fireType = woodTypeSplit[0] + "_" + woodTypeSplit[1];
                var woodBlockType = woodType.replace(fireType + "_", "");

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
