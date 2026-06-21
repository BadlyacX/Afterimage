package com.badlyac.afterimage.fortester;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.dimension.palemimic.PaleMimicPlainDoorHandler;
import com.badlyac.afterimage.registry.registries.ModDimensions;
import com.badlyac.afterimage.util.AfterimageTeleportUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

/**
 * 僅供測試用：此資料夾內的方法不得被資料夾外的類別引用，
 * 也不得干涉資料夾外的類別行為（只允許單向讀取，如 getDoorDestinations()）。
 */
@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public final class ForTesterCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("enterTornExpanse")
                .requires(source -> source.hasPermission(2))
                .executes(ForTesterCommands::enterTornExpanse));

        dispatcher.register(Commands.literal("enterNostalgia")
                .requires(source -> source.hasPermission(2))
                .executes(ForTesterCommands::enterNostalgia));

        dispatcher.register(Commands.literal("getEachDoor")
                .requires(source -> source.hasPermission(2))
                .executes(ForTesterCommands::getEachDoor));
    }

    private static int enterTornExpanse(CommandContext<CommandSourceStack> context) {
        ServerPlayer player;
        try {
            player = context.getSource().getPlayerOrException();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("[ForTester] This command should be executed by player!"));
            return 0;
        }

        AfterimageTeleportUtil.teleportThroughDoor(player, ModDimensions.TORN_EXPANSE_LEVEL);
        context.getSource().sendSuccess(() -> Component.literal("[ForTester] You have been teleported to torn_expanse。"), false);
        return 1;
    }

    private static int enterNostalgia(CommandContext<CommandSourceStack> context) {
        ServerPlayer player;
        try {
            player = context.getSource().getPlayerOrException();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("[ForTester] This command should be executed by player!"));
            return 0;
        }

        AfterimageTeleportUtil.teleportThroughDoor(player, ModDimensions.NOSTALGIA_LEVEL);
        context.getSource().sendSuccess(() -> Component.literal("[ForTester] You have been teleported to nostalgia."), false);
        return 1;
    }

    private static int getEachDoor(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Map<BlockPos, ResourceKey<Level>> doors = PaleMimicPlainDoorHandler.getDoorDestinations();

        if (doors.isEmpty()) {
            source.sendSuccess(() -> Component.literal("[ForTester] No doors have been generated yet"), false);
            return 0;
        }

        doors.forEach((pos, dimension) ->
                source.sendSuccess(() -> Component.literal(
                        "[ForTester] Door: " + "(" + pos.toShortString() + ")" +" -> " + dimension.location()
                ), false)
        );

        return doors.size();
    }
}
