package com.badlyac.afterimage.item;

import com.badlyac.afterimage.world.AfterimageTravelHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class AfterimageAnchorItem extends Item {

    private static final int COOLDOWN_TICKS = 8 * 20;

    public AfterimageAnchorItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            @NotNull Level level,
            @NotNull Player player,
            @NotNull InteractionHand hand
    ) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (!serverPlayer.getCooldowns().isOnCooldown(this)) {
                System.out.println("ANCHOR USE CALLED");
                AfterimageTravelHandler.tryToggle(serverPlayer);
                serverPlayer.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
            }
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }
}
