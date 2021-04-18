package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.event.events.UpdateWalkingPlayerEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.BlockUtil;
import me.alpha432.oyvey.util.EntityUtil;
import me.alpha432.oyvey.util.InventoryUtil;
import me.alpha432.oyvey.util.Timer;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

public class Selftrap extends Module {
    private final Setting<Integer> blocksPerTick = register(new Setting<Integer>("BlocksPerTick", 8, 1, 20));
    private final Setting<Integer> delay = register(new Setting<Integer>("Delay", 50, 0, 250));
    private final Setting<Boolean> rotate = register(new Setting<Boolean>("Rotate", true));
    private final Setting<Integer> disableTime = register(new Setting<Integer>("DisableTime", 200, 50, 300));
    private final Setting<Boolean> disable = register(new Setting<Boolean>("AutoDisable", true));
    private final Setting<Boolean> packet = register(new Setting<Boolean>("Packet", false));
    private final Timer offTimer = new Timer();
    private final Timer timer = new Timer();
    private boolean hasOffhand = false;
    private final Map<BlockPos, Integer> retries = new HashMap<BlockPos, Integer>();
    private final Timer retryTimer = new Timer();
    private int blocksThisTick = 0;
    private boolean isSneaking;
    private boolean offHand = false;

    public Selftrap() {
        super("SelfTrap", "Lure your enemies in!", Module.Category.COMBAT, true, false, true);
    }

    @Override
    public void onEnable() {
        if (Selftrap.fullNullCheck()) {
            disable();
        }
        offTimer.reset();
    }

    @Override
    public void onTick() {
        if (isOn() && (blocksPerTick.getValue() != 1 || !rotate.getValue().booleanValue())) {
            doSelfTrap();
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (isOn() && event.getStage() == 0 && blocksPerTick.getValue() == 1 && rotate.getValue().booleanValue()) {
            doSelfTrap();
        }
    }

    @Override
    public void onDisable() {
        isSneaking = EntityUtil.stopSneaking(isSneaking);
        retries.clear();
        offHand = false;
    }

    private void doSelfTrap() {
        if (check()) {
            return;
        }
        for (BlockPos position : getPositions()) {
            int placeability = BlockUtil.isPositionPlaceable(position, false);
            if (placeability == 1 && (retries.get(position) == null || retries.get(position) < 4)) {
                placeBlock(position);
                retries.put(position, retries.get(position) == null ? 1 : retries.get(position) + 1);
            }
            if (placeability != 3) continue;
            placeBlock(position);
        }
    }

    private List<BlockPos> getPositions() {
        ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
        positions.add(new BlockPos(Selftrap.mc.player.posX, Selftrap.mc.player.posY + 2.0, Selftrap.mc.player.posZ));
        int placeability = BlockUtil.isPositionPlaceable(positions.get(0), false);
        switch (placeability) {
            case 0: {
                return new ArrayList<BlockPos>();
            }
            case 3: {
                return positions;
            }
            case 1: {
                if (BlockUtil.isPositionPlaceable(positions.get(0), false, false) == 3) {
                    return positions;
                }
            }
            case 2: {
                positions.add(new BlockPos(Selftrap.mc.player.posX + 1.0, Selftrap.mc.player.posY + 1.0, Selftrap.mc.player.posZ));
                positions.add(new BlockPos(Selftrap.mc.player.posX + 1.0, Selftrap.mc.player.posY + 2.0, Selftrap.mc.player.posZ));
            }
        }
        positions.sort(Comparator.comparingDouble(Vec3i::getY));
        return positions;
    }

    private void placeBlock(BlockPos pos) {
        if (blocksThisTick < blocksPerTick.getValue()) {
            boolean smartRotate = blocksPerTick.getValue() == 1 && rotate.getValue() != false;
            int originalSlot = Selftrap.mc.player.inventory.currentItem;
            int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
            int eChestSot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
            if (obbySlot == -1 && eChestSot == -1) {
                toggle();
            }
            Selftrap.mc.player.inventory.currentItem = obbySlot == -1 ? eChestSot : obbySlot;
            Selftrap.mc.playerController.updateController();
            isSneaking = smartRotate ? BlockUtil.placeBlockSmartRotate(pos, hasOffhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, true, packet.getValue(), isSneaking) : BlockUtil.placeBlock(pos, hasOffhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, rotate.getValue(), packet.getValue(), isSneaking);
            Selftrap.mc.player.inventory.currentItem = originalSlot;
            Selftrap.mc.playerController.updateController();
            timer.reset();
            ++blocksThisTick;
        }
    }

    private boolean check() {
        if (Selftrap.fullNullCheck()) {
            disable();
            return true;
        }
        int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
        int eChestSot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
        if (obbySlot == -1 && eChestSot == -1) {
            toggle();
        }
        blocksThisTick = 0;
        isSneaking = EntityUtil.stopSneaking(isSneaking);
        if (retryTimer.passedMs(2000L)) {
            retries.clear();
            retryTimer.reset();
        }
        if (!EntityUtil.isSafe(Selftrap.mc.player)) {
            offTimer.reset();
            return true;
        }
        if (disable.getValue().booleanValue() && offTimer.passedMs(disableTime.getValue().intValue())) {
            disable();
            return true;
        }
        return !timer.passedMs(delay.getValue().intValue());
    }
}

