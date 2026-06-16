package com.badlyac.afterimage.dimension.palemimic;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.registry.ModDimensions;
import com.badlyac.afterimage.util.AfterimageTeleportUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public final class PaleMimicPlainDoorHandler {

    private static final int CX = PaleMimicPlainWorldSetup.SPAWN.getX();
    private static final int CY = PaleMimicPlainWorldSetup.SPAWN.getY();
    private static final int CZ = PaleMimicPlainWorldSetup.SPAWN.getZ();
    private static final int D  = PaleMimicPlainWorldSetup.DOOR_DISTANCE;

    private static final List<BlockPos> DOOR_POSITIONS = List.of(
            new BlockPos(CX,     CY, CZ - D),  // 北
            new BlockPos(CX,     CY, CZ + D),  // 南
            new BlockPos(CX - D, CY, CZ),       // 西
            new BlockPos(CX + D, CY, CZ)        // 東
    );

    private static final List<ResourceKey<Level>> DIMENSIONS = List.of(
            Level.NETHER,
            Level.OVERWORLD,
            Level.END,
            ModDimensions.TORN_EXPANSE_LEVEL
    );

    private static final Map<BlockPos, ResourceKey<Level>> DOOR_DESTINATIONS = new HashMap<>();

    static {
        reassignDoors();
    }

    public static Map<BlockPos, ResourceKey<Level>> getDoorDestinations() {
        return Collections.unmodifiableMap(DOOR_DESTINATIONS);
    }

    private static void reassignDoors() {
        List<ResourceKey<Level>> shuffled = new ArrayList<>(DIMENSIONS);
        Collections.shuffle(shuffled);
        for (int i = 0; i < DOOR_POSITIONS.size(); i++) {
            DOOR_DESTINATIONS.put(DOOR_POSITIONS.get(i), shuffled.get(i));
        }
    }

    @SubscribeEvent
    public static void onUseBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().dimension() != ModDimensions.PALE_MIMIC_PLAIN_LEVEL) return;

        BlockPos clickedPos = event.getPos();
        BlockState state = player.level().getBlockState(clickedPos);
        if (!(state.getBlock() instanceof DoorBlock)) return;

        BlockPos lowerPos = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER
                ? clickedPos.below()
                : clickedPos;

        ResourceKey<Level> destination = DOOR_DESTINATIONS.get(lowerPos);
        if (destination == null) return;

        event.setCanceled(true);
        AfterimageTeleportUtil.teleportThroughDoor(player, destination);
        reassignDoors();
    }
}
