package com.badlyac.afterimage.manager;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.util.AfterimagePhaseUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.badlyac.afterimage.state.AfterimageState.*;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public final class AfterimageInteractionManager {

    /* =========================
       Attack player / entity
       ========================= */

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player attacker = event.getEntity();
        Entity target = event.getTarget();

        if (shouldBlock(attacker, target)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) return;

        Entity target = event.getEntity();
        if (shouldBlock(attacker, target)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) return;

        Entity target = event.getEntity();
        if (shouldBlock(attacker, target)) {
            event.setCanceled(true);
        }
    }

    /* =========================
       Pick up item
       ========================= */

    @SubscribeEvent
    public static void onPickupItem(EntityItemPickupEvent event) {
        Player player = event.getEntity();
        Entity entityItem = event.getEntity();
        ItemEntity item = event.getItem();

        if (entityItem.getPersistentData().contains(AfterimagePhaseUtil.KEY)) return;
        if (isInAfterimage(player)) {
            event.setCanceled(true);
        }
    }

    /* =========================
       Right click interaction (Entity)
       ========================= */

    @SubscribeEvent
    public static void onInteractEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();

        if (shouldBlock(player, target)) {
            event.setCanceled(true);
        }
    }

    /* =========================
       Right click interaction (General)
       ========================= */

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();

        if (isInAfterimage(player)) {
            event.setCanceled(true);
        }
    }

    private static boolean shouldBlock(Player player, Entity target) {
        boolean playerAfter = isInAfterimage(player);
        boolean targetAfter = target instanceof Player p && isInAfterimage(p);

        return playerAfter != targetAfter;
    }
}
