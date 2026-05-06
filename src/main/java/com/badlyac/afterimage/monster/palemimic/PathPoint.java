package com.badlyac.afterimage.monster.palemimic;

import net.minecraft.world.phys.Vec3;

public record PathPoint(
        Vec3 pos,
        float yaw,
        float pitch,
        boolean jumping
) {}
