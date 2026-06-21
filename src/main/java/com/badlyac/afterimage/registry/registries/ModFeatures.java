package com.badlyac.afterimage.registry.registries;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.data.worldgen.feature.FixedBirchFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, AfterimageMod.MOD_ID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> FIXED_BIRCH =
            FEATURES.register("fixed_birch",
                    () -> new FixedBirchFeature(NoneFeatureConfiguration.CODEC)
            );
}
