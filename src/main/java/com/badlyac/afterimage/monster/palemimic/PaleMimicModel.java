package com.badlyac.afterimage.monster.palemimic;

import com.badlyac.afterimage.AfterimageMod;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PaleMimicModel extends GeoModel<PaleMimicEntity> {

    @Override
    public ResourceLocation getModelResource(PaleMimicEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(
                AfterimageMod.MOD_ID,
                "geo/pale_mimic.geo.json"
        );
    }

    @Override
    public ResourceLocation getTextureResource(PaleMimicEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(
                AfterimageMod.MOD_ID,
                "textures/entity/pale_mimic/placeholder.png"
        );
    }

    @Override
    public ResourceLocation getAnimationResource(PaleMimicEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(
                AfterimageMod.MOD_ID,
                "animation/pale_mimic.animation.json"
        );
    }
}
