package com.badlyac.afterimage.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class AfterimageStructureLoader {

    private static final String RESOURCE_BASE = "assets/afterimage/schematic/";
    private static final Map<String, StructureTemplate> CACHE = new HashMap<>();

    public static Optional<StructureTemplate> load(ServerLevel level, String name) {
        StructureTemplate cached = CACHE.get(name);
        if (cached != null) return Optional.of(cached);

        String path = RESOURCE_BASE + name + ".nbt";
        try (InputStream stream = AfterimageStructureLoader.class.getClassLoader().getResourceAsStream(path)) {
            if (stream == null) {
                System.err.println("[Afterimage] Structure not found in classpath: " + path);
                return Optional.empty();
            }
            CompoundTag tag = NbtIo.readCompressed(stream);
            StructureTemplate template = level.getStructureManager().readStructure(tag);
            CACHE.put(name, template);
            return Optional.of(template);
        } catch (IOException e) {
            System.err.println("[Afterimage] Failed to load structure '" + name + "': " + e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 載入並放置結構。放置前會檢查 bounding box 內所有位置是否為空氣或可替換方塊；
     * 任何實心方塊佔據位置時中止並回傳 false。
     * 回傳 false 也可能代表結構檔案不存在。
     */
    public static boolean place(ServerLevel level, BlockPos origin, String name, StructurePlaceSettings settings) {
        Optional<StructureTemplate> opt = load(level, name);
        if (opt.isEmpty()) return false;
        StructureTemplate template = opt.get();
        if (!hasEnoughSpace(level, origin, template)) return false;
        return template.placeInWorld(level, origin, origin, settings, level.random, 2);
    }

    /**
     * 載入並直接放置結構，不檢查空間，重疊位置的方塊會被結構內容直接覆蓋。
     * 回傳 false 代表結構檔案不存在或放置失敗。
     */
    public static boolean replace(ServerLevel level, BlockPos origin, String name, StructurePlaceSettings settings) {
        return load(level, name)
                .map(t -> t.placeInWorld(level, origin, origin, settings, level.random, 2))
                .orElse(false);
    }

    /**
     * 檢查 origin 起算、與結構 size 等大的 bounding box 內，
     * 所有位置是否都是空氣或可替換方塊（canBeReplaced）。
     */
    private static boolean hasEnoughSpace(ServerLevel level, BlockPos origin, StructureTemplate template) {
        Vec3i size = template.getSize();
        for (int x = 0; x < size.getX(); x++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int z = 0; z < size.getZ(); z++) {
                    BlockState state = level.getBlockState(origin.offset(x, y, z));
                    if (!state.isAir() && !state.canBeReplaced()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
