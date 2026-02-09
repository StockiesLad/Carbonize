package net.jmb19905.util;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.jmb19905.block.FireView;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static net.jmb19905.Carbonize.MOD_ID;

/**
 * The following methods copy one state to another. This had to be done as the provided method for this in {@link BlockState} just doesn't work properly for some reason...
 */
public class BlockHelper {
    public static <T extends Comparable<T>> BlockState transferStateProperty(BlockState from, BlockState to, Property<T> property) {
        return to.withIfExists(property, from.get(property));
    }

    public static BlockState transferState(BlockState parent, BlockState child) {
        var stateHolder = new ObjectHolder<>(parent);
        child.getProperties().forEach(value -> stateHolder.updateValue(oldState -> transferStateProperty(child, oldState, value)));
        return stateHolder.getValue();
    }

    public static boolean isFlammable(BlockState state) {
        FireView access = ((FireView) Blocks.FIRE);
        return access.carbonize$isFlammable(state) || access.carbonize$getSpreadChance(state) > 0 || state.isBurnable();
    }

    public static boolean isNonFlammableFullCube(World world, BlockPos pos, BlockState state) {
        var isAir = state.isAir();
        var isCube = state.isFullCube(world, pos);
        var isFlammable = isFlammable(state);
        return !isAir && isCube && !isFlammable;
    }

    public static Block registerBlockAndItem(String name, Block block) {
        var identifier = new Identifier(MOD_ID, name);
        Registry.register(Registries.BLOCK, identifier, block);
        Items.register(new BlockItem(block, new FabricItemSettings()));
        return block;
    }
}
