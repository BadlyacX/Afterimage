package com.badlyac.afterimage.client.renderer;

import com.badlyac.afterimage.monster.palemimic.PaleMimicEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class PaleMimicRenderer extends MobRenderer<PaleMimicEntity, PlayerModel<PaleMimicEntity>> {

    public PaleMimicRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull PaleMimicEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/steve.png");
    }
}