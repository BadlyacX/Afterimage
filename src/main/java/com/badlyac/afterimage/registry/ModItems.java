package com.badlyac.afterimage.registry;

import com.badlyac.afterimage.AfterimageMod;
import com.badlyac.afterimage.item.AfterimageAnchorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, AfterimageMod.MOD_ID);

    public static final RegistryObject<Item> AFTERIMAGE_ANCHOR = ITEMS.register(
            "afterimage_anchor",
            () -> new AfterimageAnchorItem(new Item.Properties().stacksTo(1))
    );
}
