package com.badlyac.afterimage.data.worldgen;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.data.levelgen.placement.GridPlacement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.HeightmapPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.List;

public class BirchPlacement {

    private static final int SPACING = 32;

    public static final ResourceKey<ConfiguredFeature<?, ?>> FIXED_BIRCH_CONFIG_KEY = ResourceKey.create(
            Registries.CONFIGURED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(AfterimageMod.MOD_ID, "fixed_birch")
    );

    public static final ResourceKey<PlacedFeature> GRID_BIRCH_KEY = ResourceKey.create(
            Registries.PLACED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(AfterimageMod.MOD_ID, "grid_birch")
    );

    public static void bootstrap(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        Holder<ConfiguredFeature<?, ?>> fixedBirch = configuredFeatures.getOrThrow(FIXED_BIRCH_CONFIG_KEY);

        List<PlacementModifier> modifiers = List.of(
                new GridPlacement(SPACING),
                HeightmapPlacement.onHeightmap(Heightmap.Types.OCEAN_FLOOR),
                BiomeFilter.biome(),
                PlacementUtils.filteredByBlockSurvival(Blocks.BIRCH_SAPLING)
        );

        PlacementUtils.register(context, GRID_BIRCH_KEY, fixedBirch, modifiers);
    }

    public static void bootstrapConfigured(BootstapContext<ConfiguredFeature<?, ?>> context) {
        context.register(FIXED_BIRCH_CONFIG_KEY, new ConfiguredFeature<>(
                com.badlyac.afterimage.registry.ModFeatures.FIXED_BIRCH.get(),
                NoneFeatureConfiguration.INSTANCE
        ));
    }
}
