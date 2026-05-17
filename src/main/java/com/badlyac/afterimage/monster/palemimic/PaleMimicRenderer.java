package com.badlyac.afterimage.monster.palemimic;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PaleMimicRenderer extends MobRenderer<PaleMimicEntity, PlayerModel<PaleMimicEntity>> {

    public PaleMimicRenderer(EntityRendererProvider.Context context) {
        super(
                context,
                new PlayerModel<>(
                        context.bakeLayer(ModelLayers.PLAYER_SLIM),
                        true
                ),
                0.5F
        );
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(PaleMimicEntity entity) {

        UUID uuid = entity.getUUID();

        AbstractClientPlayer player =
                (AbstractClientPlayer) Minecraft.getInstance().level.
                        getPlayerByUUID(uuid);

        if (player != null) return player.getSkinTextureLocation();

        GameProfile profile = new GameProfile(uuid, "pale_mimic");

        return Minecraft.getInstance()
                .getSkinManager()
                .getInsecureSkinLocation(profile);
    }
}
