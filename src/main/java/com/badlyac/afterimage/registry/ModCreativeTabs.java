package com.badlyac.afterimage.registry;

import com.badlyac.afterimage.AfterimageMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AfterimageMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> AFTERIMAGE_TAB = TABS.register(
            "afterimage",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.AFTERIMAGE_ANCHOR.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.AFTERIMAGE_ANCHOR.get());
                    })
                    .title(net.minecraft.network.chat.Component.literal("Afterimage"))
                    .build()
    );
}

