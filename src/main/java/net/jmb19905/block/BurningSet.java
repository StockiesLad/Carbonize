package net.jmb19905.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.jmb19905.api.FireType;
import net.jmb19905.block.charcoal.*;
import net.jmb19905.block.charring.CharringWoodBlock;
import net.jmb19905.block.ember.*;
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
    private final FireType fireType;

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

    //TODO: make soot release more smoke
    public BurningSet(FireType fireType) {
        this.fireType = fireType;
        ALL_SETS.add(this);
        ToIntFunction<BlockState> luminanceFunc = state -> fireType.asFireBlock().getDefaultState().getLuminance();
        
        charringWood = register("charring_wood", new CharringWoodBlock(FabricBlockSettings.create().nonOpaque().luminance(luminanceFunc).sounds(BlockSoundGroup.WOOD).dropsNothing()));

        emberLog = register("ember_log", new EmberPillarBlock(FabricBlockSettings.create().luminance(luminanceFunc).mapColor(state -> MapColor.ORANGE).instrument(Instrument.BASS).strength(2.0f).sounds(BlockSoundGroup.WOOD).burnable(), this::getFireType));
        emberPlanks = register("ember_planks", new EmberBlock(FabricBlockSettings.copy(emberLog).strength(2F, 3F), fireType));
        emberStack = register("ember_stack", new EmberStackBlock(FabricBlockSettings.copy(emberLog), fireType));
        emberStairs = register("ember_stairs", new EmberStairsBlock(emberPlanks.getDefaultState(), FabricBlockSettings.copy(emberPlanks), fireType));
        emberSlab = register("ember_slab", new EmberSlabBlock(FabricBlockSettings.copy(emberPlanks), fireType));
        emberFence = register("ember_fence", new EmberFenceBlock(FabricBlockSettings.copy(emberPlanks), fireType));
        emberFenceGate = register("ember_fence_gate", new EmberFenceGateBlock(FabricBlockSettings.copy(emberPlanks), FlammableFaller.EMBER_WOOD_TYPE, fireType));

        sootLog = register("soot_log",  new PillarBlock(FabricBlockSettings.create().mapColor(state -> MapColor.BLACK).instrument(Instrument.BASS).strength(1.0f).sounds(BlockSoundGroup.WOOD).burnable()));
        sootPlanks = register("soot_planks", new Block(FabricBlockSettings.copy(sootLog).strength(1.0F, 1.5F)));
        sootStack = register( "soot_stack", new StackBlock(FabricBlockSettings.copy(sootLog).nonOpaque()));
        sootStairs = register("soot_stairs", new FlammableFallingStairsBlock(sootPlanks.getDefaultState(), FabricBlockSettings.copy(sootPlanks)));
        sootSlab = register( "soot_slab", new SlabBlock(FabricBlockSettings.copy(sootPlanks)));
        sootFence = register("soot_fence", new FenceBlock(FabricBlockSettings.copy(sootPlanks)));
        sootFenceGate = register("soot_fence_gate", new FenceGateBlock(FabricBlockSettings.copy(sootPlanks), FlammableFaller.BURNT_WOOD_TYPE));

        charcoalBlock = register("charcoal_block", new Block(FabricBlockSettings.copy(Blocks.COAL_BLOCK)));
        charcoalLog = register("charcoal_log", new FlammableFallingPillarBlock(FabricBlockSettings.copy(sootLog)));
        charcoalPlanks = register("charcoal_planks", new FlammableFallingBlock(FabricBlockSettings.copy(sootPlanks)));
        charcoalStack = register( "charcoal_stack", new FlammableFallingStackBlock(FabricBlockSettings.copy(sootLog).nonOpaque()));
        charcoalStairs = register("charcoal_stairs", new FlammableFallingStairsBlock(sootPlanks.getDefaultState(), FabricBlockSettings.copy(sootPlanks)));
        charcoalSlab = register( "charcoal_slab", new FlammableFallingSlabBlock(FabricBlockSettings.copy(sootPlanks)));
        charcoalFence = register("charcoal_fence", new FlammableFallingFenceBlock(FabricBlockSettings.copy(sootPlanks)));
        charcoalFenceGate = register("charcoal_fence_gate", new FlammableFallingFenceGateBlock(FabricBlockSettings.copy(sootPlanks)));

        this.type = fireType.getSerialId().replace("_fire", "").replace("default", "");
        
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
        return fireType;
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

    //TODO: improve flammability registration
    private Block register(String name, Block block) {
        allBlocks.add(block);
        TASKS.add(() -> {
            registerBlockAndItem((type.isEmpty() ? "" : (type + "_")) + name, block);
            if (name.contains("charcoal") || name.contains("soot"))
                getFireType().ifCapability(capability ->
                        capability.registerFlammable(block, 15, 30));
        });
        return block;
    }

    public static void init() {
        while (!BurningSet.TASKS.isEmpty())
            Objects.requireNonNull(BurningSet.TASKS.poll()).run();
    }
}
