package com.badlyac.afterimage;

import com.badlyac.afterimage.network.AfterimageNetwork;
import com.badlyac.afterimage.registry.ModCreativeTabs;
import com.badlyac.afterimage.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(AfterimageMod.MOD_ID)
public class AfterimageMod {
    public static final String MOD_ID = "afterimage";
    private static final Logger LOGGER = LogUtils.getLogger();

    public AfterimageMod(FMLJavaModLoadingContext context) {
        IEventBus bus = context.getModEventBus();

        AfterimageNetwork.register();

        ModItems.ITEMS.register(bus);
        ModCreativeTabs.TABS.register(bus);
    }

}
