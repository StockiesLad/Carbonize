package net.jmb19905.core;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.jmb19905.api.FireType;
import net.jmb19905.block.charring.CharringWoodBlockEntity;
import net.jmb19905.multiblock.CharcoalPitMultiblock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import static net.jmb19905.core.CarbonizeConstants.CONFIG;
import static net.jmb19905.core.CarbonizeConstants.MOD_ID;
import static net.jmb19905.core.CarbonizeCommon.*;
import static net.jmb19905.multiblock.CharcoalPitProviders.addMultiBlock;

public class CharcoalPitInit {
    private static final Identifier AETHER = new Identifier("aether", "the_aether");
    private static final Identifier NETHER = new Identifier("the_nether");

    //This is for owo-lib. DO NOT REMOVE
    public static final Identifier CHARRING_WOOD_ID = new Identifier(MOD_ID, "charring_wood");

    public static final BlockEntityType<CharringWoodBlockEntity> CHARRING_WOOD_TYPE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(MOD_ID, "charring_wood"),
            FabricBlockEntityTypeBuilder.create(CharringWoodBlockEntity::new)
                    .addBlocks(CHARCOAL_SET.charringWood, SOUL_CHARCOAL_SET.charringWood).build()
    );

    public static void init() {
        addMultiBlock(world -> {
            var id = world.getDimensionKey().getValue();
            return !(id.equals(AETHER) || id.equals(NETHER));
        }, CharcoalPitMultiblock::new);
        addMultiBlock(world -> world.getDimensionKey().getValue().equals(NETHER), CharcoalPitMultiblock::new);

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!CONFIG.charcoalPile()) return ActionResult.PASS;

            ItemStack stack = player.getStackInHand(hand);

            if (world.isClient) return ActionResult.PASS;
            if (hitResult.getType() != HitResult.Type.BLOCK) return ActionResult.PASS;
            if (!player.isSneaking()) return ActionResult.PASS;
            if (!stack.isIn(IGNITERS)) return ActionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();

            for (FireType fireType : FireType.getAllTypes()) {
                BlockState parentState = world.getBlockState(pos);

                if (!fireType.isBlockFlammable(parentState)) continue;

                int blockCount = CharcoalPitMultiblock.collectFuels((ServerWorld) world, pos, hitResult.getSide(), fireType).size();
                if (blockCount < CONFIG.charcoalPileMinimumCount()) continue;

                world.playSound(null, pos, SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.BLOCKS, 2, 1);
                handleIgnition(stack, player, hand);
                world.setBlockState(pos, fireType.charringBlock.getDefaultState());
                world.getBlockEntity(pos, CHARRING_WOOD_TYPE).ifPresent(blockEntity -> blockEntity.sync(parentState));
                return ActionResult.CONSUME;
            }

            return ActionResult.PASS;
        });
    }

    private static void handleIgnition(ItemStack stack, PlayerEntity player, Hand hand) {
        if (stack.isIn(DAMAGE_IGNITERS)) stack.damage(stack.getDamage() + 1, player, p -> p.sendToolBreakStatus(hand));
        if (stack.isIn(CONSUME_IGNITERS)) stack.decrement(1);
    }
}
