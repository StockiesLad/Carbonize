package net.jmb19905.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.jmb19905.Carbonize;
import net.jmb19905.block.AshBlock;
import net.jmb19905.block.GenericFireBlock;
import net.jmb19905.recipe.BurnRecipe;
import net.jmb19905.util.BlockHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public class FireMixin extends AbstractFireMixin {
    @Shadow
    private void trySpreadingFire(World world, BlockPos pos, int spreadFactor, Random random, int currentAge) {
    }

    @Shadow
    private BlockState getStateWithAge(WorldAccess world, BlockPos pos, int age) {
        return null;
    }

    @Inject(method = "registerDefaultFlammables", at = @At("TAIL"))
    private static void registerSoulFire(CallbackInfo ci) {
        GenericFireBlock.registerDefaultFlammables();
    }

    @Redirect(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FireBlock;trySpreadingFire(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/util/math/random/Random;I)V"))
    private void spreadFireAggressively(FireBlock fire, World world, BlockPos newPos, int spreadFactor, Random random, int currentAge, @Local(argsOnly = true) BlockPos pos, @Local(name = "k") int bonus) {
        this.trySpreadingFire(world, newPos, spreadFactor, random, currentAge);
        if (!Carbonize.CONFIG.increasedFireSpreadRange()) return;
        var offset = newPos.subtract(pos);
        var diagonal = new BlockPos(1, 1, 1).multiply(offset.getX() + offset.getY() + offset.getZ()).subtract(offset);
        var oppDiagonal = new BlockPos(1, 1, 1)
                .subtract(new Vec3i(offset.getZ(), offset.getX(), offset.getY())
                        .multiply(2)
                        .multiply(offset.getX() + offset.getY() + offset.getZ())
                ).multiply(offset.getX() + offset.getY() + offset.getZ()).subtract(offset);
        this.trySpreadingFire(world, diagonal.add(pos), (diagonal.getY() == 0 ? 300 : 250) + bonus, random, currentAge);
        this.trySpreadingFire(world, oppDiagonal.add(pos), (diagonal.getY() == 0 ? 300 : 250) + bonus, random, currentAge);
        offset = offset.multiply(2);
        this.trySpreadingFire(world, offset.add(pos), (offset.getY() == 0 ? 300 : 250) + bonus, random, currentAge);
    }

    @Redirect(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean checkDerivativeInfiniburn(BlockState state, TagKey<Block> tagKey) {
        if ((Object) this instanceof GenericFireBlock fireBlock)
            return fireBlock.checkInfiniburn.test(state);
        return state.isIn(tagKey);
    }


    @Redirect(method = "trySpreadingFire", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private boolean tryCarbonize(World world, BlockPos pos, BlockState state, int flags, @Local(name = "j") int j, @Local(argsOnly = true) Random random) {
        if (random.nextInt(10) == 0 && !tryProduceBiproduct(world, pos, random))
            return world.setBlockState(pos, getStateWithAge(world, pos, j), Block.NOTIFY_ALL);
        return true;
    }

    @Redirect(method = "trySpreadingFire", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;removeBlock(Lnet/minecraft/util/math/BlockPos;Z)Z"))
    private boolean tweakFireSpreadChance(World world, BlockPos pos, boolean move) {
        var speed = 1;
        if ((Object) this instanceof GenericFireBlock)
            speed = 2;
        if (world.random.nextInt(3) <= speed)
            return world.setBlockState(pos, this.getStateWithAge(world, pos, 0), Block.NOTIFY_ALL);
        else return world.removeBlock(pos, move);
    }


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Unique
    private boolean tryProduceBiproduct(World world, BlockPos pos, Random random) {
        BlockState state = world.getBlockState(pos);
        world.getRecipeManager().listAllOfType(Carbonize.BURN_RECIPE_TYPE);
        if (Carbonize.CONFIG.burnCrafting()) {
            for (BurnRecipe burnRecipe : world.getRecipeManager().listAllOfType(Carbonize.BURN_RECIPE_TYPE)) {
                if (state.isIn(burnRecipe.input())) {
                    float exposure = getExposed(world, pos);
                    float randomVal = random.nextFloat();
                    if (randomVal > exposure) {
                        world.setBlockState(pos, BlockHelper.transferState(burnRecipe.result().getDefaultState(), state));
                        return true;
                    } else if (randomVal > 0.3f && Carbonize.CONFIG.createAsh()) {
                        world.setBlockState(pos, Carbonize.ASH_LAYER.getDefaultState().with(AshBlock.LAYERS, getAshLayerCount(random, state)));
                        return true;
                    }
                }
            }
        }
        if (random.nextFloat() > 0.3f && Carbonize.CONFIG.createAsh()) {
            world.setBlockState(pos, Carbonize.ASH_LAYER.getDefaultState().with(AshBlock.LAYERS, getAshLayerCount(random, state)));
            return true;
        }
        return false;
    }

    @Unique
    private int getAshLayerCount(Random random, BlockState state) {
        int layers = random.nextInt(2) + 1;
        layers += ((int) state.getBlock().getHardness());
        return MathHelper.clamp(layers, 1, 4);
    }

    @Unique
    private float getExposed(World world, BlockPos pos) {
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
        if ((Object) this instanceof GenericFireBlock fireBlock)
            block = fireBlock.parentSupplier.get();
        return state.isOf(block);
    }

    @Redirect(method = "scheduledTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;scheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V"))
    protected void override$scheduleBlockTick(ServerWorld world, BlockPos pos, Block block, int i) {
        if ((Object) this instanceof GenericFireBlock fireBlock) {
            block = fireBlock.parentSupplier.get();
            i = i / 3;
        }
        world.scheduleBlockTick(pos, block, i);
    }

    @Redirect(method = "onBlockAdded", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;scheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V"))
    protected void override$onBlockAdded(World world, BlockPos pos, Block block, int i) {
        if ((Object) this instanceof GenericFireBlock fireBlock) {
            block = fireBlock.parentSupplier.get();
            i = i / 3;
        }
        world.scheduleBlockTick(pos, block, i);
    }

    @Redirect(method = "getStateForPosition", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/FireBlock;getDefaultState()Lnet/minecraft/block/BlockState;"))
    protected BlockState override$getDefaultState(FireBlock block) {
        if ((Object) this instanceof GenericFireBlock fireBlock)
            return fireBlock.parentSupplier.get().getDefaultState();
        return block.getDefaultState();
    }
}