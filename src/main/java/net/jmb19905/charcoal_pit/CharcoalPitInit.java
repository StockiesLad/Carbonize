package net.jmb19905.charcoal_pit;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.jmb19905.block.StackBlock;
import net.jmb19905.charcoal_pit.block.CharringWoodBlock;
import net.jmb19905.charcoal_pit.block.CharringWoodBlockEntity;
import net.jmb19905.charcoal_pit.multiblock.CharcoalPitMultiblock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import static net.jmb19905.Carbonize.*;
import static net.jmb19905.charcoal_pit.multiblock.CharcoalPitProviders.addMultiBlock;

public class CharcoalPitInit {
    private static final Identifier AETHER = new Identifier("aether", "the_aether");
    private static final Identifier NETHER = new Identifier("the_nether");

    //This is for owo-lib. DO NOT REMOVE
    public static final Identifier CHARRING_WOOD_ID = new Identifier(MOD_ID, "charring_wood");

    public static final Block CHARRING_WOOD = new CharringWoodBlock(FabricBlockSettings.create().nonOpaque().luminance(15).sounds(BlockSoundGroup.WOOD).dropsNothing());
    public static final Block CHARRING_STACK = new StackBlock(FabricBlockSettings.create().nonOpaque());

    public static final Block SOUL_CHARRING_WOOD = new CharringWoodBlock(FabricBlockSettings.create().nonOpaque().luminance(10).sounds(BlockSoundGroup.WOOD).dropsNothing());
    public static final Block SOUL_CHARRING_STACK = new StackBlock(FabricBlockSettings.create().nonOpaque());

    public static final BlockEntityType<CharringWoodBlockEntity> CHARRING_WOOD_TYPE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(MOD_ID, "charring_wood"),
            FabricBlockEntityTypeBuilder.create(CharringWoodBlockEntity::new).addBlocks(CHARRING_WOOD, SOUL_CHARRING_WOOD).build()
    );

    public static void init() {
        addMultiBlock(world -> {
            var id = world.getDimensionKey().getValue();
            return !(id.equals(AETHER) || id.equals(NETHER));
        }, CharcoalPitMultiblock::new);
        addMultiBlock(world -> world.getDimensionKey().getValue().equals(NETHER), CharcoalPitMultiblock::new);

        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "charring_wood"), CHARRING_WOOD);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "charring_stack"), CHARRING_STACK);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "charring_wood"), new BlockItem(CHARRING_WOOD, new FabricItemSettings()));

        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "soul_charring_wood"), SOUL_CHARRING_WOOD);
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "soul_charring_stack"), SOUL_CHARRING_STACK);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "soul_charring_wood"), new BlockItem(SOUL_CHARRING_WOOD, new FabricItemSettings()));
        FlammableBlockRegistry.getDefaultInstance().add(CHARRING_WOOD, 15, 30);
        FlammableBlockRegistry.getDefaultInstance().add(CHARRING_STACK, 15, 30);

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!CONFIG.charcoalPile()) return ActionResult.PASS;
            ItemStack stack = player.getStackInHand(hand);
            if (!world.isClient && stack.isIn(IGNITERS) && player.isSneaking()) {
                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockPos pos = hitResult.getBlockPos();
                    int blockCount = CharcoalPitMultiblock.collectFuels((ServerWorld) world, pos, hitResult.getSide()).size();
                    if (blockCount >= CONFIG.charcoalPileMinimumCount() && handleIgnition(stack, player, hand)) {
                        BlockState parentState = world.getBlockState(pos);
                        world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 2, 1);
                        world.setBlockState(pos, CHARRING_WOOD.getDefaultState());
                        world.getBlockEntity(pos, CHARRING_WOOD_TYPE).ifPresent(blockEntity -> blockEntity.sync(parentState));
                        return ActionResult.CONSUME;
                    }
                }
            }
            return ActionResult.PASS;
        });
    }

    private static boolean handleIgnition(ItemStack stack, PlayerEntity player, Hand hand) {
        if (stack.isIn(DAMAGE_IGNITERS)) {
            stack.damage(stack.getDamage() + 1, player, p -> p.sendToolBreakStatus(hand));
            return true;
        }

        if (stack.isIn(CONSUME_IGNITERS)) {
            stack.decrement(1);
            return true;
        }

        return false;
    }
}
