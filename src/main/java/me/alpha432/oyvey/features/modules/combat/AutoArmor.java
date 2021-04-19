package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.InventoryUtil;
import me.alpha432.oyvey.util.Timer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AutoArmor extends Module {
    private final Setting<Integer> delay = register(new Setting<Integer>("Delay", 50, 0, 500));
    private final Setting<Boolean> curse = register(new Setting<Boolean>("Vanishing", false));
    private final Setting<Boolean> mendingTakeOff = register(new Setting<Boolean>("AutoMend", false));
    private final Setting<Integer> closestEnemy = register(new Setting<Object>("Enemy", Integer.valueOf(8), Integer.valueOf(1), Integer.valueOf(20), v -> mendingTakeOff.getValue()));
    private final Setting<Integer> repair = register(new Setting<Object>("Repair%", Integer.valueOf(80), Integer.valueOf(1), Integer.valueOf(100), v -> mendingTakeOff.getValue()));
    private final Setting<Integer> actions = register(new Setting<Integer>("Packets", 3, 1, 12));
    private final Timer timer = new Timer();
    private final Queue<InventoryUtil.Task> taskList = new ConcurrentLinkedQueue<InventoryUtil.Task>();
    private final List<Integer> doneSlots = new ArrayList<Integer>();
    boolean flag;

    public AutoArmor() {
        super("AutoArmor", "Puts Armor on for you.", Module.Category.COMBAT, true, false, false);
    }

    @Override
    public void onLogin() {
        timer.reset();
    }

    @Override
    public void onDisable() {
        taskList.clear();
        doneSlots.clear();
        flag = false;
    }

    @Override
    public void onLogout() {
        taskList.clear();
        doneSlots.clear();
    }

    @Override
    public void onTick() {
        if (AutoArmor.fullNullCheck() || AutoArmor.mc.currentScreen instanceof GuiContainer && !(AutoArmor.mc.currentScreen instanceof GuiInventory)) {
            return;
        }
        if (taskList.isEmpty()) {
            int slot;
            ItemStack feet;
            int slot2;
            ItemStack legging;
            int slot3;
            ItemStack chest;
            int slot4;
            if (mendingTakeOff.getValue().booleanValue() && InventoryUtil.holdingItem(ItemExpBottle.class) && AutoArmor.mc.gameSettings.keyBindUseItem.isKeyDown() && AutoArmor.mc.world.playerEntities.stream().noneMatch(e -> e != AutoArmor.mc.player && !OyVey.friendManager.isFriend(e.getName()) && AutoArmor.mc.player.getDistance(e) <= (float) closestEnemy.getValue().intValue()) && !flag) {
                int goods;
                int dam;
                int takeOff = 0;
                for (Map.Entry<Integer, ItemStack> armorSlot : getArmor().entrySet()) {
                    ItemStack stack = armorSlot.getValue();
                    float percent = (float) repair.getValue().intValue() / 100.0f;
                    dam = Math.round((float) stack.getMaxDamage() * percent);
                    if (dam >= (goods = stack.getMaxDamage() - stack.getItemDamage())) continue;
                    ++takeOff;
                }
                if (takeOff == 4) {
                    flag = true;
                }
                if (!flag) {
                    ItemStack itemStack1 = AutoArmor.mc.player.inventoryContainer.getSlot(5).getStack();
                    if (!itemStack1.isEmpty) {
                        int goods2;
                        float percent = (float) repair.getValue().intValue() / 100.0f;
                        int dam2 = Math.round((float) itemStack1.getMaxDamage() * percent);
                        if (dam2 < (goods2 = itemStack1.getMaxDamage() - itemStack1.getItemDamage())) {
                            takeOffSlot(5);
                        }
                    }
                    ItemStack itemStack2 = AutoArmor.mc.player.inventoryContainer.getSlot(6).getStack();
                    if (!itemStack2.isEmpty) {
                        int goods3;
                        float percent = (float) repair.getValue().intValue() / 100.0f;
                        int dam3 = Math.round((float) itemStack2.getMaxDamage() * percent);
                        if (dam3 < (goods3 = itemStack2.getMaxDamage() - itemStack2.getItemDamage())) {
                            takeOffSlot(6);
                        }
                    }
                    ItemStack itemStack3 = AutoArmor.mc.player.inventoryContainer.getSlot(7).getStack();
                    if (!itemStack3.isEmpty) {
                        float percent = (float) repair.getValue().intValue() / 100.0f;
                        dam = Math.round((float) itemStack3.getMaxDamage() * percent);
                        if (dam < (goods = itemStack3.getMaxDamage() - itemStack3.getItemDamage())) {
                            takeOffSlot(7);
                        }
                    }
                    ItemStack itemStack4 = AutoArmor.mc.player.inventoryContainer.getSlot(8).getStack();
                    if (!itemStack4.isEmpty) {
                        int goods4;
                        float percent = (float) repair.getValue().intValue() / 100.0f;
                        int dam4 = Math.round((float) itemStack4.getMaxDamage() * percent);
                        if (dam4 < (goods4 = itemStack4.getMaxDamage() - itemStack4.getItemDamage())) {
                            takeOffSlot(8);
                        }
                    }
                }
                return;
            }
            flag = false;
            ItemStack helm = AutoArmor.mc.player.inventoryContainer.getSlot(5).getStack();
            if (helm.getItem() == Items.AIR && (slot4 = InventoryUtil.findArmorSlot(EntityEquipmentSlot.HEAD, curse.getValue(), true)) != -1) {
                getSlotOn(5, slot4);
            }
            if ((chest = AutoArmor.mc.player.inventoryContainer.getSlot(6).getStack()).getItem() == Items.AIR && (slot3 = InventoryUtil.findArmorSlot(EntityEquipmentSlot.CHEST, curse.getValue(), true)) != -1) {
                getSlotOn(6, slot3);
            }
            if ((legging = AutoArmor.mc.player.inventoryContainer.getSlot(7).getStack()).getItem() == Items.AIR && (slot2 = InventoryUtil.findArmorSlot(EntityEquipmentSlot.LEGS, curse.getValue(), true)) != -1) {
                getSlotOn(7, slot2);
            }
            if ((feet = AutoArmor.mc.player.inventoryContainer.getSlot(8).getStack()).getItem() == Items.AIR && (slot = InventoryUtil.findArmorSlot(EntityEquipmentSlot.FEET, curse.getValue(), true)) != -1) {
                getSlotOn(8, slot);
            }
        }
        if (timer.passedMs((int) ((float) delay.getValue().intValue() * OyVey.serverManager.getTpsFactor()))) {
            if (!taskList.isEmpty()) {
                for (int i = 0; i < actions.getValue(); ++i) {
                    InventoryUtil.Task task = taskList.poll();
                    if (task == null) continue;
                    task.run();
                }
            }
            timer.reset();
        }
    }

    private void takeOffSlot(int slot) {
        if (taskList.isEmpty()) {
            int target = -1;
            for (int i : InventoryUtil.findEmptySlots(true)) {
                if (doneSlots.contains(target)) continue;
                target = i;
                doneSlots.add(i);
            }
            if (target != -1) {
                taskList.add(new InventoryUtil.Task(slot));
                taskList.add(new InventoryUtil.Task(target));
                taskList.add(new InventoryUtil.Task());
            }
        }
    }

    private void getSlotOn(int slot, int target) {
        if (taskList.isEmpty()) {
            doneSlots.remove((Object) target);
            taskList.add(new InventoryUtil.Task(target));
            taskList.add(new InventoryUtil.Task(slot));
            taskList.add(new InventoryUtil.Task());
        }
    }

    private Map<Integer, ItemStack> getArmor() {
        return getInventorySlots(5, 8);
    }

    private Map<Integer, ItemStack> getInventorySlots(int current, int last) {
        HashMap<Integer, ItemStack> fullInventorySlots = new HashMap<Integer, ItemStack>();
        while (current <= last) {
            fullInventorySlots.put(current, AutoArmor.mc.player.inventoryContainer.getInventory().get(current));
            ++current;
        }
        return fullInventorySlots;
    }
}

