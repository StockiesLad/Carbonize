package net.jmb19905.multiblock;

import net.jmb19905.core.CarbonizeCommon;
import net.jmb19905.api.FireType;
import net.jmb19905.block.charring.CharringWoodBlock;
import net.jmb19905.block.charring.CharringWoodBlockEntity;
import net.jmb19905.util.BlockHelper;
import net.jmb19905.util.BlockPosWrapper;
import net.jmb19905.util.ObjectHolder;
import net.jmb19905.util.queue.Queuer;
import net.jmb19905.util.queue.TaskManager;
import net.jmb19905.util.queue.WrappedQueuer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.TntBlock;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static net.jmb19905.core.CharcoalPitInit.CHARRING_WOOD_TYPE;
import static net.jmb19905.block.charring.CharringWoodBlock.STAGE;
import static net.jmb19905.block.charring.CharringWoodBlock.Stage.*;

/**
 * This class is practically just a giant block entity. So just treat it as one. {@link  CharcoalPitManager} will handle everything else.
 * Every {@link CharringWoodBlockEntity} must always try to upload its data to here for everything to work properly. This multi-block
 * must be the only one to either tryExecute a task directly or delegate it to its block entities.
 *
 * <p></p> NOTE: The world is accessed via the handler.
 *
 */
public class CharcoalPitMultiblock implements WrappedQueuer<CharcoalPitMultiblock> {
    public static final Map<Direction, BooleanProperty> DIRECTION_PROPERTIES = ConnectingBlock.FACING_PROPERTIES.entrySet().stream().filter(entry -> entry.getKey() != Direction.DOWN).collect(Util.toMap());
    public static final BlockState FIRE_STATE = Blocks.FIRE.getDefaultState();
    public static final int SINGLE_BURN_TIME = 200;

    private final Queuer<CharcoalPitMultiblock> worker;
    private final CharcoalPitManager pitManager;
    private final List<BlockPosWrapper> blockPositions;
    private final List<ChunkPos> chunkPositions;
    private final FireType fireType;
    private int maxBurnTime;
    private int burnTime;
    private boolean extinguished;

    public CharcoalPitMultiblock(
            @Nullable CharcoalPitManager charcoalPitManager,
            List<BlockPos> blockPositions,
            FireType fireType,
            int maxBurnTime,
            int burnTime,
            boolean extinguished
    ) {
        this.pitManager = charcoalPitManager;
        this.fireType = fireType;
        this.worker = new TaskManager<>();
        this.blockPositions = new LinkedList<>(blockPositions.stream().map(BlockPosWrapper::new).toList());
        this.chunkPositions = new LinkedList<>();
        this.maxBurnTime = maxBurnTime;
        this.burnTime = burnTime;
        this.extinguished = extinguished;
    }

    public CharcoalPitMultiblock(CharcoalPitManager charcoalPitManager, BlockPos pos, FireType fireType, int maxBurnTime, int burnTime, boolean extinguished) {
        this(charcoalPitManager, List.of(pos), fireType, maxBurnTime, burnTime, extinguished);
    }

    public CharcoalPitMultiblock(Info info) {
        this(info.charcoalPitManager(), info.blockPositions(), info.fireType(), info.maxBurnTime(), info.burnTime(), info.extinguished());
    }

    public static CharcoalPitMultiblock def() {
        return new CharcoalPitMultiblock(null, new BlockPos(0, 0, 0), FireType.DEFAULT_FIRE_TYPE, SINGLE_BURN_TIME, 0, false);
    }

    /**
     * Ticks are managed prescribed by {@link CharcoalPitManager#tick()}. This is where {@link Queuer} executes queued tasks.
     */
    public void tick() {
        burnTime++;

        testSides();

        executeQueue(this);

        if (burnTime == maxBurnTime / 6)
            update();
        else if (burnTime == maxBurnTime * 4/6)
            update();
        else if (burnTime >= maxBurnTime)
            update();
        else if (blockPositions.size() == 1)
            update();

        testUnity();

    }

    /**
     * Ensures ALL the chunks are tickable. Prevents unnecessary ticking & syncs the entire charcoal pit.
     * <p></p> This is useful for preventing:
     * <p> 1 - Charcoal pits falling out of sync on chunk borders.
     * <p> 2. - Charcoal pits ticking, setting blockStates and other things it SHOULD NOT BE DOING if its chunks aren't loaded.
     */
    public boolean canTick() {
        return this.getWorld() != null && chunkPositions.stream().allMatch(getWorld()::shouldTick);
    }

    public ServerWorld getWorld() {
        return pitManager.world;
    }

    public CharringWoodBlock.Stage getStage() {
        if (burnTime >= maxBurnTime * 4/6)
            return CHARRING;
        else if (burnTime >= maxBurnTime / 6)
            return BURNING;
        else return IGNITING;
    }

    public List<BlockPosWrapper> positions() {
        return blockPositions;
    }

    public FireType fireType() {
        return fireType;
    }

    public int maxBurnTime() {
        return maxBurnTime;
    }

    public int burnTime() {
        return burnTime;
    }

    public boolean extinguished() {
        return extinguished;
    }

    public int getBlockCount() {
        return blockPositions.size();
    }

    public int getRemainingBurnTime() {
        return maxBurnTime - burnTime;
    }

    public void invalidate() {
        this.blockPositions.clear();
        this.chunkPositions.clear();
        this.burnTime = maxBurnTime + 1;
    }

    public boolean isInvalidated() {
        return getBlockCount() == 0 || getRemainingBurnTime() < 0;
    }

    public boolean hasPosition(BlockPos pos) {
        return hasPosition(new BlockPosWrapper(pos));
    }

    public boolean hasPosition(BlockPosWrapper pos) {
        return blockPositions.stream().anyMatch(blockPosition -> blockPosition.equals(pos));
    }

    public void add(BlockPosWrapper pos) {
        if (blockPositions.stream().noneMatch(blockPosition -> blockPosition.equals(pos)))
            addPosition(pos);
    }

    public void add(List<BlockPosWrapper> wrappers) {
        wrappers.forEach(this::add);
    }

    public void consume(CharcoalPitMultiblock data) {
        add(data.blockPositions);
        var totalBlockCount = getBlockCount();
        this.maxBurnTime = calculateBurnTime(this.maxBurnTime, data.maxBurnTime, totalBlockCount);
        this.burnTime =  calculateBurnTime(this.burnTime, data.burnTime, totalBlockCount);
        this.extinguished = this.extinguished || data.extinguished;
        data.invalidate();
        queueUpdate();
    }

    public void queueUpdate() {
        queue(this::update);
    }

    @Override
    public Queuer<CharcoalPitMultiblock> getQueuer() {
        return worker;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (super.equals(obj)) return true;
        if (obj instanceof CharcoalPitMultiblock data) {
            return  this.blockPositions.equals(data.blockPositions) &&
                    this.maxBurnTime == data.maxBurnTime &&
                    this.burnTime == data.burnTime &&
                    this.extinguished == data.extinguished;
        }

        return false;
    }


    private void testUnity() {
        var blockCount = getBlockCount();

        if (blockCount <= 1) return;

        var testedPositions = collectExisting(getWorld(), blockPositions.get(0).expose(), fireType);
        var testedBlockCount = testedPositions.size();

        if (testedBlockCount >= blockCount) return;

        var remainingBlockCount = getBlockCount() - testedBlockCount;
        double testedRatio = (double) testedBlockCount / blockCount;
        double remainingRatio = (double) remainingBlockCount / blockCount;

        var newMultiBlock = new CharcoalPitMultiblock(
                pitManager,
                testedPositions,
                fireType,
                (int) (maxBurnTime * testedRatio),
                (int) (burnTime * testedRatio),
                extinguished
        );



        pitManager.add(newMultiBlock);

        testedPositions.forEach(pos -> {
            removePosition(new BlockPosWrapper(pos));
            getWorld().getBlockEntity(pos, CHARRING_WOOD_TYPE).ifPresent(entity -> entity.queue(() -> entity.replaceData(newMultiBlock)));
        });

        this.maxBurnTime = (int) (maxBurnTime * remainingRatio);
        this.burnTime = (int) (burnTime * remainingRatio);

    }

    private void testSides() {
        accessMembers(ctx -> {
            var pos = ctx.pos();
            var state = ctx.state();
            var entity = ctx.entity();
            for (var dir : Direction.values())
                testSide(pos, dir, state, entity);
        });
    }

    private void testSide(BlockPos pos, Direction dir, BlockState ignoredState, CharringWoodBlockEntity entity) {
        var sidePos = pos.offset(dir);
        var sideState = getWorld().getBlockState(sidePos);
        // Checks if a block is not flammable and a full cube to determine if it's a valid wall; replaces 'charcoal_pile_valid_wall'.
        if (BlockHelper.isNonFlammableFullCube(getWorld(), pos, sideState, fireType)) return;

        // Terminate loop if the side pos is already a member of this multi-block - avoids unnecessary checks. One of the multi-block-exclusive optimisations.
        if (hasPosition(sidePos)) return;

        if (sideState.isOf(fireType.charringBlock)) {
            queue(() -> pitManager.merge(pos, sidePos));
        } else if (sideState.isIn(CarbonizeCommon.CHARCOAL_PILE_VALID_FUEL)) {
            if (getWorld().random.nextInt(10) != 0) return;
            var parent = getWorld().getBlockState(sidePos);
            //This is an example of a delegation. I have to do this to sync the models properly and I have no idea why, but it just works.
            entity.queue(() -> {
                getWorld().setBlockState(sidePos, fireType.charringBlock.getDefaultState());
                getWorld().getBlockEntity(sidePos, CHARRING_WOOD_TYPE).ifPresent(blockEntity -> blockEntity.sync(parent));
            });
        } else if (entity.getCachedState().get(STAGE).ordinal() > IGNITING.ordinal()) {
            if (sideState.isReplaceable() || sideState.isAir()) {
                BlockState fireState;
                if (dir != Direction.UP)
                    fireState = FIRE_STATE.with(DIRECTION_PROPERTIES.get(dir.getOpposite()), true);
                else fireState = FIRE_STATE;
                getWorld().setBlockState(sidePos, fireState);
            } else if (getWorld().getRandom().nextInt(100) == 0) {
                if (sideState.isOf(Blocks.TNT)) {
                    TntBlock.primeTnt(getWorld(), sidePos);
                    getWorld().setBlockState(pos, Blocks.AIR.getDefaultState());
                } else if (!sideState.isFullCube(getWorld(), pos) && !sideState.isIn(BlockTags.FIRE)) {
                    getWorld().breakBlock(pos, false);
                    getWorld().setBlockState(pos, FIRE_STATE);
                }
            }
        }
    }

    private void update() {
        if (burnTime >= maxBurnTime) {
            accessMembers(ctx -> getWorld().setBlockState(ctx.pos(), ctx.entity().getFinal()));
            if (!extinguished)
                if (!blockPositions.isEmpty()) {
                    getWorld().playSound(null, blockPositions.get(getWorld().getRandom().nextInt(blockPositions.size())).expose(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 2f, 1f);
                    extinguished = true;
                }
        } else if (burnTime >= maxBurnTime * 2/3)
            updateStates();
        else if (burnTime >= maxBurnTime / 3)
            updateStates();
        else updateStates();
    }

    private void updateStates() {
        accessMembers(ctx -> {
            var stage = getStage();
            if (!ctx.state().get(STAGE).equals(stage)) {
                getWorld().setBlockState(ctx.pos(), ctx.state().with(STAGE, stage));
                ctx.entity().update();
            }
        });
    }

    private void accessMembers(Consumer<CharcoalPitContext> consumer) {
        for (BlockPosWrapper blockPosition : blockPositions) {
            var exposedPos = blockPosition.expose();
            var state = getWorld().getBlockState(exposedPos);
            if (state.isOf(fireType.charringBlock)) {
                var entity = getWorld().getBlockEntity(exposedPos, CHARRING_WOOD_TYPE);
                consumer.accept(new CharcoalPitContext(exposedPos, state, entity.orElseThrow()));
            } else {
                var blockCount = getBlockCount();
                this.burnTime = this.burnTime * (blockCount - 1) / blockCount;
                this.maxBurnTime = this.maxBurnTime * (blockCount - 1) / blockCount;
                queue(() -> removePosition(blockPosition));
            }
        }
    }

    private void addPosition(BlockPosWrapper pos) {
        var chunkPos = new ChunkPos(pos.expose());
        if (chunkPositions.stream().noneMatch(chunkPosition -> chunkPosition.equals(chunkPos)))
            chunkPositions.add(chunkPos);
        blockPositions.add(pos);

    }

    private void removePosition(BlockPosWrapper posWrapper) {
        var chunkPos = new ChunkPos(posWrapper.expose());
        blockPositions.remove(posWrapper);
        if (blockPositions.stream().noneMatch(pos -> chunkPos.equals(new ChunkPos(pos.expose()))))
            chunkPositions.remove(chunkPos);
    }

    public static List<BlockPos> collectFuels(ServerWorld world, BlockPos startingPos, Direction directionFrom, FireType fireType) {
        List<BlockPos> alreadyChecked = new ArrayList<>();
        alreadyChecked.add(startingPos.offset(directionFrom));
        return collect(world, startingPos, new ObjectHolder<>(new ArrayList<>()), alreadyChecked, false, fireType).getValue();
    }

    public static List<BlockPos> collectExisting(ServerWorld world, BlockPos startingPos, FireType fireType) {
        return collect(world, startingPos, new ObjectHolder<>(new ArrayList<>()), new ArrayList<>(), true, fireType).getValue();
    }

    private static ObjectHolder<List<BlockPos>> collect(ServerWorld world, BlockPos pos,
            ObjectHolder<List<BlockPos>> parsedPositions, List<BlockPos> checkedPositions, boolean checkExisting, FireType fireType) {
        if (checkedPositions.contains(pos)) return parsedPositions;
        if (parsedPositions.isLocked()) return parsedPositions;

        checkedPositions.add(pos);
        BlockState state = world.getBlockState(pos);

        if (BlockHelper.isNonFlammableFullCube(world, pos, state, fireType))
             return parsedPositions;

        if (!checkExisting && !state.isIn(CarbonizeCommon.CHARCOAL_PILE_VALID_FUEL))
            return parsedPositions;

        if (checkExisting && !state.isOf(fireType.charringBlock))
            return parsedPositions;

        parsedPositions.getValue().add(pos);

        for (Direction direction : Direction.values()) {
                collect(world, pos.offset(direction), parsedPositions, checkedPositions, checkExisting, fireType);
            if (parsedPositions.isLocked())
                break;
        }

        return parsedPositions;
    }

    private static int calculateBurnTime(int firstBurnTime, int secondBurnTime, int totalBlockCount) {
        return (int) (Math.max(firstBurnTime, secondBurnTime) + Math.min(firstBurnTime, secondBurnTime) / Math.cbrt(Math.min(totalBlockCount, 1)));
    }

    public record Info(@Nullable CharcoalPitManager charcoalPitManager, List<BlockPos> blockPositions,
                       FireType fireType, int maxBurnTime, int burnTime, boolean extinguished) {
        public Info(@Nullable CharcoalPitManager charcoalPitManager, BlockPos blockPosition,
                    FireType fireType, int maxBurnTime, int burnTime, boolean extinguished) {
            this(charcoalPitManager, new ArrayList<>(Collections.singletonList(blockPosition)), fireType, maxBurnTime, burnTime, extinguished);
        }
    }

    private record CharcoalPitContext(BlockPos pos, BlockState state, CharringWoodBlockEntity entity) {}
}