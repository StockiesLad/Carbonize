package net.jmb19905.core;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.jmb19905.block.*;
import net.jmb19905.charcoal_pit.FireType;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.Instrument;
import net.minecraft.sound.BlockSoundGroup;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.jmb19905.util.BlockHelper.registerBlockAndItem;

public class CharcoalSet {
    public static final Queue<Runnable> TASKS = new ArrayDeque<>();
    private static final List<CharcoalSet> ALL_SETS = new ArrayList<>();
    private final List<Block> allBlocks = new ArrayList<>();

    public final String type;
    private final FireType fireType;
    public final Block charcoalBlock;
    public final Block charcoalLog;
    public final Block charcoalPlanks;
    public final Block charcoalStack;
    public final Block charcoalStairs;
    public final Block charcoalSlab;
    public final Block charcoalFence;
    public final Block charcoalFenceGate;

    public CharcoalSet(String type, FireType fireType) {
        ALL_SETS.add(this);
        this.type = type;
        this.fireType = fireType;
        charcoalBlock = register("charcoal_block", new Block(FabricBlockSettings.copy(Blocks.COAL_BLOCK)));
        charcoalLog = register("charcoal_log", new FlammableFallingPillarBlock(FabricBlockSettings.create().mapColor(state -> MapColor.BLACK).instrument(Instrument.BASS).strength(2.0f).sounds(BlockSoundGroup.WOOD).burnable()));
        charcoalPlanks = register("charcoal_planks", new FlammableFallingBlock(FabricBlockSettings.create().mapColor(state -> MapColor.BLACK).instrument(Instrument.BASS).strength(2.0f).sounds(BlockSoundGroup.WOOD).burnable()));
        charcoalStack = register( "charcoal_stack", new FlammableFallingStackBlock(FabricBlockSettings.copy(charcoalPlanks).nonOpaque()));
        charcoalStairs = register("charcoal_stairs", new FlammableFallingStairsBlock(Blocks.OAK_PLANKS.getDefaultState(), FabricBlockSettings.copy(charcoalPlanks)));
        charcoalFence = register("charcoal_fence", new FlammableFallingFenceBlock(FabricBlockSettings.copy(charcoalPlanks)));
        charcoalFenceGate = register("charcoal_fence_gate", new FlammableFallingFenceGateBlock(FabricBlockSettings.copy(charcoalPlanks)));
        charcoalSlab = register( "charcoal_slab", new FlammableFallingSlabBlock(FabricBlockSettings.copy(charcoalPlanks)));

        FuelRegistry.INSTANCE.add(charcoalBlock, 16000);
        FuelRegistry.INSTANCE.add(charcoalStack, 1600 * 5);
        FuelRegistry.INSTANCE.add(charcoalLog, 1600 * 4);
        FuelRegistry.INSTANCE.add(charcoalPlanks, 1600 * 4);
        FuelRegistry.INSTANCE.add(charcoalStairs, 1600 * 3);
        FuelRegistry.INSTANCE.add(charcoalFence, 1600 * 4);
        FuelRegistry.INSTANCE.add(charcoalFenceGate, 1600 * 4);
        FuelRegistry.INSTANCE.add(charcoalSlab, 1600 * 2);
    }

    public CharcoalSet() {
        this("", FireType.DEFAULT_FIRE_TYPE);
    }

    public List<Block> getAll() {
        return allBlocks;
    }

    private Block register(String name, Block block) {
        TASKS.add(() -> {
            allBlocks.add(block);
            registerBlockAndItem((type.isEmpty() ? "" : (type + "_")) + name, block);
            fireType.carbonize$registerFlammableBlock(block, 15, 30);
        });
        return block;
    }

    public static void iterateBlocks(BiConsumer<CharcoalSet, Block> consumer) {
        ALL_SETS.forEach(set -> set.getAll().forEach(block -> consumer.accept(set, block)));
    }

    public static void iterateSets(Consumer<CharcoalSet> consumer) {
        ALL_SETS.forEach(consumer);
    }
}
