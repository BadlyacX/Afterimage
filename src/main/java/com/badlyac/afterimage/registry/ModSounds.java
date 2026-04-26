package com.badlyac.afterimage.registry;

import com.badlyac.afterimage.AfterimageMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AfterimageMod.MOD_ID);

    public static final RegistryObject<SoundEvent> ENTER_SOUND =
            SOUND_EVENTS.register("enter_sound",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(AfterimageMod.MOD_ID, "enter_sound"))
            );

    public static final RegistryObject<SoundEvent> EXIT_SOUND =
            SOUND_EVENTS.register("exit_sound",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(AfterimageMod.MOD_ID, "exit_sound"))
            );
}