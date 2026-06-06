package com.badlyac.afterimage.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Consumer;

public final class PostProcessEffectUtil {
    private static ResourceLocation loadedEffect;
    private static Field postChainPassesField;

    private PostProcessEffectUtil() {
    }

    public static void load(ResourceLocation effect) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (effect.equals(loadedEffect)) return;

        loadedEffect = effect;
        mc.execute(() -> mc.gameRenderer.loadEffect(effect));
    }

    public static void disable() {
        if (loadedEffect == null) return;

        loadedEffect = null;
        Minecraft mc = Minecraft.getInstance();
        mc.execute(mc.gameRenderer::shutdownEffect);
    }

    public static boolean isEnabled() {
        return loadedEffect != null;
    }

    public static ResourceLocation getLoadedEffect() {
        return loadedEffect;
    }

    public static void ensureLoadedEffectIsActive() {
        if (loadedEffect == null) return;

        Minecraft mc = Minecraft.getInstance();
        PostChain effect = mc.gameRenderer.currentEffect();
        if (effect == null || !loadedEffect.toString().equals(effect.getName())) {
            mc.gameRenderer.loadEffect(loadedEffect);
        }
    }

    public static void applyToPasses(ResourceLocation expectedEffect, Consumer<PostPass> passConsumer) {
        PostChain effect = Minecraft.getInstance().gameRenderer.currentEffect();
        if (effect == null || !expectedEffect.toString().equals(effect.getName())) return;

        for (PostPass pass : getPasses(effect)) {
            passConsumer.accept(pass);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<PostPass> getPasses(PostChain effect) {
        try {
            if (postChainPassesField == null) {
                postChainPassesField = getPostChainPassesField();
            }

            return (List<PostPass>) postChainPassesField.get(effect);
        } catch (ReflectiveOperationException exception) {
            return List.of();
        }
    }

    private static Field getPostChainPassesField() throws NoSuchFieldException {
        for (String name : List.of("passes", "f_110009_")) {
            try {
                Field field = PostChain.class.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }

        throw new NoSuchFieldException("PostChain passes");
    }
}
