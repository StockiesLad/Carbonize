package net.jmb19905.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.jmb19905.api.FireType;
import net.jmb19905.block.BurningSet;
import net.jmb19905.block.StackBlock;
import net.jmb19905.core.CarbonizeCommon;
import net.minecraft.block.*;
import net.minecraft.data.client.*;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

import java.util.*;

import static net.jmb19905.core.CarbonizeConstants.MOD_ID;
import static net.minecraft.data.client.BlockStateModelGenerator.buildBlockStateVariants;
import static net.minecraft.data.client.BlockStateModelGenerator.createSingletonBlockState;

public class CarbonizeModelDataGen extends FabricModelProvider {
    private static final Model STACK_MODEL = new Model(Optional.of(new Identifier(MOD_ID, "block/raw_stack" )), Optional.empty(), TextureKey.TEXTURE);
    private static Model getLayerModel(int layer) {
        return new Model(Optional.of(new Identifier(MOD_ID, "block/layer_height_" + layer)), Optional.empty(), TextureKey.TEXTURE, TextureKey.PARTICLE);
    }

    public CarbonizeModelDataGen(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        registerSnowLike(blockStateModelGenerator, CarbonizeCommon.ASH_BLOCK, CarbonizeCommon.ASH_LAYER);
        registerFire(blockStateModelGenerator, FireType.SOUL_FIRE_TYPE);
        registerStack(blockStateModelGenerator, CarbonizeCommon.WOOD_STACK, Blocks.SPRUCE_PLANKS);

        BurningSet.iterateSets(set -> {
            blockStateModelGenerator.registerSimpleCubeAll(set.charringWood);
            blockStateModelGenerator.registerSimpleCubeAll(set.charcoalBlock);

            Map<String, List<Block>> map = new HashMap<>();
            set.getAllBlocks().forEach(block -> {
                var blockId = Registries.BLOCK.getId(block).getPath();
                if (blockId.equals(Registries.BLOCK.getId(set.charcoalBlock).getPath()))
                    return;
                for (var type : List.of("charcoal", "soot", "ember")) {
                    if (blockId.contains(type)) {
                        map.putIfAbsent(type, new ArrayList<>());
                        map.get(type).add(block);
                        break;
                    }
                }
            });

            map.forEach((type, blocks) -> {
                var planks = blocks.stream()
                        .filter(block -> Registries.BLOCK.getId(block).getPath().contains("planks"))
                        .findFirst().orElseThrow();
                var pool = blockStateModelGenerator.registerCubeAllModelTexturePool(planks);
                blocks.forEach(block -> {
                    if (block instanceof FenceBlock) pool.fence(block);
                    else if (block instanceof FenceGateBlock) pool.fenceGate(block);
                    else if (block instanceof StairsBlock) pool.stairs(block);
                    else if (block instanceof SlabBlock) pool.slab(block);
                    else if (block instanceof PillarBlock)
                        blockStateModelGenerator.registerLog(block).log(block);
                    else if (block instanceof StackBlock)
                        registerStack(blockStateModelGenerator, block, planks);
                });
            });
        });
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(CarbonizeCommon.ASH, Models.GENERATED);
    }

    public void registerStack(BlockStateModelGenerator generator, Block stack, Block texture) {
        var model = STACK_MODEL.upload(stack, TextureMap.texture(texture), generator.modelCollector);
        generator.blockStateCollector.accept(VariantsBlockStateSupplier.create(stack,
                BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R0).put(VariantSettings.MODEL, model),
                BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R90).put(VariantSettings.MODEL, model),
                BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R180).put(VariantSettings.MODEL, model),
                BlockStateVariant.create().put(VariantSettings.Y, VariantSettings.Rotation.R270).put(VariantSettings.MODEL, model)
        ));
    }

    private void registerFire(BlockStateModelGenerator blockStateModelGenerator, FireType fireType) {
        var block = fireType.asFireBlock();
        When when = When.create().set(Properties.NORTH, false).set(Properties.EAST, false).set(Properties.SOUTH, false).set(Properties.WEST, false).set(Properties.UP, false);
        List<Identifier> list = blockStateModelGenerator.getFireFloorModels(block);
        List<Identifier> list2 = blockStateModelGenerator.getFireSideModels(block);
        List<Identifier> list3 = blockStateModelGenerator.getFireUpModels(block);
        blockStateModelGenerator.blockStateCollector.accept(MultipartBlockStateSupplier.create(block)
                .with(when, buildBlockStateVariants(list, (blockStateVariant) -> blockStateVariant))
                .with(When.anyOf(When.create().set(Properties.NORTH, true), when), buildBlockStateVariants(list2, (blockStateVariant) -> blockStateVariant))
                .with(When.anyOf(When.create().set(Properties.EAST, true), when), buildBlockStateVariants(list2, (blockStateVariant) -> blockStateVariant.put(VariantSettings.Y, VariantSettings.Rotation.R90)))
                .with(When.anyOf(When.create().set(Properties.SOUTH, true), when), buildBlockStateVariants(list2, (blockStateVariant) -> blockStateVariant.put(VariantSettings.Y, VariantSettings.Rotation.R180)))
                .with(When.anyOf(When.create().set(Properties.WEST, true), when), buildBlockStateVariants(list2, (blockStateVariant) -> blockStateVariant.put(VariantSettings.Y, VariantSettings.Rotation.R270)))
                .with(When.create().set(Properties.UP, true), buildBlockStateVariants(list3, (blockStateVariant) -> blockStateVariant)));
    }

    private void registerSnowLike(BlockStateModelGenerator modelCollector, Block fullCube, Block snowLike) {
        TextureMap textureMap = TextureMap.all(fullCube);
        Identifier identifier = Models.CUBE_ALL.upload(fullCube, textureMap, modelCollector.modelCollector);
        modelCollector.blockStateCollector.accept(VariantsBlockStateSupplier.create(snowLike).coordinate(BlockStateVariantMap.create(Properties.LAYERS).register((height) -> {
            BlockStateVariant stateVariant = BlockStateVariant.create();
            VariantSetting<Identifier> variantSetting = VariantSettings.MODEL;
            Identifier layerId;
            if (height < 8) {
                layerId = ModelIds.getBlockSubModelId(snowLike, "_height" + height * 2);
                getLayerModel(height * 2).upload(layerId, new TextureMap().put(TextureKey.TEXTURE, identifier).put(TextureKey.PARTICLE, identifier), modelCollector.modelCollector);
            } else {
                layerId = identifier;
            }

            return stateVariant.put(variantSetting, layerId);
        })));
        modelCollector.registerParentedItemModel(snowLike, ModelIds.getBlockSubModelId(snowLike, "_height2"));
        modelCollector.blockStateCollector.accept(createSingletonBlockState(fullCube, identifier));
    }
}
