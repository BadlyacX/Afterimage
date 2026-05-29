package com.badlyac.afterimage.monster.palemimic;

import com.badlyac.afterimage.AfterimageMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public class PaleMimicRenderer extends MobRenderer<PaleMimicEntity, PlayerModel<PaleMimicEntity>> {
    private static final ResourceLocation PALE_MIMIC_SKIN = ResourceLocation.fromNamespaceAndPath(
            AfterimageMod.MOD_ID,
            "textures/entity/pale_mimic/palemimic_default_skin.png"
    );

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
        Optional<UUID> disguisePlayerId = entity.getDisguisePlayerId();

        if (disguisePlayerId.isPresent()) {
            ClientPacketListener connection = Minecraft.getInstance().getConnection();

            if (connection != null) {
                PlayerInfo playerInfo = connection.getPlayerInfo(disguisePlayerId.get());

                if (playerInfo != null) {
                    return playerInfo.getSkinLocation();
                }
            }
        }

        return PALE_MIMIC_SKIN;
    }
}
