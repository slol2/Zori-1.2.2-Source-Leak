package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Viewmodel extends Module {
    public float defaultFov;
    public Setting<Integer> viewmodelDistance = register(new Setting<Integer>("ViewmodelDistance", 125, 0, 170, "Changes the distance of the Viewmodel"));

    public Viewmodel() {
        super("Viewmodel", "Changes viewmodel of items", Category.RENDER, false, false, false);
    }

    @SubscribeEvent
    public void fovVMC(final EntityViewRenderEvent.FOVModifier e) {
            e.setFOV((float)viewmodelDistance.getValue());
    }

    public void onEnable() {
        defaultFov = mc.gameSettings.fovSetting;
    }

    public void onDisable() {
        mc.gameSettings.fovSetting = defaultFov;
    }
}
