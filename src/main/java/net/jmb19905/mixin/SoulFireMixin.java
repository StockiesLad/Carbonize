package net.jmb19905.mixin;

import com.google.common.collect.ImmutableMap;
import net.jmb19905.block.GenericFireBlock;
import net.jmb19905.block.ISoulFireAccess;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.minecraft.block.FireBlock.*;

@Mixin(SoulFireBlock.class)
public class SoulFireMixin extends AbstractFireMixin implements ISoulFireAccess {
    @Shadow
    public static boolean isSoulBase(BlockState state) {return false;}

    @Unique
    private static final GenericFireBlock FIRE_BLOCK = new GenericFireBlock(
            false,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .replaceable()
                    .noCollision()
                    .breakInstantly()
                    .luminance((state) -> 10)
                    .sounds(BlockSoundGroup.WOOL)
                    .pistonBehavior(PistonBehavior.DESTROY),
            () -> (AbstractFireBlock) Blocks.SOUL_FIRE,
            (state, tagKey) -> isSoulBase(state),
            (view, pos) -> {
                BlockPos blockPos = pos.down();
                BlockState state = view.getBlockState(blockPos);
                TagKey<Block> tag = null;
                if (view instanceof World world)
                    tag = world.getDimension().infiniburn();
                return state.isIn(tag) ? ((IFireAccess)Blocks.FIRE).carbonize$getStateForPosition(view, pos) :
                        ((ISoulFireAccess)Blocks.SOUL_FIRE).carbonize$getStateForPosition(view, pos);
            },
            0.75F,
            300
    );

    @Unique
    private Map<BlockState, VoxelShape> shapesByState;

    @SuppressWarnings("DataFlowIssue")
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(AbstractBlock.Settings settings, CallbackInfo ci) {
        setDefaultState(getStateManager().getDefaultState()
                .with(AGE, 0)
                .with(NORTH, false)
                .with(EAST, false)
                .with(SOUTH, false)
                .with(WEST, false)
                .with(UP, false)
        );
        shapesByState = ImmutableMap.copyOf(getStateManager()
                .getStates()
                .stream()
                .filter((state) -> state.get(AGE) == 0)
                .collect(Collectors.toMap(Function.identity(), IFireAccess::carbonize$getShapeForState))
        );
    }

    @Override
    protected void override$appendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        ((IFireAccess)FIRE_BLOCK).carbonize$appendProperties(builder);
        ci.cancel();
    }


    @Inject(method = "getStateForNeighborUpdate", at = @At("HEAD"), cancellable = true)
    private void override$getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> cir) {
        cir.setReturnValue(FIRE_BLOCK.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos));
        cir.cancel();
    }

    @Override
    public void override$getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        cir.setReturnValue(this.shapesByState.get(state.with(AGE, 0)));
        cir.cancel();
    }

    @Override
    public void override$getPlacementState(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> cir) {
        cir.setReturnValue(FIRE_BLOCK.getPlacementState(ctx));
        cir.cancel();
    }

    @Override
    public void override$onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify, CallbackInfo ci) {
        FIRE_BLOCK.onBlockAdded(state, world, pos, oldState, notify);
        ci.cancel();
    }

    @Override
    public void override$scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        FIRE_BLOCK.scheduledTick(state, world, pos, random);
    }

    @Inject(method = "canPlaceAt", at = @At("RETURN"), cancellable = true)
    private void override$CanPlaceAt(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
         cir.setReturnValue(cir.getReturnValue() || FIRE_BLOCK.canPlaceAt(state, world, pos));
         cir.cancel();
    }

    @Inject(method = "isFlammable", at = @At("HEAD"), cancellable = true)
    protected void override$isFlammable(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(((IFireAccess)FIRE_BLOCK).carbonize$isFlammable(state));
    }

    @Override
    public void carbonize$registerFlammableBlock(Block block, int burnChance, int spreadChance) {
        ((IFireAccess)FIRE_BLOCK).carbonize$registerFlammableBlock(block, burnChance, spreadChance);
    }

    @Override
    public boolean carbonize$isFlammable(BlockState state) {
        return ((IFireAccess)FIRE_BLOCK).carbonize$isFlammable(state);
    }

    @Override
    public BlockState carbonize$getStateForPosition(BlockView world, BlockPos pos) {
        return ((IFireAccess)FIRE_BLOCK).carbonize$getStateForPosition(world, pos);
    }
}
