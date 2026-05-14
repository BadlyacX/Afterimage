package com.badlyac.afterimage.monster.palemimic;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PaleMimicRenderer extends GeoEntityRenderer<PaleMimicEntity> {
    public PaleMimicRenderer(EntityRendererProvider.Context context) {
        super(context, new PaleMimicModel());
        this.shadowRadius = 0.5F;
    }
}
