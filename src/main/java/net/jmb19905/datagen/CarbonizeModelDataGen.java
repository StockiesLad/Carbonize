package net.jmb19905.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.jmb19905.api.FireType;
import net.jmb19905.block.BurningSet;
import net.jmb19905.block.StackBlock;
import net.jmb19905.block.ember.AbstractEmberBlock;
import net.jmb19905.block.ember.AbstractEmberBlock.Stage;
import net.jmb19905.core.CarbonizeCommon;
import net.jmb19905.util.IModel;
import net.jmb19905.util.ModelHelper;
import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.block.enums.StairShape;
import net.minecraft.data.client.*;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.*;
import java.util.function.Function;

import static net.jmb19905.block.ember.AbstractEmberBlock.Stage.BURNING;
import static net.jmb19905.core.CarbonizeConstants.MOD_ID;
import static net.minecraft.data.client.BlockStateModelGenerator.buildBlockStateVariants;
import static net.minecraft.data.client.BlockStateModelGenerator.createSingletonBlockState;
import static net.minecraft.data.client.BlockStateVariant.create;
import static net.minecraft.data.client.VariantSettings.*;
import static net.minecraft.util.math.Direction.*;

public class CarbonizeModelDataGen extends FabricModelProvider {
    private static final Model STACK_MODEL = new Model(Optional.of(new Identifier(MOD_ID, "block/raw_stack" )), Optional.empty(), TextureKey.TEXTURE);
    private static Model getLayerModel(int layer) {
        return new Model(Optional.of(new Identifier(MOD_ID, "block/layer_height_" + layer)), Optional.empty(), TextureKey.TEXTURE, TextureKey.PARTICLE);
    }

    public CarbonizeModelDataGen(FabricDataOutput output) {
        super(output);
    }

    //Smoldering Texture: Embers with -80 brightness and +80 contrast.
    //Soot and Charcoal texture with 32% opacity smoldering texture for with sooting and charring respectively
    @Override
    public void generateBlockStateModels(BlockStateModelGenerator generator) {
        registerSnowLike(generator, CarbonizeCommon.ASH_BLOCK, CarbonizeCommon.ASH_LAYER);
        registerFire(generator, FireType.SOUL_FIRE_TYPE);
        registerStack(generator, CarbonizeCommon.WOOD_STACK, Blocks.SPRUCE_PLANKS);

        BurningSet.iterateSets(set -> {
            generator.registerSimpleCubeAll(set.charringWood);

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
                if (planks instanceof AbstractEmberBlock) {
                    registerEmberPlanks(generator, planks);
                    blocks.forEach(block -> {
                        if (block instanceof FenceBlock) registerEmberFence(generator, block, planks);
                        else if (block instanceof FenceGateBlock) registerEmberFenceGate(generator, block, planks);
                        else if (block instanceof StairsBlock) registerEmberStairs(generator, block, planks);
                        else if (block instanceof SlabBlock) registerEmberSlab(generator, block, planks);
                        else if (block instanceof PillarBlock) registerEmberLog(generator, block);
                        else if (block instanceof StackBlock) registerEmberStack(generator, block, planks);
                        else if (block != planks) registerEmberLeaves(generator, block);
                    });
                } else {
                    var pool = generator.registerCubeAllModelTexturePool(planks);
                    blocks.forEach(block -> {
                        if (block instanceof FenceBlock) pool.fence(block);
                        else if (block instanceof FenceGateBlock) pool.fenceGate(block);
                        else if (block instanceof StairsBlock) pool.stairs(block);
                        else if (block instanceof SlabBlock) pool.slab(block);
                        else if (block instanceof PillarBlock) generator.registerLog(block).log(block);
                        else if (block instanceof StackBlock) registerStack(generator, block, planks);
                        else if (block != planks) generator.registerSimpleCubeAll(block);
                    });
                }
            });
        });
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        itemModelGenerator.register(CarbonizeCommon.ASH, Models.GENERATED);
    }

    public void emberTemplate(
            BlockStateModelGenerator generator, Block block, Block texture,
            TriConsumer<Map<String, Identifier>, Identifier, TextureMap> createModels,
            TriFunction<MultipartBlockStateSupplier, Map<String, Identifier>, Function<When, When>, MultipartBlockStateSupplier> stateGetter
    ) {

        var blockStateSupplier = MultipartBlockStateSupplier.create(block);
        for (Stage stage : Stage.values()) {
            Map<String, Identifier> models = new HashMap<>();

            String model_name = Registries.BLOCK.getId(block).toString();
            String texture_name = Registries.BLOCK.getId(texture).toString().replace(":", ":block/");

            if (stage != BURNING) {
                model_name = model_name.replace("ember", stage.name().toLowerCase());
                texture_name = texture_name.replace("ember", stage.name().toLowerCase());
            }

            Identifier modelId = new Identifier(model_name);
            TextureMap textureMap = TextureMap.texture(new Identifier(texture_name));

            createModels.accept(models, modelId, textureMap);

            blockStateSupplier = stateGetter.apply(blockStateSupplier, models,
                    when -> {
                        var whenStage = When.create().set(AbstractEmberBlock.STAGE, stage);
                        if (when == null)
                            return whenStage;
                        return When.allOf(when, whenStage);
                    });
        }

        generator.blockStateCollector.accept(blockStateSupplier);
    }
    public void registerEmberPlanks(BlockStateModelGenerator generator, Block planks) {
        emberTemplate(generator, planks, planks,
                (map, model, textureMap) ->
                        map.put("cube", ((IModel)Models.CUBE_ALL).carbonize$upload(model, TextureMap.all(textureMap.getTexture(TextureKey.TEXTURE)), generator.modelCollector)),
                (blockStateSupplier, map, thenWhen) ->
                    blockStateSupplier.with(thenWhen.apply(null), BlockStateVariant.create().put(MODEL, map.get("cube")))
        );
    }
    public void registerEmberLeaves(BlockStateModelGenerator generator, Block leaves) {
        emberTemplate(generator, leaves, leaves,
                (map, model, textureMap) ->
                    map.put("cube", ((IModel)Models.CUBE_ALL).carbonize$upload(model, TextureMap.all(textureMap.getTexture(TextureKey.TEXTURE)), generator.modelCollector)),
                (blockStateSupplier, map, thenWhen) ->
                    blockStateSupplier.with(thenWhen.apply(null), BlockStateVariant.create().put(MODEL, map.get("cube")))
        );
    }
    public void registerEmberLog(BlockStateModelGenerator generator, Block log) {
        emberTemplate(generator, log, log,
                (map, model, textureMap) -> {
                    var texId = textureMap.getTexture(TextureKey.TEXTURE);
                    textureMap = new TextureMap().put(TextureKey.SIDE, texId).put(TextureKey.END, new Identifier(texId.toString() + "_top")).put(TextureKey.PARTICLE, texId);
                    map.put("vertical", ((IModel)Models.CUBE_COLUMN).carbonize$upload(model, textureMap, generator.modelCollector));
                    map.put("horizontal", ((IModel)Models.CUBE_COLUMN_HORIZONTAL).carbonize$upload(model, textureMap, generator.modelCollector));
                },
                (blockStateSupplier, map, thenWhen) -> blockStateSupplier
                        .with(thenWhen.apply(When.create().set(Properties.AXIS, Axis.Y)),
                                BlockStateVariant.create().put(VariantSettings.MODEL, map.get("vertical")))
                        .with(thenWhen.apply(When.create().set(Properties.AXIS, Axis.Z)),
                                BlockStateVariant.create().put(VariantSettings.MODEL, map.get("horizontal")).put(VariantSettings.X, Rotation.R90))
                        .with(thenWhen.apply(When.create().set(Properties.AXIS, Axis.X)),
                                BlockStateVariant.create().put(VariantSettings.MODEL, map.get("horizontal")).put(VariantSettings.X, Rotation.R90).put(VariantSettings.Y, Rotation.R90))
        );
    }
    public void registerEmberSlab(BlockStateModelGenerator generator, Block slab, Block texture) {
        emberTemplate(generator, slab, texture,
                (map, model, textureMap) -> {
                    textureMap = TextureMap.all(textureMap.getTexture(TextureKey.TEXTURE));
                    map.put("bottom", ((IModel)Models.SLAB).carbonize$upload(model, textureMap, generator.modelCollector));
                    map.put("top", ((IModel)Models.SLAB_TOP).carbonize$upload(model, textureMap, generator.modelCollector));
                    map.put("double", textureMap.getTexture(TextureKey.ALL));
                },
                (blockStateSupplier, map, thenWhen) -> blockStateSupplier
                        .with(thenWhen.apply(When.create().set(Properties.SLAB_TYPE, SlabType.BOTTOM)), BlockStateVariant.create().put(MODEL, map.get("bottom")))
                        .with(thenWhen.apply(When.create().set(Properties.SLAB_TYPE, SlabType.TOP)), BlockStateVariant.create().put(MODEL, map.get("top")))
                        .with(thenWhen.apply(When.create().set(Properties.SLAB_TYPE, SlabType.DOUBLE)), BlockStateVariant.create().put(MODEL, map.get("double")))
        );
    }
    public void registerEmberStairs(BlockStateModelGenerator generator, Block stairs, Block texture) {
        emberTemplate(generator, stairs, texture,
                (map, model, textureMap) -> {
                    textureMap = TextureMap.all(textureMap.getTexture(TextureKey.TEXTURE));
                    map.put("inner", ((IModel)Models.INNER_STAIRS).carbonize$upload(model, textureMap, generator.modelCollector));
                    map.put("default", ((IModel)Models.STAIRS).carbonize$upload(model, textureMap, generator.modelCollector));
                    map.put("outer", ((IModel)Models.OUTER_STAIRS).carbonize$upload(model, textureMap, generator.modelCollector));
                },
                (blockStateSupplier, map, thenWhen) -> {
                    List<Direction> directions = List.of(EAST, SOUTH, WEST, NORTH);
                    for (StairShape stairShape : StairShape.values())
                        for (BlockHalf half : List.of(BlockHalf.values()))
                            for (int i = 0; i < directions.size(); i++) {
                                 Direction direction = directions.get(i);
                                 var shapeName = stairShape.name().toLowerCase();
                                 if (shapeName.contains("right") && half == BlockHalf.TOP)
                                    direction = directions.get(ModelHelper.shift(i, -1, directions.size()));
                                 if (shapeName.contains("left") && half == BlockHalf.BOTTOM)
                                    direction = directions.get(ModelHelper.shift(i, 1, directions.size()));

                                 var variant = BlockStateVariant.create().put(Y, Rotation.values()[i]);
                                 variant = variant.put(MODEL, shapeName.contains("inner") ? map.get("inner") : shapeName.contains("outer") ? map.get("outer") : map.get("default"));
                                 if (half == BlockHalf.TOP)
                                    variant = variant.put(X, Rotation.R180);
                                 if (half == BlockHalf.TOP || i != 0)
                                    variant = variant.put(UVLOCK, true);
                                 var when = thenWhen.apply(When.allOf(
                                        When.create().set(Properties.HORIZONTAL_FACING, direction),
                                        When.create().set(Properties.BLOCK_HALF, half),
                                        When.create().set(Properties.STAIR_SHAPE, stairShape)
                                 ));
                                 blockStateSupplier.with(when, variant);
                            }

                    return blockStateSupplier;
                }
        );
    }
    public void registerEmberFenceGate(BlockStateModelGenerator generator, Block emberFenceGate, Block emberTexture) {
        emberTemplate(generator, emberFenceGate, emberTexture,
                (map, model, textureMap) -> {
                    map.put("fence_gate_open", ((IModel)Models.TEMPLATE_FENCE_GATE_OPEN).carbonize$upload(model, textureMap, generator.modelCollector));
                    map.put("fence_gate_closed", ((IModel)Models.TEMPLATE_FENCE_GATE).carbonize$upload(model, textureMap, generator.modelCollector));
                    map.put("fence_gate_gate_wall_open", ((IModel)Models.TEMPLATE_FENCE_GATE_WALL_OPEN).carbonize$upload(model, textureMap, generator.modelCollector));
                    map.put("fence_gate_gate_wall_closed", ((IModel)Models.TEMPLATE_FENCE_GATE_WALL).carbonize$upload(model, textureMap, generator.modelCollector));
                },
                (blockStateSupplier, map, thenWhen) -> {
                    var closedModelId = map.get("fence_gate_closed");
                    var openModelId = map.get("fence_gate_open");
                    var closedWallModelId = map.get("fence_gate_gate_wall_closed");
                    var openWallModelId = map.get("fence_gate_gate_wall_open");

                    for (var direction : Direction.Type.HORIZONTAL) {
                        var rotation = switch (direction) {
                            case SOUTH -> Rotation.R0;
                            case WEST -> Rotation.R90;
                            case NORTH -> Rotation.R180;
                            case EAST -> Rotation.R270;
                            default -> null;
                        };

                        blockStateSupplier = blockStateSupplier
                                .with(whenFenceGate(thenWhen, direction, false, false), create().put(MODEL, closedModelId).put(Y, rotation))
                                .with(whenFenceGate(thenWhen, direction, true, false), create().put(MODEL, closedWallModelId).put(Y, rotation))
                                .with(whenFenceGate(thenWhen, direction, false, true), create().put(MODEL, openModelId).put(Y, rotation))
                                .with(whenFenceGate(thenWhen, direction, true, true), create().put(MODEL, openWallModelId).put(Y, rotation));
                    }

                    return blockStateSupplier;
                }
        );
    }
    private When whenFenceGate(Function<When, When> thenWhen, Direction direction, boolean inWall, boolean open) {
        return thenWhen.apply(When.allOf(When.create().set(Properties.HORIZONTAL_FACING, direction), When.create().set(Properties.IN_WALL, inWall), When.create().set(Properties.OPEN, open)));
    }
    public void registerEmberFence(BlockStateModelGenerator generator, Block emberFence, Block emberTexture) {
        var emberTextureMap = TextureMap.texture(emberTexture);
        emberTemplate(generator, emberFence, emberTexture,
                (map, model, textureMap) -> {
                    map.put("post_model", ((IModel)Models.FENCE_POST).carbonize$upload(model, textureMap, generator.modelCollector));
                    map.put("side_model", ((IModel)Models.FENCE_SIDE).carbonize$upload(model, textureMap, generator.modelCollector));
                },
                (blockStateSupplier, map, thenWhen) -> {
                    var post_model = map.get("post_model");
                    var side_model = map.get("side_model");
                    return blockStateSupplier
                            .with(thenWhen.apply(null), create().put(MODEL, post_model))
                            .with(thenWhen.apply(When.create().set(Properties.NORTH, true)), create().put(MODEL, side_model).put(UVLOCK, true))
                            .with(thenWhen.apply(When.create().set(Properties.EAST, true)), create().put(MODEL, side_model).put(Y, Rotation.R90).put(UVLOCK, true))
                            .with(thenWhen.apply(When.create().set(Properties.SOUTH, true)), create().put(MODEL, side_model).put(Y, Rotation.R180).put(UVLOCK, true))
                            .with(thenWhen.apply(When.create().set(Properties.WEST, true)), create().put(MODEL, side_model).put(Y, Rotation.R270).put(UVLOCK, true));
                }
        );
        Identifier inventory_model = Models.FENCE_INVENTORY.upload(emberFence, emberTextureMap, generator.modelCollector);
        generator.registerParentedItemModel(emberFence, inventory_model);
    }
    public void registerEmberStack(BlockStateModelGenerator generator, Block emberStack, Block emberTexture) {
        emberTemplate(generator, emberStack, emberTexture,
            (map, model, textureMap) ->
                    map.put("model", ((IModel)STACK_MODEL).carbonize$upload(model, textureMap, generator.modelCollector)),
            (blockStateSupplier, map, thenWhen) -> {
                var model = map.get("model");
                return blockStateSupplier.with(thenWhen.apply(null),
                        create().put(Y, Rotation.R0).put(MODEL, model),
                        create().put(Y, Rotation.R90).put(MODEL, model),
                        create().put(Y, Rotation.R180).put(MODEL, model),
                        create().put(Y, Rotation.R270).put(MODEL, model));
            }
        );
    }
    public void registerStack(BlockStateModelGenerator generator, Block stack, Block texture) {
        var model = STACK_MODEL.upload(stack, TextureMap.texture(texture), generator.modelCollector);
        generator.blockStateCollector.accept(VariantsBlockStateSupplier.create(stack,
                create().put(Y, Rotation.R0).put(MODEL, model),
                create().put(Y, Rotation.R90).put(MODEL, model),
                create().put(Y, Rotation.R180).put(MODEL, model),
                create().put(Y, Rotation.R270).put(MODEL, model)
        ));
    }
    private void registerFire(BlockStateModelGenerator blockStateModelGenerator, @SuppressWarnings("SameParameterValue") FireType fireType) {
        var block = fireType.asFireBlock();
        When when = When.create().set(Properties.NORTH, false).set(Properties.EAST, false).set(Properties.SOUTH, false).set(Properties.WEST, false).set(Properties.UP, false);
        List<Identifier> list = blockStateModelGenerator.getFireFloorModels(block);
        List<Identifier> list2 = blockStateModelGenerator.getFireSideModels(block);
        List<Identifier> list3 = blockStateModelGenerator.getFireUpModels(block);
        blockStateModelGenerator.blockStateCollector.accept(MultipartBlockStateSupplier.create(block)
                .with(when, buildBlockStateVariants(list, (blockStateVariant) -> blockStateVariant))
                .with(When.anyOf(When.create().set(Properties.NORTH, true), when), buildBlockStateVariants(list2, (blockStateVariant) -> blockStateVariant))
                .with(When.anyOf(When.create().set(Properties.EAST, true), when), buildBlockStateVariants(list2, (blockStateVariant) -> blockStateVariant.put(Y, Rotation.R90)))
                .with(When.anyOf(When.create().set(Properties.SOUTH, true), when), buildBlockStateVariants(list2, (blockStateVariant) -> blockStateVariant.put(Y, Rotation.R180)))
                .with(When.anyOf(When.create().set(Properties.WEST, true), when), buildBlockStateVariants(list2, (blockStateVariant) -> blockStateVariant.put(Y, Rotation.R270)))
                .with(When.create().set(Properties.UP, true), buildBlockStateVariants(list3, (blockStateVariant) -> blockStateVariant)));
    }
    private void registerSnowLike(BlockStateModelGenerator modelCollector, @SuppressWarnings("SameParameterValue") Block fullCube, @SuppressWarnings("SameParameterValue") Block snowLike) {
        TextureMap textureMap = TextureMap.all(fullCube);
        Identifier identifier = Models.CUBE_ALL.upload(fullCube, textureMap, modelCollector.modelCollector);
        modelCollector.blockStateCollector.accept(VariantsBlockStateSupplier.create(snowLike).coordinate(BlockStateVariantMap.create(Properties.LAYERS).register((height) -> {
            BlockStateVariant stateVariant = create();
            Identifier layerId;
            if (height < 8) {
                layerId = ModelIds.getBlockSubModelId(snowLike, "_height" + height * 2);
                getLayerModel(height * 2).upload(layerId, new TextureMap().put(TextureKey.TEXTURE, identifier).put(TextureKey.PARTICLE, identifier), modelCollector.modelCollector);
            } else {
                layerId = identifier;
            }

            return stateVariant.put(MODEL, layerId);
        })));
        modelCollector.registerParentedItemModel(snowLike, ModelIds.getBlockSubModelId(snowLike, "_height2"));
        modelCollector.blockStateCollector.accept(createSingletonBlockState(fullCube, identifier));
    }
}
