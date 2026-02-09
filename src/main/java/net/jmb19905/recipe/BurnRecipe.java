package net.jmb19905.recipe;

import com.google.gson.JsonObject;
import net.jmb19905.Carbonize;
import net.jmb19905.charcoal_pit.CharcoalPitInit;
import net.jmb19905.charcoal_pit.FireType;
import net.jmb19905.charcoal_pit.multiblock.CharcoalPitMultiblock;
import net.minecraft.block.Block;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BurnRecipe implements Recipe<SimpleInventory> {
    private final Identifier id;
    private final FireType fireType;
    private final int burnTime;
    private final TagKey<Block> input;
    private final Block medium;
    private final Block result;

    public BurnRecipe(Identifier id, FireType fireType, int burnTime, TagKey<Block> input, Block medium, Block result) {
        this.id = id;
        this.fireType = fireType;
        this.burnTime = burnTime;
        this.input = input;
        this.medium = medium;
        this.result = result;
    }

    public BurnRecipe(Identifier id, TagKey<Block> input, Block medium, Block result) {
        this(id, FireType.DEFAULT_FIRE_TYPE, CharcoalPitMultiblock.SINGLE_BURN_TIME, input, medium, result);
    }

    public BurnRecipe(Identifier id, TagKey<Block> input, Block result) {
        this(id, FireType.DEFAULT_FIRE_TYPE, CharcoalPitMultiblock.SINGLE_BURN_TIME, input, CharcoalPitInit.CHARRING_WOOD, result);
    }

    public FireType fireType() {
        return fireType;
    }

    public int burnTime () {
        return burnTime;
    }

    public TagKey<Block> input() {
        return input;
    }

    public Block medium() {
        return medium;
    }

    public Block result() {
        return result;
    }

    public RecipeJsonProvider asJsonProvider() {
        return new RecipeJsonProvider() {
            @Override
            public void serialize(JsonObject json) {
                assert getSerializer() != null;
                ((BurnRecipeSerializer)getSerializer()).write(json, BurnRecipe.this);
            }

            @Override
            public Identifier getRecipeId() {
                return BurnRecipe.this.getId();
            }

            @Override
            public RecipeSerializer<?> getSerializer() {
                return BurnRecipe.this.getSerializer();
            }

            @Override
            public @Nullable JsonObject toAdvancementJson() {
                return null;
            }

            @Override
            public @Nullable Identifier getAdvancementId() {
                return null;
            }
        };
    }

    @Override
    public boolean matches(SimpleInventory inventory, World world) {
        return false;
    }

    @Override
    public ItemStack craft(SimpleInventory inventory, DynamicRegistryManager registryManager) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return ItemStack.EMPTY;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public BurnRecipeSerializer getSerializer() {
        return BurnRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Carbonize.BURN_RECIPE_TYPE;
    }

    @Override
    public String toString() {
        return  "BurnRecipe[" +
                    "id=" + id + ", " +
                    "fireType=" + fireType + ", " +
                    "input=" + input + ", " +
                    "medium=" + medium + ", " +
                    "result=" + result +
                "]@" + hashCode();
    }
}
