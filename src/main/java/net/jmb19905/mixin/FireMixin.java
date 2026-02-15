package net.jmb19905.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.jmb19905.api.FireAccess;
import net.jmb19905.api.FireType;
import net.jmb19905.block.AshBlock;
import net.jmb19905.block.fire.ModularFireBlock;
import net.jmb19905.core.CarbonizeCommon;
import net.jmb19905.recipe.BurnRecipe;
import net.jmb19905.util.BlockHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.block.TntBlock;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.jmb19905.core.CarbonizeConstants.CONFIG;

//TODO: expose state manager and setDefault state to make ModularFireBlock easier to use.
//TODO: add OPTIONAL support for ember blocks (in case some fires dont want it).
@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(FireBlock.class)
public class FireMixin extends AbstractFireMixin implements FireAccess {
    @Unique
    private final Object2IntMap<TagKey<Block>> tagBurnChances = new Object2IntOpenHashMap<>();
    @Unique
    private final Object2IntMap<TagKey<Block>> tagSpreadChances = new Object2IntOpenHashMap<>();

    @Shadow
    private BlockState getStateWithAge(WorldAccess world, BlockPos pos, int age) {
        return null;
    }

    @Shadow
    private void registerFlammableBlock(Block block, int burnChance, int spreadChance) {}

    @Shadow
    protected boolean isFlammable(BlockState state) {return false;}

    @Shadow
    private int getSpreadChance(BlockState state) {return 0;}

    @Shadow
    private int getBurnChance(BlockState state) {return 0;}

    @Shadow
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {}

    @Shadow
    protected BlockState getStateForPosition(BlockView world, BlockPos pos) {return null;}

    @Inject(method = "registerDefaultFlammables", at = @At("TAIL"))
    private static void registerOtherFlammables(CallbackInfo ci) {
        ModularFireBlock.registerDefaultFlammables();
    }

    @Redirect(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean checkPlacementConditions(BlockState state, TagKey<Block> tagKey, @Local(argsOnly = true) ServerWorld view, @Local(argsOnly = true) BlockPos pos) {
        if ((Object) this instanceof ModularFireBlock fireBlock)
            return fireBlock.parentView().isBaseInfiniburn(view, pos);
        else return isBaseInfiniburn(view, pos);
    }

    @Redirect(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FireBlock;trySpreadingFire(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/util/math/random/Random;I)V"))
    private void spreadFireAggressively(FireBlock fire, World world, BlockPos newPos, @SuppressWarnings("ParameterCanBeLocal") int reflectivity, Random random, int currentAge, @Local(argsOnly = true) BlockPos pos, @Local(name = "k") int bonus) {
        reflectivity = getReflectivity() + bonus;
        carbonize$trySpread(world, pos, newPos, reflectivity + (pos.subtract(newPos).getY() > 0 ? -25 : 25), random, currentAge);
        if (!CONFIG.increasedFireSpreadRange() && !world.isRaining()) return;

        if (random.nextBoolean()) return;
        var offset = newPos.subtract(pos);
        var diagonal = new BlockPos(1, 1, 1).multiply(offset.getX() + offset.getY() + offset.getZ()).subtract(offset);
        var oppDiagonal = new BlockPos(1, 1, 1)
                .subtract(new Vec3i(offset.getZ(), offset.getX(), offset.getY())
                        .multiply(2)
                        .multiply(offset.getX() + offset.getY() + offset.getZ())
                ).multiply(offset.getX() + offset.getY() + offset.getZ()).subtract(offset);
        carbonize$trySpread(world, pos, diagonal.add(pos), reflectivity + (diagonal.getY() > 0 ? -15 : 15), random, currentAge);
        carbonize$trySpread(world, pos, oppDiagonal.add(pos), reflectivity + (oppDiagonal.getY() > 0 ? -15 : 15), random, currentAge);

        if (random.nextBoolean()) return;
        offset = offset.multiply(2);
        carbonize$trySpread(world, pos, offset.add(pos), reflectivity + (offset.getY() > 0 ? -50 : 50), random, currentAge);
    }

    //TODO: make fire spread blocked by non flammable blocks in the way.
    @Unique
    private void carbonize$trySpread(World world, BlockPos posFrom, BlockPos pos, int reflectivity, Random random, int currentAge) {
        int i = this.getSpreadChance(world.getBlockState(pos));
        if (random.nextInt(reflectivity) < i) {

            BlockState state = world.getBlockState(pos);
            var dx = posFrom.getX() - pos.getX();
            var dy = posFrom.getY() - pos.getY();
            var dz = posFrom.getZ() - pos.getZ();
            //Inverse square law
            var heat = getMaxTemperature() / Math.max(dx * dx + dy * dy + dz * dz, 1);
            heat = heat - (world.hasRain(pos) ? 250 : 0);
            heat = heat + 20 * currentAge;

            var moment = Math.signum(heat) * random.nextInt(Math.max(Math.abs(heat), 1));
            if (moment >= -100 && moment <= 200) {
                tryIgniteTnt(world, pos, state);
            } else if (moment >= -300 && moment <= 600) {
                if (tryIgniteTnt(world, pos, state)) return;

                assert world.getServer() != null;
                for (var burnRecipe : world.getServer().getRecipeManager().listAllOfType(CarbonizeCommon.BURN_RECIPE_TYPE))
                    if (burnRecipe.isInput(state, asFireType())) {
                        world.setBlockState(pos, burnRecipe.burnBlock().getDefaultState());
                        break;
                    }
            } else if (moment >= -400 && moment <= 800) {
                if (tryIgniteTnt(world, pos, state)) return;

                int newAge = Math.max(0, Math.min(15, currentAge - 5 + random.nextInt(10)));
                world.setBlockState(pos, this.getStateWithAge(world, pos, newAge), Block.NOTIFY_ALL);
            } else {
                if (state.getBlock() instanceof TntBlock) {
                    world.createExplosion(null, pos.getX(), pos.getY() + 0.0625, pos.getZ(), 4.0F, World.ExplosionSourceType.TNT);
                } else world.removeBlock(pos, false);
            }
        }
    }

    @Unique
    private boolean tryIgniteTnt(World world, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof TntBlock) {
            TntBlock.primeTnt(world, pos);
            return true;
        }
        return false;
    }


    //TODO: dont forget about this
    @Unique
    private boolean hasNotProducedBiproduct(World world, BlockPos pos, Random random) {
        BlockState state = world.getBlockState(pos);
        world.getRecipeManager().listAllOfType(CarbonizeCommon.BURN_RECIPE_TYPE);
        if (CONFIG.burnCrafting()) {
            for (BurnRecipe burnRecipe : world.getRecipeManager().listAllOfType(CarbonizeCommon.BURN_RECIPE_TYPE)) {
                if (burnRecipe.isInput(state, asFireType())) {
                    float surfaceExposurePercentage = getExposedSurfacePercentage(world, pos);
                    float randomFloat = random.nextFloat();
                    if (randomFloat > surfaceExposurePercentage) {
                        world.setBlockState(pos, BlockHelper.transferState(burnRecipe.successResult().getDefaultState(), state));
                        return false;
                    }
                }
            }
        }
        if (random.nextFloat() > 0.3f && CONFIG.createAsh()) {
            world.setBlockState(pos, CarbonizeCommon.ASH_LAYER.getDefaultState().with(AshBlock.LAYERS, getAshLayerCount(random, state)));
            return false;
        }
        return true;
    }

    @Unique
    private int getAshLayerCount(Random random, BlockState state) {
        int layers = random.nextInt(2) + 1;
        layers += ((int) state.getBlock().getHardness());
        return MathHelper.clamp(layers, 1, 4);
    }

    @Unique
    private float getExposedSurfacePercentage(World world, BlockPos pos) {
        float val = 0.0f;
        for (Direction dir : Direction.values()) {
            if (world.getBlockState(pos.offset(dir)).isAir()) {
                val += 1f/6f;
            }
        }
        return val;
    }

    @Redirect(method = "getStateWithAge", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    protected boolean override$getStateWithAge(BlockState state, Block block) {
        if ((Object) this instanceof ModularFireBlock fireBlock)
            block = fireBlock.getType().asFireBlock();
        return state.isOf(block);
    }

    @Redirect(method = "getStateWithAge", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FireBlock;getState(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
    private BlockState getProperState(BlockView view, BlockPos pos) {
        if ((Object) this instanceof ModularFireBlock fireBlock)
            return ModularFireBlock.findAppropriateFire(view, pos, fireBlock.getType());
        else return ModularFireBlock.findAppropriateFire(view, pos, FireType.DEFAULT_FIRE_TYPE);
    }

    @Redirect(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;scheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V"))
    protected void override$scheduleBlockTick(ServerWorld world, BlockPos pos, Block block, int i) {
        if ((Object) this instanceof ModularFireBlock fireBlock) {
            block = fireBlock.getType().asFireBlock();
            i = (int) Math.ceil(i * getTickSpeedFactor());
        }
        world.scheduleBlockTick(pos, block, i);
    }

    @Redirect(method = "onBlockAdded", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;scheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V"))
    protected void override$onBlockAdded(World world, BlockPos pos, Block block, int i) {
        if ((Object) this instanceof ModularFireBlock fireBlock) {
            block = fireBlock.getType().asFireBlock();
            i = (int) Math.ceil(i * getTickSpeedFactor());
        }
        world.scheduleBlockTick(pos, block, i);
    }

    @Redirect(method = "getStateForPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FireBlock;getDefaultState()Lnet/minecraft/block/BlockState;"))
    protected BlockState override$getDefaultState(FireBlock block) {
        if ((Object) this instanceof ModularFireBlock fireBlock)
            return fireBlock.getType().asFireBlock().getDefaultState();
        return block.getDefaultState();
    }

    //TODO: Idk if this Works
    @Redirect(method = "areBlocksAroundFlammable", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FireBlock;isFlammable(Lnet/minecraft/block/BlockState;)Z"))
    private boolean orEmber(FireBlock fireBlock, BlockState state) {
        return isFlammable(state) || state.isOf(CarbonizeCommon.CHARCOAL_SET.emberPlanks);
    }
    //TODO: this is breaking fire blocks. Soul soot is losing fire when its flammable.
    @Redirect(method = "getStateForPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FireBlock;isFlammable(Lnet/minecraft/block/BlockState;)Z"))
    private boolean orEmber$(FireBlock fireBlock, BlockState state) {
        return isFlammable(state) || state.isOf(CarbonizeCommon.CHARCOAL_SET.emberPlanks);
    }
    //TODO: PLEASE REWRITE. ITS SHIT
    @Redirect(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;canPlaceAt(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean removeIfEmber(BlockState state, WorldView world, BlockPos pos) {
        BlockPos blockPos = pos.down();
        var downState = world.getBlockState(blockPos);
        var downCond = downState.isSideSolidFullSquare(world, blockPos, Direction.UP) && !downState.isOf(CarbonizeCommon.CHARCOAL_SET.emberPlanks);
        var sideCond = false;

        for (Direction direction : Direction.values()) {
            var locState = world.getBlockState(pos.offset(direction));
            if (locState.isOf(CarbonizeCommon.CHARCOAL_SET.emberPlanks)) continue;
            if (this.isFlammable(locState)) sideCond = true;
        }

        return  downCond || sideCond;
    }

    @Inject(method = "getSpreadChance", at = @At("RETURN"), cancellable = true)
    private void getTagSpreadChance(BlockState state, CallbackInfoReturnable<Integer> cir) {
        for (TagKey<Block> tag : tagSpreadChances.keySet()) {
            if (state.isIn(tag)) {
                var tagSpreadChance = tagSpreadChances.getOrDefault(tag, 0);
                if (tagSpreadChance > 0) {
                    cir.setReturnValue(tagSpreadChance);
                    cir.cancel();
                }
            }
        }
    }

    @Inject(method = "getBurnChance(Lnet/minecraft/block/BlockState;)I", at = @At("RETURN"), cancellable = true)
    private void getTagBurnChance(BlockState state, CallbackInfoReturnable<Integer> cir) {
        for (TagKey<Block> tag : tagBurnChances.keySet()) {
            if (state.isIn(tag)) {
                var tagBurnChance = tagBurnChances.getOrDefault(tag, 0);
                if (tagBurnChance > 0) {
                    cir.setReturnValue(tagBurnChance);
                    cir.cancel();
                }
            }
        }
    }

    @Override
    public void registerFlammable(Block block, int burnChance, int spreadChance) {
        registerFlammableBlock(block, burnChance, spreadChance);
    }

    @Override
    public void registerFlammable(TagKey<Block> tag, int burnChance, int spreadChance) {
        tagBurnChances.put(tag, burnChance);
        tagSpreadChances.put(tag, spreadChance);
    }

    @Override
    public BlockState findAppropriateState(BlockView view, BlockPos pos) {
        return getStateForPosition(view, pos);
    }

    @Override
    public boolean isBlockFlammable(BlockState state) {
        return isFlammable(state);
    }

    @Override
    public int getBlockSpreadChance(BlockState state) {
        return getSpreadChance(state);
    }

    @Override
    public int getBlockBurnChance(BlockState state) {
        return getBurnChance(state);
    }

    @Override
    public int getMaxTemperature() {
        return (Object)this instanceof ModularFireBlock fireBlock ? fireBlock.getType().getMaxTemperature() : 1100;
    }

    @Override
    public int getReflectivity() {
        return (Object)this instanceof ModularFireBlock fireBlock ? fireBlock.getType().getReflectivity() : 400;
    }

    @Override
    public double getTickSpeedFactor() {
        return (Object)this instanceof ModularFireBlock fireBlock ? fireBlock.getType().getTickSpeedFactor() : 1;
    }

    @Override
    public FireType asFireType() {
        return (Object)this instanceof ModularFireBlock fireBlock ? fireBlock.getType() : FireType.DEFAULT_FIRE_TYPE;
    }

    @Override
    public boolean isBaseInfiniburn(BlockView view, BlockPos pos) {
        if ((Object)this instanceof ModularFireBlock fireBlock)
            return fireBlock.parentView().isBlockFlammable(view.getBlockState(pos));
        return view.getBlockState(pos.down()).isIn(((World)view).getDimension().infiniburn());
    }

    @Override
    public void carbonize$appendProperties(StateManager.Builder<Block, BlockState> builder) {
        appendProperties(builder);
    }
}