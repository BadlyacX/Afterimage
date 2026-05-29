package com.badlyac.afterimage.data.levelgen.placement;

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
        int chunkX = blockPos.getX();
        int chunkZ = blockPos.getZ();

        int gridX = Math.floorDiv(chunkX, spacing) * spacing;
        int gridZ = Math.floorDiv(chunkZ, spacing) * spacing;

        return Stream.of(new BlockPos(gridX, blockPos.getY(), gridZ));
    }

    @Override
    public @NotNull PlacementModifierType<?> type() {
        return ModPlacementTypes.GRID.get();
    }
}
