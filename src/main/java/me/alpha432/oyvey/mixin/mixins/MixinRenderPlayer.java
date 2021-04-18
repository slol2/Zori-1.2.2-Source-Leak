package me.alpha432.oyvey.mixin.mixins;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.modules.render.TexturedChams;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ RenderPlayer.class })

public class MixinRenderPlayer {
    @Inject(method = "renderEntityName", at = @At("HEAD"), cancellable = true)
    public void renderEntityNameHook(AbstractClientPlayer entityIn, double x, double y, double z, String name, double distanceSq, CallbackInfo info) {
        if (OyVey.moduleManager.isModuleEnabled("NameTags"))
            info.cancel();
    }

    @Overwrite
    public ResourceLocation getEntityTexture(final AbstractClientPlayer entity) {
        if (OyVey.moduleManager.isModuleEnabled("TexturedChams")) {
            GL11.glColor4f(TexturedChams.red.getValue() / 255.0f, TexturedChams.green.getValue() / 255.0f, TexturedChams.blue.getValue() / 255.0f, TexturedChams.alpha.getValue() / 255.0f);
            return new ResourceLocation("minecraft:steve_skin1.png");
        }
        return entity.getLocationSkin();
    }

}