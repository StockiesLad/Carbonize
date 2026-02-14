package net.jmb19905.recipe;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.function.Predicate;

public class TagOrBlockPredicate implements Predicate<BlockState> {
    public final TagKey<Block> tag;
    public final Block block;

    public TagOrBlockPredicate(String rawInput) {
        if (rawInput.contains("#")) {
            tag = TagKey.of(RegistryKeys.BLOCK, new Identifier(rawInput.replace("#", "")));
            block = null;
        }
        else {
            tag = null;
            block = Registries.BLOCK.get(new Identifier(rawInput));
        }
    }

    public String serialize() {
        if (tag != null)
            return "#" + tag.id().toString();
        else return Registries.BLOCK.getId(block).toString();
    }

    @Override
    public boolean test(BlockState state) {
        return (tag != null && state.isIn(tag)) || (block != null && state.isOf(block));
    }
}
