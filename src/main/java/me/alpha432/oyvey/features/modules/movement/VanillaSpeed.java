package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.event.events.MoveEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.manager.Mapping;
import me.alpha432.oyvey.util.EntityUtil;
import me.alpha432.oyvey.util.Timer;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.Minecraft;
import net.minecraft.init.MobEffects;

import java.lang.reflect.Field;

public class VanillaSpeed extends Module {
    public Setting<Float> timerSpeed = register(new Setting<Float>("TimerSpeed", 1.15f, 1.0f, 1.5f));
    public Setting<Float> jumpHeight = register(new Setting<Float>("JumpHeight", 0.41f, 0.0f, 1.0f));
    public Setting<Float> vanillaSpeed = register(new Setting<Float>("JumpHeight", 1.0f, 0.1f, 3.0f));
    public Setting<movemode> MoveModes = register(new Setting("Mode", movemode.Strafe));

    public VanillaSpeed() {
        super("VanillaSpeed", "i dont even know anymore", Module.Category.MOVEMENT, true, false, false);
    }

    public enum movemode {
        Strafe, Vanilla
    }

    private boolean slowDown;
    private double playerSpeed;
    private Timer timer = new Timer();

    private void setTimer(final float value) {
        try {
            final Field timer = Minecraft.class.getDeclaredField(Mapping.timer);
            timer.setAccessible(true);
            final Field tickLength = net.minecraft.util.Timer.class.getDeclaredField(Mapping.tickLength);
            tickLength.setAccessible(true);
            tickLength.setFloat(timer.get(mc), 50.0f / value);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onEnable() {
        super.onEnable();
        playerSpeed = EntityUtil.getBaseMoveSpeed();
    }

    public void onDisable() {
        timer.reset();
    }

    public void onUpdate(MoveEvent event) {
        if(mc.player.isInLava() || mc.player.isInWater() || mc.player.isOnLadder() || mc.player.isInWeb) {
            return;
        }
        if(MoveModes.getValue().equals("Strafe")) {
            double heightY = jumpHeight.getValue();
            if(mc.player.onGround && EntityUtil.isMoving(mc.player) && timer.hasReached(300)) {
                setTimer((float)timerSpeed.getValue());
                if(mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                    heightY += (mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1f;
                }
                event.setY(mc.player.motionY = heightY);
                playerSpeed = EntityUtil.getBaseMoveSpeed() * (EntityUtil.isColliding(0, -0.5, 0) instanceof BlockLiquid && !EntityUtil.isInLiquid() ? 0.9 : 1.901);
                slowDown = true;
                timer.reset();
            }else {
                if(slowDown || mc.player.collidedHorizontally) {
                    playerSpeed -= (EntityUtil.isColliding(0, -0.8, 0) instanceof BlockLiquid && !EntityUtil.isInLiquid()) ? 0.4 : 0.7 * (playerSpeed = EntityUtil.getBaseMoveSpeed());
                    slowDown = false;
                }else {
                    playerSpeed -= playerSpeed / 159.0;
                }
            }
            playerSpeed = Math.max(playerSpeed, EntityUtil.getBaseMoveSpeed());
            double[] dir = EntityUtil.forward(playerSpeed);
            event.setX(dir[0]);
            event.setZ(dir[1]);
        }
        if(MoveModes.getValue().equals("Vanilla")) {
        if(mc.player == null || mc.world == null) {
            disable();
            return;
        }
            if(mc.player.moveForward > 0) {
                double direction = getDirection();
                double speed = vanillaSpeed.getValue();
                setTimer((float)timerSpeed.getValue());
                mc.player.motionX = -Math.sin(direction) * speed;
                mc.player.motionZ = Math.cos(direction) * speed;
            }
        }

    }

    public static float getDirection() {
        float var1 = mc.player.rotationYaw;

        if(mc.player.moveForward < 0.0f) var1 += 180.0f;
        float forward = 1.0f;

        if(mc.player.moveForward < 0.0f) forward = -0.5f;
        else if(mc.player.moveForward > 0.0f) forward = 0.5f;

        if(mc.player.moveStrafing > 0.0f) var1 -= 90.f * forward;

        if(mc.player.moveStrafing < 0.0f) var1 += 90.0f * forward;

        var1 *= 0.017453292f;
        return var1;
    }

}