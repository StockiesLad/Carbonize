package net.jmb19905.recipe;

import com.google.gson.JsonObject;
import net.jmb19905.api.FireType;
import net.jmb19905.core.CarbonizeCommon;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class BurnRecipe implements Recipe<SimpleInventory> {
    private final Identifier id;
    private final FireType fireType;
    private final int burnTime;
    private final TagOrBlockPredicate input;
    private final Block burnBlock;
    private final Block soot;
    private final Block charcoal;

    public BurnRecipe(
            Identifier id,
            FireType fireType,
            int burnTime,
            TagOrBlockPredicate input,
            Block burnBlock,
            Block failResult,
            Block successResult
    ) {
        this.id = id;
        this.fireType = fireType;
        this.burnTime = burnTime;
        this.input = input;
        this.burnBlock = burnBlock;
        this.soot = failResult;
        this.charcoal = successResult;
    }

    public int burnTime() {
        return burnTime;
    }

    public FireType fireType() {
        return fireType;
    }

    public boolean isInput(BlockState state, FireType fireType) {
        return input.test(state) && this.fireType.equals(fireType) && fireType.isBlockFlammable(state);
    }

    public TagOrBlockPredicate input() {
        return input;
    }

    public Block burnBlock() {
        return burnBlock;
    }

    public Block failResult() {
        return soot;
    }

    public Block successResult() {
        return charcoal;
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
        return CarbonizeCommon.BURN_RECIPE_TYPE;
    }

    @Override
    public String toString() {
        return  "BurnRecipe[" +
                    "id=" + id + ", " +
                    "input=" + Arrays.toString(input.getClass().getDeclaredFields()) + ", " +
                    "medium=" + burnBlock + ", " +
                    "result=" + charcoal +
                "]@" + hashCode();
    }
}
