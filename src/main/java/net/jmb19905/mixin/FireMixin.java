package net.jmb19905.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.jmb19905.Carbonize;
import net.jmb19905.block.AshBlock;
import net.jmb19905.block.FireAccess;
import net.jmb19905.block.ModularFireBlock;
import net.jmb19905.charcoal_pit.FireType;
import net.jmb19905.recipe.BurnRecipe;
import net.jmb19905.util.BlockHelper;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.jmb19905.core.CarbonCore.CONFIG;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(FireBlock.class)
public class FireMixin extends AbstractFireMixin implements FireAccess {
    @Shadow
    private void trySpreadingFire(World world, BlockPos pos, int spreadFactor, Random random, int currentAge) {
    }

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
    private static void registerSoulFire(CallbackInfo ci) {
        ModularFireBlock.registerDefaultFlammables();
    }

    @Redirect(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FireBlock;trySpreadingFire(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/util/math/random/Random;I)V"))
    private void spreadFireAggressively(FireBlock fire, World world, BlockPos newPos, @SuppressWarnings("ParameterCanBeLocal") int spreadFactor, Random random, int currentAge, @Local(argsOnly = true) BlockPos pos, @Local(name = "k") int bonus) {
        spreadFactor = getGlobalSpreadFactor() + bonus;
        this.trySpreadingFire(world, newPos, spreadFactor + (pos.subtract(newPos).getY() > 0 ? -25 : 25), random, currentAge);
        if (!CONFIG.increasedFireSpreadRange() && !world.isRaining()) return;

        if (random.nextBoolean()) return;
        var offset = newPos.subtract(pos);
        var diagonal = new BlockPos(1, 1, 1).multiply(offset.getX() + offset.getY() + offset.getZ()).subtract(offset);
        var oppDiagonal = new BlockPos(1, 1, 1)
                .subtract(new Vec3i(offset.getZ(), offset.getX(), offset.getY())
                        .multiply(2)
                        .multiply(offset.getX() + offset.getY() + offset.getZ())
                ).multiply(offset.getX() + offset.getY() + offset.getZ()).subtract(offset);
        this.trySpreadingFire(world, diagonal.add(pos), spreadFactor + (diagonal.getY() > 0 ? -15 : 15), random, currentAge);
        this.trySpreadingFire(world, oppDiagonal.add(pos), spreadFactor + (oppDiagonal.getY() > 0 ? -15 : 15), random, currentAge);

        if (random.nextBoolean()) return;
        offset = offset.multiply(2);
        this.trySpreadingFire(world, offset.add(pos), spreadFactor + (offset.getY() > 0 ? -50 : 50), random, currentAge);
    }

    @Redirect(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean checkPlacementConditions(BlockState state, TagKey<Block> tagKey, @Local(argsOnly = true) ServerWorld view, @Local(argsOnly = true) BlockPos pos) {
        if ((Object) this instanceof ModularFireBlock fireBlock)
            return fireBlock.parentView().isBaseInfiniburn(view, pos);
        else return isBaseInfiniburn(view, pos);
    }

    @Redirect(method = "trySpreadingFire", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private boolean tryCarbonize(World world, BlockPos pos, BlockState state, int flags, @Local(name = "j") int j, @Local(argsOnly = true) Random random) {
        if (random.nextInt(5) == 0 && hasNotProducedBiproduct(world, pos, random))
            return world.setBlockState(pos, getStateWithAge(world, pos, j), Block.NOTIFY_ALL);
        return true;
    }

    @Redirect(method = "trySpreadingFire", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
    private boolean tweakFireSpreadChance(World world, BlockPos pos, boolean move) {
        var speed = getGlobalSpreadChance();
        if (world.random.nextFloat() <= speed)
            return world.setBlockState(pos, this.getStateWithAge(world, pos, 0), Block.NOTIFY_ALL);
        else return world.removeBlock(pos, move);
    }


    @Unique
    private boolean hasNotProducedBiproduct(World world, BlockPos pos, Random random) {
        BlockState state = world.getBlockState(pos);
        world.getRecipeManager().listAllOfType(Carbonize.BURN_RECIPE_TYPE);
        if (CONFIG.burnCrafting()) {
            for (BurnRecipe burnRecipe : world.getRecipeManager().listAllOfType(Carbonize.BURN_RECIPE_TYPE)) {
                if (state.isIn(burnRecipe.input()) && burnRecipe.fireType().equals(FireType.find(this).orElseThrow())) {
                    float surfaceExposurePercentage = getExposedSurfacePercentage(world, pos);
                    float randomFloat = random.nextFloat();
                    if (randomFloat > surfaceExposurePercentage) {
                        world.setBlockState(pos, BlockHelper.transferState(burnRecipe.result().getDefaultState(), state));
                        return false;
                    }
                }
            }
        }
        if (random.nextFloat() > 0.3f && CONFIG.createAsh()) {
            world.setBlockState(pos, Carbonize.ASH_LAYER.getDefaultState().with(AshBlock.LAYERS, getAshLayerCount(random, state)));
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
            block = fireBlock.getType().asBlock();
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
            block = fireBlock.getType().asBlock();
            i = (int) Math.ceil(i * getLifeSpeedModifier());
        }
        world.scheduleBlockTick(pos, block, i);
    }

    @Redirect(method = "onBlockAdded", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;scheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V"))
    protected void override$onBlockAdded(World world, BlockPos pos, Block block, int i) {
        if ((Object) this instanceof ModularFireBlock fireBlock) {
            block = fireBlock.getType().asBlock();
            i = (int) Math.ceil(i * getLifeSpeedModifier());
        }
        world.scheduleBlockTick(pos, block, i);
    }

    @Redirect(method = "getStateForPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FireBlock;getDefaultState()Lnet/minecraft/block/BlockState;"))
    protected BlockState override$getDefaultState(FireBlock block) {
        if ((Object) this instanceof ModularFireBlock fireBlock)
            return fireBlock.getType().asBlock().getDefaultState();
        return block.getDefaultState();
    }

    @Override
    public void registerFlammable(Block block, int burnChance, int spreadChance) {
        registerFlammableBlock(block, burnChance, spreadChance);
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
    public float getGlobalSpreadChance() {
        return (Object)this instanceof ModularFireBlock fireBlock ? fireBlock.getType().getGlobalSpreadChance() : 0.25F;
    }

    @Override
    public int getGlobalSpreadFactor() {
        return (Object)this instanceof ModularFireBlock fireBlock ? fireBlock.getType().getGlobalSpreadFactor() : 400;
    }

    @Override
    public double getLifeSpeedModifier() {
        return (Object)this instanceof ModularFireBlock fireBlock ? fireBlock.getType().getLifeSpeedModifier() : 1;
    }

    @Override
    public FireType asFireType() {
        return (Object)this instanceof ModularFireBlock fireBlock ? fireBlock.getType() : FireType.DEFAULT_FIRE_TYPE;
    }

    @Override
    public AbstractFireBlock asBlock() {
        return (AbstractFireBlock) (Object) this;
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