package com.badlyac.afterimage.monster.palemimic;

import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;

public record PathPoint(
        Vec3 pos,
        Vec3 movement,
        float yaw,
        float pitch,
        boolean jumping,
        boolean shiftKeyDown,
        boolean sprinting,
        boolean swimming,
        Pose pose
) {}
