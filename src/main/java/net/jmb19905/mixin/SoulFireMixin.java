package net.jmb19905.mixin;

import net.jmb19905.api.FireAccess;
import net.jmb19905.api.FireCapabilityProvider;
import net.jmb19905.api.FireType;
import net.jmb19905.block.fire.ModularFireBlock;
import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.ItemPlacementContext;
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

import static net.minecraft.block.FireBlock.AGE;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(SoulFireBlock.class)
public class SoulFireMixin extends AbstractFireMixin implements FireCapabilityProvider {
    @Shadow
    public static boolean isSoulBase(BlockState state) {return false;}

    //TODO: making this non-static might break things
    @Unique
    private static ModularFireBlock FIRE_BLOCK = new ModularFireBlock(
            false,
            AbstractBlock.Settings.create()
                    .mapColor(MapColor.LIGHT_BLUE)
                    .replaceable()
                    .noCollision()
                    .breakInstantly()
                    //.luminance((state) -> ((SoulFireBlock) (Object) this).getDefaultState().getLuminance())
                    .luminance(state -> 10)
                    .sounds(BlockSoundGroup.WOOL)
                    .pistonBehavior(PistonBehavior.DESTROY),
            () -> FireType.SOUL_FIRE_TYPE
    );

    @Unique
    private Map<BlockState, VoxelShape> shapesByState;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(AbstractBlock.Settings settings, CallbackInfo ci) {
        var fireAccess = FireAccess.tryGet(FIRE_BLOCK, false).orElseThrow();
        setDefaultState(fireAccess.carbonize$getDefaultState(getStateManager()));
        shapesByState = fireAccess.carbonize$getVoxelShapes(getStateManager());
    }

    @Override
    protected void override$appendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        FireAccess.tryGet(FIRE_BLOCK, false).orElseThrow().carbonize$appendProperties(builder);
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
    private void override$canPlaceAt(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
         cir.setReturnValue(FIRE_BLOCK.canPlaceAt(state, world, pos));
         cir.cancel();
    }

    @Inject(method = "isFlammable", at = @At("HEAD"), cancellable = true)
    protected void override$isFlammable(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(isBlockFlammable(state));
    }

    @Override
    public int getMaxTemperature() {
        return -1100;
    }

    @Override
    public int getReflectivity() {
        return 300;
    }

    @Override
    public double getTickSpeedFactor() {
        return (float) 1 / 3;
    }

    @Override
    public boolean isBaseInfiniburn(BlockView view, BlockPos pos) {
        return isSoulBase(view.getBlockState(pos.down()));
    }

    @Override
    public FireAccess getCapability() {
        return FIRE_BLOCK.access((AbstractFireBlock) (Object) this);
    }
}
