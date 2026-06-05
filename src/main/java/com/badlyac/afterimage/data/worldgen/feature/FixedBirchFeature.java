package com.badlyac.afterimage.data.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FixedBirchFeature extends Feature<NoneFeatureConfiguration> {

    private static final int TRUNK_HEIGHT = 5;
    private static final BlockState LOG = Blocks.BIRCH_LOG.defaultBlockState();

    public FixedBirchFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();

        Set<BlockPos> logs = createLogPositions(origin);
        List<BlockPos> leaves = createLeafPositions(origin);

        if (!canPlace(level, logs, leaves)) {
            return false;
        }

        setGroundToDirt(level, origin.below());

        for (BlockPos log : logs) {
            setBlock(level, log, LOG);
        }

        for (BlockPos leaf : leaves) {
            if (!logs.contains(leaf)) {
                setBlock(level, leaf, createLeafState(level, leaf, logs));
            }
        }

        return true;
    }

    private static Set<BlockPos> createLogPositions(BlockPos origin) {
        Set<BlockPos> logs = new HashSet<>();

        for (int y = 0; y < TRUNK_HEIGHT; y++) {
            logs.add(origin.above(y));
        }

        return logs;
    }

    private static List<BlockPos> createLeafPositions(BlockPos origin) {
        List<BlockPos> leaves = new ArrayList<>();

        addLeafRow(leaves, origin.above(TRUNK_HEIGHT), 1);
        addLeafRow(leaves, origin.above(TRUNK_HEIGHT - 1), 1);
        addLeafRow(leaves, origin.above(TRUNK_HEIGHT - 2), 2);
        addLeafRow(leaves, origin.above(TRUNK_HEIGHT - 3), 2);

        return leaves;
    }

    private static void addLeafRow(List<BlockPos> leaves, BlockPos center, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (Math.abs(x) == radius && Math.abs(z) == radius) {
                    continue;
                }

                leaves.add(center.offset(x, 0, z));
            }
        }
    }

    private static boolean canPlace(WorldGenLevel level, Set<BlockPos> logs, List<BlockPos> leaves) {
        for (BlockPos log : logs) {
            if (!isInBuildHeight(level, log) || !canReplace(level.getBlockState(log))) {
                return false;
            }
        }

        for (BlockPos leaf : leaves) {
            if (!logs.contains(leaf) && (!isInBuildHeight(level, leaf) || !canReplace(level.getBlockState(leaf)))) {
                return false;
            }
        }

        return true;
    }

    private static boolean isInBuildHeight(WorldGenLevel level, BlockPos pos) {
        return pos.getY() >= level.getMinBuildHeight() && pos.getY() < level.getMaxBuildHeight();
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir() || state.is(BlockTags.REPLACEABLE_BY_TREES);
    }

    private static void setGroundToDirt(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (!state.is(BlockTags.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.MYCELIUM)) {
            level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 19);
        }
    }

    private static BlockState createLeafState(WorldGenLevel level, BlockPos leaf, Set<BlockPos> logs) {
        return Blocks.BIRCH_LEAVES.defaultBlockState()
                .setValue(LeavesBlock.DISTANCE, distanceToNearestLog(leaf, logs))
                .setValue(LeavesBlock.PERSISTENT, false)
                .setValue(LeavesBlock.WATERLOGGED, level.getFluidState(leaf).isSourceOfType(Fluids.WATER));
    }

    private static int distanceToNearestLog(BlockPos leaf, Set<BlockPos> logs) {
        int distance = LeavesBlock.DECAY_DISTANCE;

        for (BlockPos log : logs) {
            distance = Math.min(distance, leaf.distManhattan(log));
        }

        return Math.max(1, distance);
    }
}
