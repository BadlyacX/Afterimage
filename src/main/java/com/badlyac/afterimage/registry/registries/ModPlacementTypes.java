package com.badlyac.afterimage.registry.registries;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.data.levelgen.placement.GridPlacement;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModPlacementTypes {

    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, AfterimageMod.MOD_ID);

    public static final RegistryObject<PlacementModifierType<GridPlacement>> GRID =
            PLACEMENT_MODIFIERS.register("grid",
                    () -> () -> GridPlacement.CODEC
            );
}

