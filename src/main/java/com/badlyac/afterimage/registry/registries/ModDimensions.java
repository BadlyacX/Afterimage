package com.badlyac.afterimage.registry.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ModDimensions {

    public static final ResourceKey<Level> AFTERIMAGE_LEVEL =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath("afterimage", "afterimage"));

    public static final ResourceKey<Level> PALE_MIMIC_PLAIN_LEVEL =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath("afterimage", "pale_mimic_plain"));

    public static final ResourceKey<Level> TORN_EXPANSE_LEVEL =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath("afterimage", "torn_expanse"));

    public static final ResourceKey<Level> NOSTALGIA_LEVEL =
            ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.fromNamespaceAndPath("afterimage", "nostalgia"));

}
