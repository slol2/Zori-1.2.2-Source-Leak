package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.event.events.KeyPressedEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class NoSlowDown extends Module {
    public Setting<Boolean> guiMove = register(new Setting<Boolean>("GuiMove", true));
    public Setting<Boolean> noSlow = register(new Setting<Boolean>("NoSlow", true));
    public Setting<Boolean> soulSand = register(new Setting<Boolean>("SoulSand", true));
    public Setting<Boolean> strict = register(new Setting<Boolean>("Strict", false));
    public Setting<Boolean> sneakPacket = register(new Setting<Boolean>("SneakPacket", false));
    public Setting<Boolean> endPortal = register(new Setting<Boolean>("EndPortal", false));
    public Setting<Boolean> webs = register(new Setting<Boolean>("Webs", false));
    public final Setting<Double> webHorizontalFactor = register(new Setting<Double>("WebHSpeed", 2.0, 0.0, 100.0));
    public final Setting<Double> webVerticalFactor = register(new Setting<Double>("WebVSpeed", 2.0, 0.0, 100.0));
    private static NoSlowDown INSTANCE = new NoSlowDown();
    private boolean sneaking = false;
    private static KeyBinding[] keys = new KeyBinding[]{NoSlowDown.mc.gameSettings.keyBindForward, NoSlowDown.mc.gameSettings.keyBindBack, NoSlowDown.mc.gameSettings.keyBindLeft, NoSlowDown.mc.gameSettings.keyBindRight, NoSlowDown.mc.gameSettings.keyBindJump, NoSlowDown.mc.gameSettings.keyBindSprint};

    public NoSlowDown() {
        super("NoSlowDown", "Prevents you from getting slowed down.", Module.Category.MOVEMENT, true, false, false);
        setInstance();
    }

    private void setInstance() {
        INSTANCE = this;
    }

    public static NoSlowDown getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NoSlowDown();
        }
        return INSTANCE;
    }

    @Override
    public void onUpdate() {
        if (guiMove.getValue().booleanValue()) {
            if (NoSlowDown.mc.currentScreen instanceof GuiOptions || NoSlowDown.mc.currentScreen instanceof GuiVideoSettings || NoSlowDown.mc.currentScreen instanceof GuiScreenOptionsSounds || NoSlowDown.mc.currentScreen instanceof GuiContainer || NoSlowDown.mc.currentScreen instanceof GuiIngameMenu) {
                for (KeyBinding bind : keys) {
                    KeyBinding.setKeyBindState((int)bind.getKeyCode(), (boolean)Keyboard.isKeyDown((int)bind.getKeyCode()));
                }
            } else if (NoSlowDown.mc.currentScreen == null) {
                for (KeyBinding bind : keys) {
                    if (Keyboard.isKeyDown((int)bind.getKeyCode())) continue;
                    KeyBinding.setKeyBindState((int)bind.getKeyCode(), (boolean)false);
                }
            }
        }
        if (webs.getValue().booleanValue() && OyVey.moduleManager.getModuleByClass(Flight.class).isDisabled() && OyVey.moduleManager.getModuleByClass(PacketFly.class).isDisabled() && NoSlowDown.mc.player.isInWeb) {
            NoSlowDown.mc.player.motionX *= webHorizontalFactor.getValue().doubleValue();
            NoSlowDown.mc.player.motionZ *= webHorizontalFactor.getValue().doubleValue();
            NoSlowDown.mc.player.motionY *= webVerticalFactor.getValue().doubleValue();
        }
        Item item = NoSlowDown.mc.player.getActiveItemStack().getItem();
        if (sneaking && !NoSlowDown.mc.player.isHandActive() && sneakPacket.getValue().booleanValue()) {
            NoSlowDown.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)NoSlowDown.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            sneaking = false;
        }
    }

    @SubscribeEvent
    public void onUseItem(PlayerInteractEvent.RightClickItem event) {
        Item item = NoSlowDown.mc.player.getHeldItem(event.getHand()).getItem();
        if ((item instanceof ItemFood || item instanceof ItemBow || item instanceof ItemPotion && sneakPacket.getValue().booleanValue()) && !sneaking) {
            NoSlowDown.mc.player.connection.sendPacket((Packet)new CPacketEntityAction((Entity)NoSlowDown.mc.player, CPacketEntityAction.Action.START_SNEAKING));
            sneaking = true;
        }
    }

    @SubscribeEvent
    public void onInput(InputUpdateEvent event) {
        if (noSlow.getValue().booleanValue() && NoSlowDown.mc.player.isHandActive() && !NoSlowDown.mc.player.isRiding()) {
            event.getMovementInput().moveStrafe *= 5.0f;
            event.getMovementInput().moveForward *= 5.0f;
        }
    }

    @SubscribeEvent
    public void onKeyEvent(KeyPressedEvent event) {
        if (guiMove.getValue().booleanValue() && event.getStage() == 0 && !(NoSlowDown.mc.currentScreen instanceof GuiChat)) {
            event.info = event.pressed;
        }
    }

    @SubscribeEvent
    public void onPacket(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer && strict.getValue().booleanValue() && noSlow.getValue().booleanValue() && NoSlowDown.mc.player.isHandActive() && !NoSlowDown.mc.player.isRiding()) {
            NoSlowDown.mc.player.connection.sendPacket((Packet)new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, new BlockPos(Math.floor(NoSlowDown.mc.player.posX), Math.floor(NoSlowDown.mc.player.posY), Math.floor(NoSlowDown.mc.player.posZ)), EnumFacing.DOWN));
        }
    }
}

