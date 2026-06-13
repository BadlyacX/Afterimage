package com.badlyac.afterimage.dimension.palemimic;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public final class PaleMimicPlainWorldSetup {

    static final BlockPos SPAWN = new BlockPos(9, -60, 8);
    static final int BOUNDARY_RADIUS = 200;
    static final int DOOR_DISTANCE = 180;

    @SubscribeEvent
    public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getTo() != ModDimensions.PALE_MIMIC_PLAIN_LEVEL) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        placeDoors(player.serverLevel());
    }

    public static void applyTo(ServerLevel level) {
        placeDoors(level);
    }

    private static void placeDoors(ServerLevel level) {
        int cx = SPAWN.getX();
        int cy = SPAWN.getY();
        int cz = SPAWN.getZ();

        placeDoor(level, new BlockPos(cx,                 cy, cz - DOOR_DISTANCE), Direction.SOUTH); // 北側
        placeDoor(level, new BlockPos(cx,                 cy, cz + DOOR_DISTANCE), Direction.NORTH); // 南側
        placeDoor(level, new BlockPos(cx - DOOR_DISTANCE, cy, cz),                 Direction.EAST);  // 西側
        placeDoor(level, new BlockPos(cx + DOOR_DISTANCE, cy, cz),                 Direction.WEST);  // 東側
    }

    private static void placeDoor(ServerLevel level, BlockPos pos, Direction facing) {
        if (level.getBlockState(pos).getBlock() instanceof DoorBlock) return;

        level.setBlock(pos, Blocks.OAK_DOOR.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER)
                .setValue(BlockStateProperties.OPEN, false)
                .setValue(BlockStateProperties.DOOR_HINGE, DoorHingeSide.LEFT),
                3);

        level.setBlock(pos.above(), Blocks.OAK_DOOR.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER)
                .setValue(BlockStateProperties.OPEN, false)
                .setValue(BlockStateProperties.DOOR_HINGE, DoorHingeSide.LEFT),
                3);
    }
}
