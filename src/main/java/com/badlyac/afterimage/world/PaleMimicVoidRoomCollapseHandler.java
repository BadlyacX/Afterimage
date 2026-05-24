package com.badlyac.afterimage.world;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.registry.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public final class PaleMimicVoidRoomCollapseHandler {
    private static final int START_DELAY_TICKS = 40;
    private static final int BREAK_INTERVAL_TICKS = 3;
    private static final int EFFECT_DURATION_TICKS = 80;

    private static final List<Collapse> COLLAPSES = new ArrayList<>();

    private PaleMimicVoidRoomCollapseHandler() {
    }

    public static void start(BlockPos center) {
        COLLAPSES.add(new Collapse(center.immutable()));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        ServerLevel voidLevel = event.getServer().getLevel(ModDimensions.PALE_MIMIC_VOID_LEVEL);
        if (voidLevel != null) {
            applyVoidEffects(voidLevel);
        }

        Iterator<Collapse> iterator = COLLAPSES.iterator();
        while (iterator.hasNext()) {
            Collapse collapse = iterator.next();

            if (voidLevel == null || collapse.tick(voidLevel)) {
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (isPaleMimicVoid(event.getLevel())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player && isPaleMimicVoid(event.getLevel())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockMultiPlace(BlockEvent.EntityMultiPlaceEvent event) {
        if (event.getEntity() instanceof Player && isPaleMimicVoid(event.getLevel())) {
            event.setCanceled(true);
        }
    }

    private static boolean isPaleMimicVoid(net.minecraft.world.level.LevelAccessor level) {
        return level instanceof Level actualLevel
                && actualLevel.dimension() == ModDimensions.PALE_MIMIC_VOID_LEVEL;
    }

    private static void applyVoidEffects(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.DARKNESS,
                    EFFECT_DURATION_TICKS,
                    0,
                    false,
                    false,
                    false
            ));
            player.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    EFFECT_DURATION_TICKS,
                    2,
                    false,
                    false,
                    false
            ));
            player.addEffect(new MobEffectInstance(
                    MobEffects.DIG_SLOWDOWN,
                    EFFECT_DURATION_TICKS,
                    2,
                    false,
                    false,
                    false
            ));
        }
    }

    private static final class Collapse {
        private final List<BlockPos> blocks;
        private int ticks;
        private int index;

        private Collapse(BlockPos center) {
            this.blocks = createBreakOrder(center);
        }

        private boolean tick(ServerLevel level) {
            ticks++;

            if (ticks < START_DELAY_TICKS) {
                return false;
            }

            if ((ticks - START_DELAY_TICKS) % BREAK_INTERVAL_TICKS != 0) {
                return false;
            }

            if (index >= blocks.size()) {
                return true;
            }

            BlockPos pos = blocks.get(index++);
            BlockState state = level.getBlockState(pos);

            if (state.is(Blocks.CRYING_OBSIDIAN)) {
                level.levelEvent(2001, pos, Block.getId(state));
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }

            return index >= blocks.size();
        }

        private static List<BlockPos> createBreakOrder(BlockPos center) {
            List<BlockPos> result = new ArrayList<>();

            for (int x = -2; x <= 2; x++) {
                for (int y = -2; y <= 2; y++) {
                    for (int z = -2; z <= 2; z++) {
                        boolean shell = Math.abs(x) == 2 || Math.abs(y) == 2 || Math.abs(z) == 2;
                        if (shell) {
                            result.add(center.offset(x, y, z));
                        }
                    }
                }
            }

            result.sort(Comparator
                    .comparingInt((BlockPos pos) -> pos.getY() == center.getY() - 2 ? 1 : 0)
                    .thenComparingInt(pos -> Math.abs(pos.getX() - center.getX()) + Math.abs(pos.getZ() - center.getZ()))
                    .thenComparingInt(BlockPos::getY));

            return result;
        }
    }
}
