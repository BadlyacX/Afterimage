package com.badlyac.afterimage.client;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;

import java.util.ArrayList;
import java.util.Collection;

public final class NostalgiaPackSwitcher {

    private static final String PROGRAMMER_ART_ID = "programmer_art";

    private static boolean active = false;
    private static boolean playerHadPackEnabled = false;

    public static void sync(boolean inNostalgia) {
        if (inNostalgia == active) return;
        active = inNostalgia;

        Minecraft mc = Minecraft.getInstance();
        PackRepository repo = mc.getResourcePackRepository();
        Collection<String> selected = new ArrayList<>(repo.getSelectedIds());

        if (inNostalgia) {
            playerHadPackEnabled = selected.contains(PROGRAMMER_ART_ID);
            if (!playerHadPackEnabled) {
                selected.add(PROGRAMMER_ART_ID);
                repo.setSelected(selected);
                mc.reloadResourcePacks();
            }
        } else {
            if (!playerHadPackEnabled && selected.contains(PROGRAMMER_ART_ID)) {
                selected.remove(PROGRAMMER_ART_ID);
                repo.setSelected(selected);
                mc.reloadResourcePacks();
            }
        }
    }
}
