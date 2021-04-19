package me.alpha432.oyvey.features.modules.oldfag;

import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityGhast;

public class GhastFinder extends Module {
    private int timer;

    public GhastFinder() {
        super("GhastFinder", "h", Module.Category.MISC, true, false, false);
    }

    public void onUpdate() {
        if (mc.player != null && mc.world != null) {
            timer++;
            for (final Entity e : mc.world.loadedEntityList) {
                if (e instanceof EntityGhast && timer >= 100) {
                    Command.sendMessage("Found Ghast! X:" + (int) e.posX + "Y:" + (int) e.posY + " Z:" + (int) e.posZ);
                    timer = -150;
                }
            }
        }
    }
}
