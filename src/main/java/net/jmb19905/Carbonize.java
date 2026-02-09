package net.jmb19905;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.jmb19905.block.AshBlock;
import net.jmb19905.block.StackBlock;
import net.jmb19905.charcoal_pit.CharcoalPitInit;
import net.jmb19905.charcoal_pit.FireType;
import net.jmb19905.config.CarbonizeConfig;
import net.jmb19905.core.CarbonizeItemGroup;
import net.jmb19905.core.CharcoalSet;
import net.jmb19905.recipe.BurnRecipe;
import net.jmb19905.recipe.BurnRecipeSerializer;
import net.minecraft.block.*;
import net.minecraft.block.enums.Instrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.Item;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Carbonize implements ModInitializer {
	public static final String MOD_ID = "carbonize";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final CarbonizeConfig CONFIG = CarbonizeConfig.createAndLoad();

	public static final CharcoalSet CHARCOAL_SET = new CharcoalSet();
	public static final CharcoalSet SOUL_CHARCOAL_SET = new CharcoalSet("soul", FireType.SOUL_FIRE_TYPE);

	public static final Block WOOD_STACK = new StackBlock(FabricBlockSettings.create()
			.instrument(Instrument.BASS)
			.strength(2.0f)
			.sounds(BlockSoundGroup.WOOD)
			.nonOpaque()
			.burnable());

	public static final Block ASH_LAYER = new AshBlock(FabricBlockSettings.create()
			.mapColor(MapColor.GRAY)
			.sounds(BlockSoundGroup.SAND)
			.replaceable()
			.notSolid()
			.ticksRandomly()
			.strength(0.1f)
			.blockVision((state, world, pos) -> state.get(SnowBlock.LAYERS) >= 8)
			.pistonBehavior(PistonBehavior.DESTROY)
			.ticksRandomly());

	public static final Block ASH_BLOCK = new FallingBlock(FabricBlockSettings.create()
			.mapColor(MapColor.GRAY)
			.sounds(BlockSoundGroup.SAND));

	public static final Item ASH = new BoneMealItem(new FabricItemSettings());

	public static final RecipeType<BurnRecipe> BURN_RECIPE_TYPE = registerRecipeType(BurnRecipeSerializer.ID);

	public static final TagKey<Block> WOODEN_STACKS = TagKey.of(RegistryKeys.BLOCK, new Identifier(MOD_ID, "wooden_stacks"));
	public static final TagKey<Block> CHARRING_BLOCKS = TagKey.of(RegistryKeys.BLOCK, new Identifier(MOD_ID, "charring_blocks"));
	public static final TagKey<Block> CHARCOAL_BLOCKS = TagKey.of(RegistryKeys.BLOCK, new Identifier(MOD_ID, "charcoal_blocks"));
	public static final TagKey<Block> CHARCOAL_PILE_VALID_FUEL = TagKey.of(RegistryKeys.BLOCK, new Identifier(MOD_ID, "charcoal_pile_valid_fuel"));
	public static final TagKey<Item> DAMAGE_IGNITERS = TagKey.of(RegistryKeys.ITEM, new Identifier(MOD_ID, "damage_igniters"));
	public static final TagKey<Item> CONSUME_IGNITERS = TagKey.of(RegistryKeys.ITEM, new Identifier(MOD_ID, "consume_igniters"));
	public static final TagKey<Item> IGNITERS = TagKey.of(RegistryKeys.ITEM, new Identifier(MOD_ID, "igniters"));

    @Override
	public void onInitialize() {
		CharcoalPitInit.init();
		while (!CharcoalSet.TASKS.isEmpty())
			Objects.requireNonNull(CharcoalSet.TASKS.poll()).run();
		Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "wood_stack"), WOOD_STACK);
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "wood_stack"), new BlockItem(WOOD_STACK, new FabricItemSettings()));

		Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "ash_layer"), ASH_LAYER);
		Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "ash_block"), ASH_BLOCK);

		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "ash_layer"), new BlockItem(ASH_LAYER, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "ash_block"), new BlockItem(ASH_BLOCK, new FabricItemSettings()));

		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "ash"), ASH);

		Registry.register(Registries.RECIPE_SERIALIZER, BurnRecipeSerializer.ID, BurnRecipeSerializer.INSTANCE);

		FlammableBlockRegistry.getDefaultInstance().add(WOOD_STACK, 15, 30);


		if (CONFIG.moreBurnableBlocks()) {
			if (CONFIG.burnableContainers()) {
				FlammableBlockRegistry.getDefaultInstance().add(Blocks.CHEST, 5, 5);
				FlammableBlockRegistry.getDefaultInstance().add(Blocks.TRAPPED_CHEST, 5, 5);
				FlammableBlockRegistry.getDefaultInstance().add(Blocks.JUKEBOX, 5, 5);
				FlammableBlockRegistry.getDefaultInstance().add(Blocks.BARREL, 5, 5);
			}

			FlammableBlockRegistry.getDefaultInstance().add(Blocks.NOTE_BLOCK, 5, 5);
			FlammableBlockRegistry.getDefaultInstance().add(Blocks.CRAFTING_TABLE, 5, 5);
			FlammableBlockRegistry.getDefaultInstance().add(Blocks.LADDER, 15, 60);
			FlammableBlockRegistry.getDefaultInstance().add(Blocks.CARTOGRAPHY_TABLE, 5, 5);
			FlammableBlockRegistry.getDefaultInstance().add(Blocks.FLETCHING_TABLE, 5, 5);
			FlammableBlockRegistry.getDefaultInstance().add(Blocks.SMITHING_TABLE, 5, 5);
			FlammableBlockRegistry.getDefaultInstance().add(Blocks.LOOM, 5, 5);
			FlammableBlockRegistry.getDefaultInstance().add(BlockTags.BANNERS, 5, 5);
			FlammableBlockRegistry.getDefaultInstance().add(BlockTags.BEDS, 5, 5);
			FlammableBlockRegistry.getDefaultInstance().add(BlockTags.ALL_SIGNS, 5, 5);
		}

		CarbonizeItemGroup.init();
	}

	@SuppressWarnings("SameParameterValue")
	private static <T extends Recipe<?>> RecipeType<T> registerRecipeType(final Identifier id) {
		return Registry.register(Registries.RECIPE_TYPE, id, new RecipeType<T>() {
			public String toString() {
				return id.getPath();
			}
		});
	}

}