package com.badlyac.afterimage.data.levelgen.placement;

import com.badlyac.afterimage.registry.registries.ModPlacementTypes;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class GridPlacement extends PlacementModifier {

    public static final Codec<GridPlacement> CODEC =
            Codec.intRange(1, 256).fieldOf("spacing")
                    .xmap(GridPlacement::new, p -> p.spacing)
                    .codec();

    private final int spacing;

    public GridPlacement(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public @NotNull Stream<BlockPos> getPositions(@NotNull PlacementContext context, @NotNull RandomSource randomSource, @NotNull BlockPos blockPos) {
        int minX = blockPos.getX();
        int minZ = blockPos.getZ();
        int maxX = minX + 15;
        int maxZ = minZ + 15;

        int firstGridX = firstGridCoordinateAtOrAfter(minX);
        int firstGridZ = firstGridCoordinateAtOrAfter(minZ);

        if (firstGridX > maxX || firstGridZ > maxZ) {
            return Stream.empty();
        }

        Stream.Builder<BlockPos> positions = Stream.builder();

        for (int x = firstGridX; x <= maxX; x += spacing) {
            for (int z = firstGridZ; z <= maxZ; z += spacing) {
                positions.add(new BlockPos(x, blockPos.getY(), z));
            }
        }

        return positions.build();
    }

    private int firstGridCoordinateAtOrAfter(int coordinate) {
        return -Math.floorDiv(-coordinate, spacing) * spacing;
    }

    @Override
    public @NotNull PlacementModifierType<?> type() {
        return ModPlacementTypes.GRID.get();
    }
}
