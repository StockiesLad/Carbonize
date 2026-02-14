package net.jmb19905.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.jmb19905.api.FireType;
import net.jmb19905.core.CarbonizeCommon;
import net.jmb19905.multiblock.CharcoalPitMultiblock;
import net.jmb19905.core.CarbonizeConstants;
import net.minecraft.block.Block;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class BurnRecipeSerializer implements RecipeSerializer<BurnRecipe> {

    public static final BurnRecipeSerializer INSTANCE = new BurnRecipeSerializer();
    public static final Identifier ID = new Identifier(CarbonizeConstants.MOD_ID, "burn");

    @Override
    public BurnRecipe read(Identifier id, JsonObject json) {
        String rawBurnTime = json.has("burnTime") ? json.get("burnTime").getAsString() : null;
        String rawFireType = json.has("fireType") ? json.get("fireType").getAsString() : null;
        String rawInput = json.get("input").getAsString();
        String rawBurnBlock = json.has("burnBlock") ? json.get("burnBlock").getAsString() : null;
        String rawSmoldering = json.get("failResult").getAsString();
        String rawOutput = json.get("successResult").getAsString();

        int burnTime = rawBurnTime != null ? Integer.parseInt(rawBurnTime) : CharcoalPitMultiblock.SINGLE_BURN_TIME;
        FireType fireType = FireType.find(rawFireType).orElse(FireType.DEFAULT_FIRE_TYPE);
        TagOrBlockPredicate input = new TagOrBlockPredicate(rawInput);
        Block burnBlock = rawBurnBlock != null ? findBlock(rawBurnBlock): CarbonizeCommon.CHARCOAL_SET.charringWood;
        Block failResult = findBlock(rawSmoldering);
        Block successResult = findBlock(rawOutput);

        return new BurnRecipe(id, fireType, burnTime, input, burnBlock, failResult, successResult);
    }

    public void write(JsonObject json, BurnRecipe recipe) {
        json.addProperty("burnTime", recipe.burnTime());
        json.addProperty("fireType", recipe.fireType().getSerialId());
        json.addProperty("input", recipe.input().serialize());
        json.addProperty("burnBlock", Registries.BLOCK.getId(recipe.burnBlock()).toString());
        json.addProperty("failResult", Registries.BLOCK.getId(recipe.failResult()).toString());
        json.addProperty("successResult", Registries.BLOCK.getId(recipe.successResult()).toString());

    }

    @Override
    public BurnRecipe read(Identifier id, PacketByteBuf buf) {
        int burnTime = buf.readInt();
        String rawFireType = buf.readString();
        String rawInput = buf.readString();
        Identifier idBurnBlock = buf.readIdentifier();
        Identifier idFailResult = buf.readIdentifier();
        Identifier idSuccessResult = buf.readIdentifier();

        FireType fireType = FireType.find(rawFireType).orElse(FireType.DEFAULT_FIRE_TYPE);
        TagOrBlockPredicate input = new TagOrBlockPredicate(rawInput);
        Block burnBlock = Registries.BLOCK.get(idBurnBlock);
        Block failResult = Registries.BLOCK.get(idFailResult);
        Block successResult = Registries.BLOCK.get(idSuccessResult);

        return new BurnRecipe(id, fireType, burnTime, input, burnBlock, failResult, successResult);
    }

    @Override
    public void write(PacketByteBuf buf, BurnRecipe recipe) {
        buf.writeInt(recipe.burnTime());
        buf.writeString(recipe.fireType().getSerialId());
        buf.writeString(recipe.input().serialize());
        buf.writeIdentifier(Registries.BLOCK.getId(recipe.burnBlock()));
        buf.writeIdentifier(Registries.BLOCK.getId(recipe.failResult()));
        buf.writeIdentifier(Registries.BLOCK.getId(recipe.successResult()));
    }

    private static Block findBlock(String identifier) {
        return identifier == null ? null :
                Registries.BLOCK.getOrEmpty(new Identifier(identifier)).orElseThrow(() -> new JsonSyntaxException("No such Block: " + identifier));
    }
}
