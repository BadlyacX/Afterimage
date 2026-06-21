package com.badlyac.afterimage.registry.registries;

import com.badlyac.afterimage.AfterimageMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(
                    ForgeRegistries.SOUND_EVENTS,
                    AfterimageMod.MOD_ID
            );

    public static final RegistryObject<SoundEvent> ENTER_SOUND =
            SOUND_EVENTS.register(
                    "enter_sound",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(
                                    AfterimageMod.MOD_ID,
                                    "enter_sound"
                            )
                    )
            );

    public static final RegistryObject<SoundEvent> EXIT_SOUND =
            SOUND_EVENTS.register(
                    "exit_sound",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(
                                    AfterimageMod.MOD_ID,
                                    "exit_sound"
                            )
                    )
            );

    public static final RegistryObject<SoundEvent> WHISPERING =
            SOUND_EVENTS.register(
                    "whispering",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(
                                    AfterimageMod.MOD_ID,
                                    "whispering"
                            )
                    )
            );

    public static final RegistryObject<SoundEvent> NECK_BONE_FRACTURE =
            SOUND_EVENTS.register(
                    "neck_bone_fracture",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(
                                    AfterimageMod.MOD_ID,
                                    "neck_bone_fracture"
                            )
                    )
            );

    public static final RegistryObject<SoundEvent> HEART_BEAT =
            SOUND_EVENTS.register(
                    "heart_beat",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(
                                    AfterimageMod.MOD_ID,
                                    "heart_beat"
                            )
                    )
            );

    public static final RegistryObject<SoundEvent> RADIO_STATIC =
            SOUND_EVENTS.register(
                    "radio_static",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(
                                    AfterimageMod.MOD_ID,
                                    "radio_static"
                            )
                    )
            );
}
