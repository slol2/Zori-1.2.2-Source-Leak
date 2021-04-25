


package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.combat.AutoCrystal;

public class SelfCrystal
extends Module {
    public SelfCrystal() {
        super("SelfCrystal", "Best module", Module.Category.COMBAT, true, false, false);
    }

    @Override
    public void onTick() {
        if (AutoCrystal.getInstance().isEnabled()) {
            AutoCrystal.target = SelfCrystal.mc.player;
        }
    }
}

