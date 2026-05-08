package com.badlyac.afterimage.registry;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.monster.palemimic.PaleMimicEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AfterimageMod.MOD_ID);

    public static final RegistryObject<EntityType<PaleMimicEntity>> PALE_MIMIC =
            ENTITIES.register("pale_mimic",
                    () -> EntityType.Builder
                            .<PaleMimicEntity>of(PaleMimicEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(8)
                            .build("pale_mimic")
            );
}
