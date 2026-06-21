package com.badlyac.afterimage.fortester;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.util.AfterimageStructureLoader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public final class PlaceSchematicCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("placeSchematic")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .then(Commands.argument("name", StringArgumentType.word())
                        .executes(PlaceSchematicCommand::execute)))
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();

        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        String name = StringArgumentType.getString(context, "name");

        Optional<StructureTemplate> opt = AfterimageStructureLoader.load(level, name);
        if (opt.isEmpty()) {
            source.sendFailure(Component.literal(
                    "[ForTester] Structure '" + name + "' not found in assets/afterimage/schematic/"));
            return 0;
        }

        StructureTemplate template = opt.get();
        boolean success = template.placeInWorld(level, pos, pos, new StructurePlaceSettings(), level.random, 2);

        if (success) {
            source.sendSuccess(() -> Component.literal(
                    "[ForTester] Placed '" + name + "' at (" + pos.toShortString() + "), size: "
                            + template.getSize().getX() + "x" + template.getSize().getY() + "x" + template.getSize().getZ()
            ), false);
            return 1;
        } else {
            source.sendFailure(Component.literal("[ForTester] Failed to place structure '" + name + "'"));
            return 0;
        }
    }
}
