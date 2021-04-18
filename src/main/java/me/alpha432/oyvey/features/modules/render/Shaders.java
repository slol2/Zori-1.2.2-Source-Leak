package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class Shaders extends Module {
    
    public Setting<Mode> shader = register(new Setting<Mode>("Mode", Mode.green));

    private static Shaders INSTANCE = new Shaders();
    public Shaders() {
        super("Shaders", "i dont even know anymore", Module.Category.RENDER, false, false, false);
    }

    @Override
    public void onUpdate() {
        if (OpenGlHelper.shadersSupported && mc.getRenderViewEntity() instanceof EntityPlayer) {
            if (mc.entityRenderer.getShaderGroup() != null) {
                mc.entityRenderer.getShaderGroup().deleteShaderGroup();
            }
            try {
                mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/" + shader.getValue() + ".json"));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (mc.entityRenderer.getShaderGroup() != null && mc.currentScreen == null) {
            mc.entityRenderer.getShaderGroup().deleteShaderGroup();
        }
    }

    @Override
    public String getDisplayInfo() {
        return shader.currentEnumName();
    }

    @Override
    public void onDisable() {
        if (mc.entityRenderer.getShaderGroup() != null) {
            mc.entityRenderer.getShaderGroup().deleteShaderGroup();
        }
    }
    public enum Mode{
        notch, antialias, art, bits, blobs, blobs2, blur, bumpy, color_convolve, creeper, deconverge, desaturate, entity_outline, flip, fxaa, green, invert, ntsc, outline, pencil, phosphor, scan_pincusion, sobel, spider, wobble
    }
}
