package com.badlyac.afterimage.fortester;

import com.badlyac.afterimage.AfterimageMod;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = AfterimageMod.MOD_ID)
public final class RecordStructureCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("recordStructure")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("pos1", BlockPosArgument.blockPos())
                    .then(Commands.argument("pos2", BlockPosArgument.blockPos())
                        .then(Commands.argument("name", StringArgumentType.word())
                            .executes(RecordStructureCommand::execute))))
        );
    }

    private static int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        ServerLevel level = source.getLevel();

        BlockPos pos1 = BlockPosArgument.getLoadedBlockPos(context, "pos1");
        BlockPos pos2 = BlockPosArgument.getLoadedBlockPos(context, "pos2");

        BlockPos min = new BlockPos(
                Math.min(pos1.getX(), pos2.getX()),
                Math.min(pos1.getY(), pos2.getY()),
                Math.min(pos1.getZ(), pos2.getZ())
        );
        BlockPos max = new BlockPos(
                Math.max(pos1.getX(), pos2.getX()),
                Math.max(pos1.getY(), pos2.getY()),
                Math.max(pos1.getZ(), pos2.getZ())
        );

        Vec3i size = new Vec3i(
                max.getX() - min.getX() + 1,
                max.getY() - min.getY() + 1,
                max.getZ() - min.getZ() + 1
        );

        StructureTemplate template = new StructureTemplate();
        template.fillFromWorld(level, min, size, false, Blocks.STRUCTURE_VOID);

        CompoundTag tag = template.save(new CompoundTag());

        String fileName = StringArgumentType.getString(context, "name");

        Path outputFile;
        try {
            Path structuresDir = server.getWorldPath(LevelResource.GENERATED_DIR)
                    .resolve("afterimage")
                    .resolve("structures");
            Files.createDirectories(structuresDir);
            outputFile = structuresDir.resolve(fileName + ".nbt");
            NbtIo.writeCompressed(tag, outputFile.toFile());
        } catch (IOException e) {
            source.sendFailure(Component.literal("[ForTester] Failed to save structure: " + e.getMessage()));
            return 0;
        }

        int blockCount = size.getX() * size.getY() * size.getZ();
        String relativePath = "generated/afterimage/structures/" + fileName + ".nbt";
        source.sendSuccess(() -> Component.literal(
                "[ForTester] Recorded " + blockCount + " blocks -> " + relativePath
        ), false);
        return blockCount;
    }
}
