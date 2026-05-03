package com.badlyac.afterimage.monster.palemimic;


import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class PaleMimicEntity extends Monster {


    public enum State {

    }

    public PaleMimicEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

}