package com.badlyac.afterimage.registry;

import com.badlyac.afterimage.registry.registries.*;
import net.minecraftforge.eventbus.api.IEventBus;

public class ModRegistry {

    public static void register(IEventBus bus) {
        ModPlacementTypes.PLACEMENT_MODIFIERS.register(bus);
        ModFeatures.FEATURES.register(bus);
        ModItems.ITEMS.register(bus);
        ModCreativeTabs.TABS.register(bus);
        ModSounds.SOUND_EVENTS.register(bus);
        ModEntities.ENTITIES.register(bus);
    }
}
