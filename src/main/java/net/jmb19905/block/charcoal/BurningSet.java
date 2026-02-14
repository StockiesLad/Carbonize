package net.jmb19905.block.charcoal;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.jmb19905.api.FireType;
import net.jmb19905.block.ember.EmberBlock;
import net.jmb19905.block.StackBlock;
import net.jmb19905.block.charring.CharringWoodBlock;
import net.minecraft.block.*;
import net.minecraft.block.enums.Instrument;
import net.minecraft.sound.BlockSoundGroup;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

import static net.jmb19905.util.BlockHelper.registerBlockAndItem;

public class BurningSet {
    private static final Queue<Runnable> TASKS = new ArrayDeque<>();
    private static final List<BurningSet> ALL_SETS = new ArrayList<>();
    private final List<Block> allBlocks = new ArrayList<>();

    public final String type;
    private final String fireTypeSerialId;

    public final Block
            charringWood,

            emberStack,
            emberLog,
            emberPlanks,
            emberStairs,
            emberSlab,
            emberFence,
            emberFenceGate,

            sootStack,
            sootLog,
            sootPlanks,
            sootStairs,
            sootSlab,
            sootFence,
            sootFenceGate,


            charcoalBlock,
            charcoalStack,
            charcoalLog,
            charcoalPlanks,
            charcoalStairs,
            charcoalSlab,
            charcoalFence,
            charcoalFenceGate;

    //TODO: add impl for null Blocks
    public BurningSet(String fireTypeSerialId) {
        ALL_SETS.add(this);
        this.fireTypeSerialId = fireTypeSerialId;
        var luminanceFunc = new CopyLuminance(fireTypeSerialId);
        
        charringWood = register("charring_wood", new CharringWoodBlock(FabricBlockSettings.create().nonOpaque().luminance(luminanceFunc).sounds(BlockSoundGroup.WOOD).dropsNothing()));

        emberStack = null;
        emberLog = null;
        emberPlanks = register("ember_planks", new EmberBlock(AbstractBlock.Settings.create().luminance(luminanceFunc).burnable().sounds(BlockSoundGroup.WOOD), this::getFireType));
        emberStairs = null;
        emberSlab = null;
        emberFence = null;
        emberFenceGate = null;

        sootLog = register("soot_log",  new PillarBlock(FabricBlockSettings.create().mapColor(state -> MapColor.BLACK).instrument(Instrument.BASS).strength(1.0f).sounds(BlockSoundGroup.WOOD).burnable()));
        sootPlanks = register("soot_planks", new Block(FabricBlockSettings.copy(sootLog).strength(1.0F, 1.5F)));
        sootStack = register( "soot_stack", new StackBlock(FabricBlockSettings.copy(sootPlanks).nonOpaque()));
        sootStairs = register("soot_stairs", new FlammableFallingStairsBlock(sootPlanks.getDefaultState(), FabricBlockSettings.copy(sootPlanks)));
        sootSlab = register( "soot_slab", new SlabBlock(FabricBlockSettings.copy(sootPlanks)));
        sootFence = register("soot_fence", new FenceBlock(FabricBlockSettings.copy(sootPlanks)));
        sootFenceGate = register("soot_fence_gate", new FenceGateBlock(FabricBlockSettings.copy(sootPlanks), FlammableFaller.CHARRED_WOOD_TYPE));

        charcoalBlock = register("charcoal_block", new Block(FabricBlockSettings.copy(Blocks.COAL_BLOCK)));
        charcoalLog = register("charcoal_log", new FlammableFallingPillarBlock(FabricBlockSettings.copy(sootLog)));
        charcoalPlanks = register("charcoal_planks", new FlammableFallingBlock(FabricBlockSettings.copy(sootPlanks)));
        charcoalStack = register( "charcoal_stack", new FlammableFallingStackBlock(FabricBlockSettings.copy(sootPlanks).nonOpaque()));
        charcoalStairs = register("charcoal_stairs", new FlammableFallingStairsBlock(sootPlanks.getDefaultState(), FabricBlockSettings.copy(sootPlanks)));
        charcoalSlab = register( "charcoal_slab", new FlammableFallingSlabBlock(FabricBlockSettings.copy(sootPlanks)));
        charcoalFence = register("charcoal_fence", new FlammableFallingFenceBlock(FabricBlockSettings.copy(sootPlanks)));
        charcoalFenceGate = register("charcoal_fence_gate", new FlammableFallingFenceGateBlock(FabricBlockSettings.copy(sootPlanks)));

        var serialIdSplit = fireTypeSerialId.split("_");
        this.type = fireTypeSerialId.replace(serialIdSplit[serialIdSplit.length - 1], "")
                .replace("default", "");
        
        FuelRegistry.INSTANCE.add(charcoalBlock, 16000);
        FuelRegistry.INSTANCE.add(charcoalStack, 1600 * 5);
        FuelRegistry.INSTANCE.add(charcoalLog, 1600 * 4);
        FuelRegistry.INSTANCE.add(charcoalPlanks, 1600 * 4);
        FuelRegistry.INSTANCE.add(charcoalStairs, 1600 * 3);
        FuelRegistry.INSTANCE.add(charcoalFence, 1600 * 4);
        FuelRegistry.INSTANCE.add(charcoalFenceGate, 1600 * 4);
        FuelRegistry.INSTANCE.add(charcoalSlab, 1600 * 2);
    }
    
    public FireType getFireType() {
        return FireType.find(fireTypeSerialId).orElseThrow();
    }

    public List<Block> getAllBlocks() {
        return allBlocks;
    }

    public static void iterateBlocks(BiConsumer<BurningSet, Block> consumer) {
        ALL_SETS.forEach(set -> set.getAllBlocks().forEach(block -> consumer.accept(set, block)));
    }

    public static void iterateSets(Consumer<BurningSet> consumer) {
        ALL_SETS.forEach(consumer);
    }

    private Block register(String name, Block block) {
        allBlocks.add(block);
        TASKS.add(() -> {
            registerBlockAndItem((type.isEmpty() ? "" : (type + "_")) + name, block);
            if (name.contains("charcoal"))
                getFireType().ifCapability(capability ->
                        capability.registerFlammable(block, 15, 30));
        });
        return block;
    }

    public static void init() {
        while (!BurningSet.TASKS.isEmpty())
            Objects.requireNonNull(BurningSet.TASKS.poll()).run();
    }
    
    private static class CopyLuminance implements ToIntFunction<BlockState> {
        private Object data;

        public CopyLuminance(String fireTypeSerialId) {
            this.data = fireTypeSerialId;
        }

        @Override
        public int applyAsInt(BlockState state) {
            if (data instanceof String serialId)
                data = FireType.find(serialId).orElseThrow().asBlock().getDefaultState().getLuminance();
            return (int) data;
        }
    }
}
