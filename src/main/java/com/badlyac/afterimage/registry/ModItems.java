package com.badlyac.afterimage.registry;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.item.AfterimageAnchorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AfterimageMod.MOD_ID);

    public static final RegistryObject<Item> AFTERIMAGE_ANCHOR = ITEMS.register(
            "afterimage_anchor",
            () -> new AfterimageAnchorItem(
                    new Item.Properties().stacksTo(1)
            )
    );

    public static final RegistryObject<Item> PALE_MIMIC_SPAWN_EGG =
            ITEMS.register(
                    "pale_mimic_spawn_egg",
                    () -> new ForgeSpawnEggItem(
                            ModEntities.PALE_MIMIC,
                            0xC0C0C0,
                            0x202020,
                            new Item.Properties()
                    )
            );
}
