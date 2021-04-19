package me.alpha432.oyvey.features.modules.combat;

import me.alpha432.oyvey.event.events.ClientEvent;
import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.event.events.UpdateWalkingPlayerEvent;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.gui.OyVeyGui;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.player.Freecam;
import me.alpha432.oyvey.features.setting.Bind;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.*;
import net.minecraft.block.*;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Auto32k extends Module {
    private final Setting<Mode> mode = register(new Setting("Mode", Mode.NORMAL));
    private final Setting<Integer> delay = register(new Setting("Delay/Place", 25, 0, 250));
    private final Setting<Integer> delayDispenser = register(new Setting("Blocks/Place", 1, 1, 8, v -> mode.getValue() != Mode.NORMAL));
    private final Setting<Integer> blocksPerPlace = register(new Setting("Actions/Place", 1, 1, 3, v -> mode.getValue() == Mode.NORMAL));
    private final Setting<Float> range = register(new Setting("PlaceRange", 4.5F, 0.0F, 6.0F));
    private final Setting<Boolean> raytrace = register(new Setting("Raytrace", false));
    private final Setting<Boolean> rotate = register(new Setting("Rotate", false));
    public Setting<Boolean> autoSwitch = register(new Setting("AutoSwitch", false, v -> mode.getValue() == Mode.NORMAL));
    public Setting<Boolean> withBind = register(new Setting("WithBind", false, v -> mode.getValue() == Mode.NORMAL && autoSwitch.getValue()));
    public Setting<Bind> switchBind = register(new Setting("SwitchBind", new Bind(-1), v -> autoSwitch.getValue() && mode.getValue() == Mode.NORMAL && withBind.getValue()));
    private final Setting<Double> targetRange = register(new Setting("TargetRange", 6.0, 0.0, 20.0));
    private final Setting<Boolean> extra = register(new Setting("ExtraRotation", false, v -> rotate.getValue() && blocksPerPlace.getValue() > 1));
    private final Setting<PlaceType> placeType = register(new Setting("Place", PlaceType.CLOSE));
    private final Setting<Boolean> freecam = register(new Setting("Freecam", false));
    private final Setting<Boolean> onOtherHoppers = register(new Setting("UseHoppers", false));
    private final Setting<Boolean> preferObby = register(new Setting("UseObby", false, v -> mode.getValue() != Mode.NORMAL));
    private final Setting<Boolean> messages = register(new Setting("Messages", false));
    private final Setting<Boolean> checkForShulker = register(new Setting("CheckShulker", true));
    private final Setting<Integer> checkDelay = register(new Setting("CheckDelay", 500, 0, 500, v -> checkForShulker.getValue()));
    private final Setting<Boolean> drop = register(new Setting("Drop", false));
    private final Setting<Boolean> checkStatus = register(new Setting("CheckState", true));
    private final Setting<Boolean> packet = register(new Setting("Packet", false));
    private final Setting<Boolean> repeatSwitch = register(new Setting("SwitchOnFail", true));
    private final Setting<Boolean> cancelNextRotation = register(new Setting("AntiPacket", false, v -> mode.getValue() == Mode.DISPENSER));
    private final Setting<Boolean> mine = register(new Setting("Mine", false, v -> drop.getValue()));
    private final Setting<Float> hopperDistance = register(new Setting("HopperRange", 8.0F, 0.0F, 20.0F));
    private final Setting<Integer> trashSlot = register(new Setting("32kSlot", 0, 0, 9));

    private float yaw;
    private float pitch;
    private boolean spoof;
    public boolean switching;

    private int lastHotbarSlot = -1;

    private int shulkerSlot = -1;
    private int hopperSlot = -1;

    private BlockPos hopperPos;
    private EntityPlayer target;
    public Step currentStep = Step.PRE;
    private final Timer placeTimer = new Timer();
    private static Auto32k instance;

    private int obbySlot = -1;
    private int dispenserSlot = -1;
    private int redstoneSlot = -1;
    private DispenserData finalDispenserData;
    private int actionsThisTick = 0;
    private boolean checkedThisTick = false;
    private boolean nextRotCanceled = false;

    public Auto32k() {
        super("Auto32k", "Auto32ks", Category.COMBAT, false, false, true);
        instance = this;
    }

    public static Auto32k getInstance() {
        if(instance == null) {
            instance = new Auto32k();
        }
        return instance;
    }

    @Override
    public void onToggle() {
        checkedThisTick = false;
        resetFields();
        if(isOn()) {
            if(mc.currentScreen instanceof GuiHopper) {
                currentStep = Step.HOPPERGUI;
            }
        }

        if(mode.getValue() == Mode.NORMAL && autoSwitch.getValue() && !withBind.getValue()) {
            switching = true;
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if(event.getStage() != 0) {
            return;
        }

        checkedThisTick = false;
        actionsThisTick = 0;
        if(isOff() || (mode.getValue() == Mode.NORMAL && autoSwitch.getValue() && !switching)) {
            return;
        }

        if(mode.getValue() == Mode.NORMAL) {
            normal32k();
        } else {
            processDispenser32k();
        }
    }

    @SubscribeEvent
    public void onGui(GuiOpenEvent event) {
        if(isOff()) {
            return;
        }

        if(mc.currentScreen instanceof GuiHopper) {
            if(drop.getValue() && mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_SWORD && hopperPos != null) {
                mc.player.dropItem(true);
                if(mine.getValue() && hopperPos != null) {
                    //int currentSlot = mc.player.inventory.currentItem;
                    int pickaxeSlot = InventoryUtil.findHotbarBlock(ItemPickaxe.class);
                    if(pickaxeSlot != -1) {
                        InventoryUtil.switchToHotbarSlot(pickaxeSlot, false);
                        if(rotate.getValue()) {
                            rotateToPos(hopperPos.up(), null);
                        }
                        mc.playerController.onPlayerDamageBlock(hopperPos.up(), mc.player.getHorizontalFacing());
                        mc.playerController.onPlayerDamageBlock(hopperPos.up(), mc.player.getHorizontalFacing());
                        mc.player.swingArm(EnumHand.MAIN_HAND);
                        //InventoryUtil.switchToHotbarSlot(currentSlot, false);
                    }
                }
            }
            resetFields();
            if(mode.getValue() != Mode.NORMAL) {
                disable();
                return;
            }
            if(!autoSwitch.getValue()) {
                disable();
            } else if(!withBind.getValue()) {
                disable();
            }
        } else if(event.getGui() instanceof GuiHopper) {
            currentStep = Step.HOPPERGUI;
        }
    }

    @Override
    public String getDisplayInfo() {
        if(switching) {
            return TextUtil.GREEN + "Switch";
        }
        return null;
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if(isOff()) {
            return;
        }

        if(Keyboard.getEventKeyState() && !(mc.currentScreen instanceof OyVeyGui) && switchBind.getValue().getKey() == Keyboard.getEventKey() && withBind.getValue()) {
            if(switching) {
                resetFields();
                switching = true;
            }
            switching = !switching;
        }
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        if(event.getStage() == 2) {
            Setting setting = event.getSetting();
            if(setting != null && setting.getFeature().equals(this) && setting.equals(mode)) {
                resetFields();
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if(isOff()) {
            return;
        }

        if(event.getPacket() instanceof CPacketPlayer) {
            if(nextRotCanceled && event.getPacket() instanceof CPacketPlayer.Rotation) {
                event.setCanceled(true);
                nextRotCanceled = false;
            }
            if(spoof) {
                CPacketPlayer packet = event.getPacket();
                packet.yaw = yaw;
                packet.pitch = pitch;
                spoof = false;
            }
        } else if(event.getPacket() instanceof CPacketCloseWindow) {
            if(mc.currentScreen instanceof GuiHopper && hopperPos != null) {
                if(drop.getValue() && mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_SWORD) {
                    mc.player.dropItem(true);
                    if(mine.getValue()) {
                        //int currentSlot = mc.player.inventory.currentItem;
                        int pickaxeSlot = InventoryUtil.findHotbarBlock(ItemPickaxe.class);
                        if(pickaxeSlot != -1) {
                            InventoryUtil.switchToHotbarSlot(pickaxeSlot, false);
                            if(rotate.getValue()) {
                                rotateToPos(hopperPos.up(), null);
                            }
                            mc.playerController.onPlayerDamageBlock(hopperPos.up(), mc.player.getHorizontalFacing());
                            mc.playerController.onPlayerDamageBlock(hopperPos.up(), mc.player.getHorizontalFacing());
                            mc.player.swingArm(EnumHand.MAIN_HAND);
                            //InventoryUtil.switchToHotbarSlot(currentSlot, false);
                        }
                    }
                }
                resetFields();
                if(!autoSwitch.getValue()) {
                    disable();
                } else if(!withBind.getValue()) {
                    disable();
                }
            }
        }
    }


    /*NORMAL 32K:*/


    private void normal32k() {
        if(autoSwitch.getValue()) {
            if(switching) {
                processNormal32k();
            } else {
                resetFields();
            }
        } else {
            processNormal32k();
        }
    }

    private void processNormal32k() {
        if(placeTimer.passedMs(delay.getValue())) {
            check();
            switch (currentStep) {
                case PRE:
                    runPreStep();
                case HOPPER:
                    if (currentStep == Step.HOPPER) {
                        checkState();
                        if(currentStep == Step.PRE) {
                            if(checkedThisTick) {
                                processNormal32k();
                            }
                            return;
                        }
                        runHopperStep();
                        if (actionsThisTick >= blocksPerPlace.getValue() && !placeTimer.passedMs(delay.getValue())) {
                            break;
                        }
                    }
                case SHULKER:
                    checkState();
                    if(currentStep == Step.PRE) {
                        if(checkedThisTick) {
                            processNormal32k();
                        }
                        return;
                    }
                    runShulkerStep();
                    if (actionsThisTick >= blocksPerPlace.getValue() && !placeTimer.passedMs(delay.getValue())) {
                        break;
                    }
                case CLICKHOPPER:
                    checkState();
                    if(currentStep == Step.PRE) {
                        if(checkedThisTick) {
                            processNormal32k();
                        }
                        return;
                    }
                    runClickHopper();
                case HOPPERGUI:
                    runHopperGuiStep();
                    break;
                default:
                    Command.sendMessage(TextUtil.RED + "This shouldnt happen, report to 3arthqu4ke!!!");
                    Command.sendMessage(TextUtil.RED + "This shouldnt happen, report to 3arthqu4ke!!!");
                    Command.sendMessage(TextUtil.RED + "This shouldnt happen, report to 3arthqu4ke!!!");
                    Command.sendMessage(TextUtil.RED + "This shouldnt happen, report to 3arthqu4ke!!!");
                    Command.sendMessage(TextUtil.RED + "This shouldnt happen, report to 3arthqu4ke!!!");
                    currentStep = Step.PRE;
                    break;
            }
        }
    }

    private void runPreStep() {
        PlaceType type = placeType.getValue();

        if(Freecam.getInstance().isOn() && !freecam.getValue()) {
            if(messages.getValue()) {
                Command.sendMessage(TextUtil.RED + "<Auto32k> Disable Freecam.");
            }
            if(autoSwitch.getValue()) {
                resetFields();
                if(!withBind.getValue()) {
                    disable();
                }
            } else {
                disable();
            }
            return;
        }

        lastHotbarSlot = mc.player.inventory.currentItem;
        hopperSlot = InventoryUtil.findHotbarBlock(BlockHopper.class);
        shulkerSlot = InventoryUtil.findHotbarBlock(BlockShulkerBox.class);

        if(mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock)mc.player.getHeldItemOffhand().getItem()).getBlock();
            if(block instanceof BlockShulkerBox) {
                shulkerSlot = -2;
            } else if(block instanceof BlockHopper) {
                hopperSlot = -2;
            }
        }

        if(shulkerSlot == -1 || hopperSlot == -1) {
            if(messages.getValue()) {
                Command.sendMessage(TextUtil.RED + "<Auto32k> Materials not found.");
            }
            if(autoSwitch.getValue()) {
                resetFields();
                if(!withBind.getValue()) {
                    disable();
                }
            } else {
                disable();
            }
            return;
        }

        target = EntityUtil.getClosestEnemy(targetRange.getValue());
        if(target == null) {
            if(autoSwitch.getValue()) {
                if(switching) {
                    resetFields();
                    switching = true;
                } else {
                    resetFields();
                }
                return;
            }
            type = placeType.getValue() == PlaceType.MOUSE ? PlaceType.MOUSE : PlaceType.CLOSE;
        }

        hopperPos = findBestPos(type, target);
        if(hopperPos != null) {
            if(mc.world.getBlockState(hopperPos).getBlock() instanceof BlockHopper) {
                currentStep = Step.SHULKER;
            } else {
                currentStep = Step.HOPPER;
            }
        } else {
            if(messages.getValue()) {
                Command.sendMessage(TextUtil.RED + "<Auto32k> Block not found.");
            }
            if(autoSwitch.getValue()) {
                resetFields();
                if(!withBind.getValue()) {
                    disable();
                }
            } else {
                disable();
            }
        }
    }

    private void runHopperStep() {
        if(currentStep == Step.HOPPER) {
            runPlaceStep(hopperPos, hopperSlot);
            currentStep = Step.SHULKER;
        }
    }

    private void runShulkerStep() {
        if(currentStep == Step.SHULKER) {
            runPlaceStep(hopperPos.up(), shulkerSlot);
            currentStep = Step.CLICKHOPPER;
        }
    }

    private void runClickHopper() {
        if(currentStep != Step.CLICKHOPPER) {
            return;
        }

        if(mode.getValue() == Mode.NORMAL && !(mc.world.getBlockState(hopperPos.up()).getBlock() instanceof BlockShulkerBox) && checkForShulker.getValue()) {
            if(placeTimer.passedMs(checkDelay.getValue())) {
                currentStep = Step.SHULKER;
            }
            return;
        }

        clickBlock(hopperPos);
        currentStep = Step.HOPPERGUI;
    }

    private void runHopperGuiStep() {
        if(mc.player.openContainer instanceof ContainerHopper && currentStep == Step.HOPPERGUI) {
            if(!EntityUtil.holding32k(mc.player)) {
                int swordIndex = -1;
                for (int i = 0; i < 5; i++) {
                    if (EntityUtil.is32k(mc.player.openContainer.inventorySlots.get(0).inventory.getStackInSlot(i))) {
                        swordIndex = i;
                        break;
                    }
                }

                if(swordIndex == -1) {
                    return;
                }

                if(trashSlot.getValue() != 0) {
                    InventoryUtil.switchToHotbarSlot(trashSlot.getValue() - 1, false);
                } else {
                    if(mode.getValue() != Mode.NORMAL && shulkerSlot > 35 && shulkerSlot != 45) {
                        InventoryUtil.switchToHotbarSlot(44 - shulkerSlot, false);
                    }
                }
                mc.playerController.windowClick(mc.player.openContainer.windowId, swordIndex, trashSlot.getValue() == 0 ? mc.player.inventory.currentItem : (trashSlot.getValue() - 1), ClickType.SWAP, mc.player);
            }
        }
    }

    private void runPlaceStep(BlockPos pos, int slot) {
        //TODO: HOLY SHIT WRITE PROPER UTIL FOR BLOCKPLACING EVERYTHING IN BLOCKUTIL IS CHINESE
        EnumFacing side = BlockUtil.getFirstFacing(pos);
        if (side == null) {
            return;
        }

        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();

        if(!mc.player.isSneaking() && (BlockUtil.blackList.contains(neighbourBlock) || BlockUtil.shulkerList.contains(neighbourBlock))) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            mc.player.setSneaking(true);
        }

        if(rotate.getValue()) {
            if(blocksPerPlace.getValue() > 1) {
                final float[] angle = RotationUtil.getLegitRotations(hitVec);
                //TODO: FIND SMART HITVEC HERE (SIMPLE USE mc.world.raytrace AND REMOVE BLOCKS UNTIL U HIT THE BLOCK)
                if(extra.getValue()) {
                    RotationUtil.faceYawAndPitch(angle[0], angle[1]); //GONNA LAG BACK
                }
            } else {
                rotateToPos(null, hitVec);
            }
        }

        InventoryUtil.switchToHotbarSlot(slot, false);
        BlockUtil.rightClickBlock(neighbour, hitVec, slot == -2 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, opposite, packet.getValue());
        placeTimer.reset();
        actionsThisTick++;
    }

    private BlockPos findBestPos(PlaceType type, EntityPlayer target) {
        BlockPos pos = null;
        NonNullList<BlockPos> positions = NonNullList.create();
        positions.addAll(BlockUtil.getSphere(EntityUtil.getPlayerPos(mc.player), range.getValue(), range.getValue().intValue(), false, true, 0).stream().filter(this::canPlace).collect(Collectors.toList()));

        switch(type) {
            case MOUSE:
                if(mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
                    BlockPos mousePos = mc.objectMouseOver.getBlockPos();
                    if(mousePos != null && !canPlace(mousePos)) {
                        BlockPos mousePosUp = mousePos.up();
                        if(canPlace(mousePosUp)) {
                            pos = mousePosUp;
                        }
                    } else {
                        pos = mousePos;
                    }
                }
                if(pos != null) {
                    break;
                }
            case CLOSE:
                positions.sort(Comparator.comparingDouble(pos2 -> mc.player.getDistanceSq(pos2)));
                pos = positions.get(0);
                break;
            case ENEMY:
                positions.sort(Comparator.comparingDouble(target::getDistanceSq));
                pos = positions.get(0);
                break;
            case MIDDLE:
                List<BlockPos> toRemove = new ArrayList<>();
                NonNullList<BlockPos> copy = NonNullList.create();
                copy.addAll(positions);
                for(BlockPos position : copy) {
                    double difference = mc.player.getDistanceSq(position) - target.getDistanceSq(position);
                    if(difference > 1 || difference < -1) {
                        toRemove.add(position);
                    }
                }
                copy.removeAll(toRemove);
                if(copy.isEmpty()) {
                    copy.addAll(positions);
                }
                copy.sort(Comparator.comparingDouble(pos2 -> mc.player.getDistanceSq(pos2)));
                pos = copy.get(0);
                break;
            case FAR:
                positions.sort(Comparator.comparingDouble(pos2 -> -target.getDistanceSq(pos2)));
                pos = positions.get(0);
                break;
        }

        return pos;
    }

    private boolean canPlace(BlockPos pos) {
        if(pos == null) {
            return false;
        }

        BlockPos boost = pos.up();

        if(!isGoodMaterial(mc.world.getBlockState(pos).getBlock(), onOtherHoppers.getValue()) || !isGoodMaterial(mc.world.getBlockState(boost).getBlock(), false)) {
            return false;
        }

        if(raytrace.getValue() && (!BlockUtil.rayTracePlaceCheck(pos, raytrace.getValue()) || !BlockUtil.rayTracePlaceCheck(pos, raytrace.getValue()))) {
            return false;
        }

        if(!mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos)).isEmpty() || !mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty()) {
            return false;
        }

        if(onOtherHoppers.getValue() && mc.world.getBlockState(pos).getBlock() instanceof BlockHopper) {
            return true;
        }

        boolean hasGoodFacing = false;
        for(EnumFacing facing : EnumFacing.values()) {
            if(facing != EnumFacing.UP) {
                if(!mc.world.getBlockState(pos.offset(facing)).getMaterial().isReplaceable()) {
                    hasGoodFacing = true;
                    break;
                }
            }
        }

        return hasGoodFacing;
    }

    private void check() {
        if(currentStep != Step.PRE && currentStep != Step.HOPPER && hopperPos != null && !(mc.currentScreen instanceof GuiHopper) && !EntityUtil.holding32k(mc.player) && (mc.player.getDistanceSq(hopperPos) > MathUtil.square(hopperDistance.getValue()) || mc.world.getBlockState(hopperPos).getBlock() != Blocks.HOPPER)) {
            resetFields();
            if(!autoSwitch.getValue() || !withBind.getValue() || mode.getValue() != Mode.NORMAL) {
                disable();
            }
        }
    }

    private void checkState() {
        if(!checkStatus.getValue() || checkedThisTick || (currentStep != Step.HOPPER && currentStep != Step.SHULKER && currentStep != Step.CLICKHOPPER)) {
            checkedThisTick = false;
            return;
        }

        if(hopperPos == null || !isGoodMaterial(mc.world.getBlockState(hopperPos).getBlock(), true) || (!isGoodMaterial(mc.world.getBlockState(hopperPos.up()).getBlock(), false) && !(mc.world.getBlockState(hopperPos.up()).getBlock() instanceof BlockShulkerBox)) || !mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(hopperPos)).isEmpty() || !mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(hopperPos.up())).isEmpty()) {
            if(autoSwitch.getValue()) {
                if(switching) {
                    resetFields();
                    if(repeatSwitch.getValue()) {
                        switching = true;
                    }
                } else {
                    resetFields();
                }
                if(!withBind.getValue()) {
                    disable();
                }
            } else {
                disable();
            }
            checkedThisTick = true;
        }
    }


    /*DISPENSER32K*/


    private void processDispenser32k() {
        if(placeTimer.passedMs(delay.getValue())) {
            check();
            switch (currentStep) {
                case PRE:
                    runDispenserPreStep();
                    if(currentStep == Step.PRE) {
                        break;
                    }
                case HOPPER:
                    runHopperStep();
                    currentStep = Step.DISPENSER;
                    if((actionsThisTick >= delayDispenser.getValue() && !placeTimer.passedMs(delay.getValue()))) {
                        break;
                    }
                case DISPENSER:
                    runDispenserStep();
                    if((actionsThisTick >= delayDispenser.getValue() && !placeTimer.passedMs(delay.getValue())) || (currentStep != Step.DISPENSER_HELPING && currentStep != Step.CLICK_DISPENSER)) {
                        break;
                    }
                case DISPENSER_HELPING:
                    runDispenserStep();
                    if((actionsThisTick >= delayDispenser.getValue() && !placeTimer.passedMs(delay.getValue())) || (currentStep != Step.CLICK_DISPENSER && currentStep != Step.DISPENSER_HELPING)) {
                        break;
                    }
                case CLICK_DISPENSER:
                    clickDispenser();
                    if(actionsThisTick >= delayDispenser.getValue() && !placeTimer.passedMs(delay.getValue())) {
                        break;
                    }
                case DISPENSER_GUI:
                    dispenserGui();
                    if(currentStep == Step.DISPENSER_GUI) {
                        break;
                    }
                case REDSTONE:
                    placeRedstone();
                    if(actionsThisTick >= delayDispenser.getValue() && !placeTimer.passedMs(delay.getValue())) {
                        break;
                    }
                case CLICKHOPPER:
                    runClickHopper();
                    if(actionsThisTick >= delayDispenser.getValue() && !placeTimer.passedMs(delay.getValue())) {
                        break;
                    }
                case HOPPERGUI:
                    runHopperGuiStep();
                    if(actionsThisTick >= delayDispenser.getValue() && !placeTimer.passedMs(delay.getValue())) {
                        break;
                    }
                default:
                    break;
            }
        }
    }

    private void placeRedstone() {
        if(!mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(hopperPos.up())).isEmpty() && !(mc.world.getBlockState(hopperPos.up()).getBlock() instanceof BlockShulkerBox)) {
            return;
        }

        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
        mc.player.setSneaking(true);
        runPlaceStep(finalDispenserData.getRedStonePos(), redstoneSlot);
        currentStep = Step.CLICKHOPPER;
    }

    private void clickDispenser() {
        clickBlock(finalDispenserData.getDispenserPos());
        currentStep = Step.DISPENSER_GUI;
    }

    private void dispenserGui() {
        if(!(mc.currentScreen instanceof GuiDispenser)) {
            return;
        }

        //TODO: QUICK_MOVE can be dumb check this
        mc.playerController.windowClick(mc.player.openContainer.windowId, shulkerSlot, 0, ClickType.QUICK_MOVE, mc.player);
        mc.player.closeScreen();
        currentStep = Step.REDSTONE;
    }

    private void clickBlock(BlockPos pos) {
        mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        mc.player.setSneaking(false);
        Vec3d hitVec = new Vec3d(pos).add(0.5, -0.5, 0.5);

        if(rotate.getValue()) {
            rotateToPos(null, hitVec);
        }

        EnumFacing facing = EnumFacing.UP;
        if(finalDispenserData != null && finalDispenserData.getDispenserPos() != null && finalDispenserData.getDispenserPos().equals(pos) && pos.getY() > new BlockPos(mc.player.getPositionVector()).up().getY()) {
            facing = EnumFacing.DOWN;
        }
        BlockUtil.rightClickBlock(pos, hitVec, shulkerSlot == -2 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, facing, packet.getValue());
        mc.player.swingArm(EnumHand.MAIN_HAND);
        mc.rightClickDelayTimer = 4;
        actionsThisTick++;
    }

    private void runDispenserStep() {
        if(finalDispenserData == null || finalDispenserData.getDispenserPos() == null || finalDispenserData.getHelpingPos() == null) {
            resetFields();
            return;
        }

        if((currentStep != Step.DISPENSER && currentStep != Step.DISPENSER_HELPING)) {
            return;
        }

        BlockPos dispenserPos = finalDispenserData.getDispenserPos();
        BlockPos helpingPos = finalDispenserData.getHelpingPos();
        if(mc.world.getBlockState(helpingPos).getMaterial().isReplaceable()) {
            currentStep = Step.DISPENSER_HELPING;
            EnumFacing facing = EnumFacing.DOWN;
            boolean foundHelpingPos = false;
            for(EnumFacing enumFacing : EnumFacing.values()) {
                BlockPos position = helpingPos.offset(enumFacing);
                if(!position.equals(hopperPos)
                        && !position.equals(hopperPos.up())
                        && !position.equals(dispenserPos)
                        && !position.equals(finalDispenserData.getRedStonePos())
                        && mc.player.getDistanceSq(position) <= MathUtil.square(range.getValue())
                        && (!raytrace.getValue() || BlockUtil.rayTracePlaceCheck(position, raytrace.getValue()))
                        && !mc.world.getBlockState(position).getMaterial().isReplaceable()) {
                    foundHelpingPos = true;
                    facing = enumFacing;
                    break;
                }
            }

            if(!foundHelpingPos) {
                disable();
                return;
            }

            BlockPos neighbour = helpingPos.offset(facing);
            EnumFacing opposite = facing.getOpposite();
            Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
            Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();

            if(!mc.player.isSneaking() && (BlockUtil.blackList.contains(neighbourBlock) || BlockUtil.shulkerList.contains(neighbourBlock))) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                mc.player.setSneaking(true);
            }

            if(rotate.getValue()) {
                if(blocksPerPlace.getValue() > 1) {
                    final float[] angle = RotationUtil.getLegitRotations(hitVec);
                    //TODO: FIND SMART HITVEC HERE (SIMPLE USE mc.world.raytrace AND REMOVE BLOCKS UNTIL U HIT THE BLOCK)
                    if(extra.getValue()) {
                        RotationUtil.faceYawAndPitch(angle[0], angle[1]); //GONNA LAG BACK
                    }
                } else {
                    rotateToPos(null, hitVec);
                }
            }

            int slot = (preferObby.getValue() && obbySlot != -1) ? obbySlot : dispenserSlot;
            InventoryUtil.switchToHotbarSlot(slot, false);
            BlockUtil.rightClickBlock(neighbour, hitVec, slot == -2 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, opposite, packet.getValue());
            placeTimer.reset();
            actionsThisTick++;
            return;
        }

        placeDispenserAgainstBlock(dispenserPos, helpingPos);
        actionsThisTick++;
        currentStep = Step.CLICK_DISPENSER;
    }

    private void placeDispenserAgainstBlock(BlockPos dispenserPos, BlockPos helpingPos) {
        EnumFacing facing = EnumFacing.DOWN;
        for(EnumFacing enumFacing : EnumFacing.values()) {
            BlockPos position = dispenserPos.offset(enumFacing);
            if(position.equals(helpingPos)) {
                facing = enumFacing;
                break;
            }
        }

        EnumFacing opposite = facing.getOpposite();
        Vec3d hitVec = new Vec3d(helpingPos).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = mc.world.getBlockState(helpingPos).getBlock();
        if(!mc.player.isSneaking() && (BlockUtil.blackList.contains(neighbourBlock) || BlockUtil.shulkerList.contains(neighbourBlock))) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            mc.player.setSneaking(true);
        }

        Vec3d rotationVec = null;
        EnumFacing facings = EnumFacing.UP;
        if(rotate.getValue()) {
            if(blocksPerPlace.getValue() > 1) {
                final float[] angle = RotationUtil.getLegitRotations(hitVec);
                //TODO: FIND SMART HITVEC HERE (SIMPLE USE mc.world.raytrace AND REMOVE BLOCKS UNTIL U HIT THE BLOCK)
                if(extra.getValue()) {
                    RotationUtil.faceYawAndPitch(angle[0], angle[1]); //GONNA LAG BACK
                }
            } else {
                rotateToPos(null, hitVec);
            }
            rotationVec = new Vec3d(helpingPos).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        } else {
            if(dispenserPos.getY() <= new BlockPos(mc.player.getPositionVector()).up().getY()) {
                for(EnumFacing enumFacing : EnumFacing.values()) {
                    BlockPos position = hopperPos.up().offset(enumFacing);
                    if(position.equals(dispenserPos)) {
                        facings = enumFacing;
                        break;
                    }
                }
                rotationVec = new Vec3d(facings.getDirectionVec());
                rotateToPos(null, rotationVec);
            } else {
                rotationVec = new Vec3d(facings.getDirectionVec());
            }
        }
        final float[] angle = RotationUtil.getLegitRotations(hitVec);
        RotationUtil.faceYawAndPitch(angle[0], angle[1]);
        if(cancelNextRotation.getValue()) {
            nextRotCanceled = true;
        }

        InventoryUtil.switchToHotbarSlot(dispenserSlot, false);
        BlockUtil.rightClickBlock(helpingPos, rotationVec, dispenserSlot == -2 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, opposite, packet.getValue());
        placeTimer.reset();
        actionsThisTick++;
        currentStep = Step.CLICK_DISPENSER;
    }

    private void runDispenserPreStep() {
        if(Freecam.getInstance().isOn() && !freecam.getValue()) {
            if(messages.getValue()) {
                Command.sendMessage(TextUtil.RED + "<Auto32k> Disable Freecam.");
            }
            disable();
            return;
        }

        lastHotbarSlot = mc.player.inventory.currentItem;
        hopperSlot = InventoryUtil.findHotbarBlock(BlockHopper.class);
        shulkerSlot = InventoryUtil.findBlockSlotInventory(BlockShulkerBox.class, false, false);
        dispenserSlot = InventoryUtil.findHotbarBlock(BlockDispenser.class);
        redstoneSlot = InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK);
        obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);

        if(mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock) {
            Block block = ((ItemBlock)mc.player.getHeldItemOffhand().getItem()).getBlock();
            if(block instanceof BlockHopper) {
                hopperSlot = -2;
            } else if(block instanceof BlockDispenser) {
                dispenserSlot = -2;
            } else if(block == Blocks.REDSTONE_BLOCK) {
                redstoneSlot = -2;
            } else if(block instanceof BlockObsidian) {
                obbySlot = -2;
            }
        }

        if(shulkerSlot == -1 || hopperSlot == -1 || dispenserSlot == -1 || redstoneSlot == -1) {
            if(messages.getValue()) {
                Command.sendMessage(TextUtil.RED + "<Auto32k> Materials not found.");
            }
            disable();
            return;
        }

        finalDispenserData = findBestPos();
        if(finalDispenserData.isPlaceable()) {
            hopperPos = finalDispenserData.getHopperPos();
            if(mc.world.getBlockState(hopperPos).getBlock() instanceof BlockHopper) {
                currentStep = Step.DISPENSER;
            } else {
                currentStep = Step.HOPPER;
            }
        } else {
            if(messages.getValue()) {
                Command.sendMessage(TextUtil.RED + "<Auto32k> Block not found.");
            }
            disable();
        }
    }

    private DispenserData findBestPos() {
        PlaceType type = placeType.getValue();
        target = EntityUtil.getClosestEnemy(targetRange.getValue());
        if(target == null) {
            type = placeType.getValue() == PlaceType.MOUSE ? PlaceType.MOUSE : PlaceType.CLOSE;
        }

        NonNullList<BlockPos> positions = NonNullList.create();
        positions.addAll(BlockUtil.getSphere(EntityUtil.getPlayerPos(mc.player), range.getValue(), range.getValue().intValue(), false, true, 0));

        DispenserData data = new DispenserData();
        switch(type) {
            case MOUSE:
                if(mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK) {
                    BlockPos mousePos = mc.objectMouseOver.getBlockPos();
                    if(mousePos != null) {
                        data = analyzePos(mousePos);
                        if(!data.isPlaceable()) {
                            data = analyzePos(mousePos.up());
                        }
                    }
                }

                if(data.isPlaceable()) {
                    return data;
                }
            case CLOSE:
                positions.sort(Comparator.comparingDouble(pos2 -> mc.player.getDistanceSq(pos2)));
                break;
            case ENEMY:
                positions.sort(Comparator.comparingDouble(target::getDistanceSq));
                break;
            case MIDDLE:
                List<BlockPos> toRemove = new ArrayList<>();
                NonNullList<BlockPos> copy = NonNullList.create();
                copy.addAll(positions);
                for(BlockPos position : copy) {
                    double difference = mc.player.getDistanceSq(position) - target.getDistanceSq(position);
                    if(difference > 1 || difference < -1) {
                        toRemove.add(position);
                    }
                }
                copy.removeAll(toRemove);
                if(copy.isEmpty()) {
                    copy.addAll(positions);
                }
                copy.sort(Comparator.comparingDouble(pos2 -> mc.player.getDistanceSq(pos2)));
                break;
            case FAR:
                positions.sort(Comparator.comparingDouble(pos2 -> -target.getDistanceSq(pos2)));
                break;
        }
        data = findData(positions);
        return data;
    }

    private DispenserData findData(NonNullList<BlockPos> positions) {
        for(BlockPos position : positions) {
            DispenserData data = analyzePos(position);
            if(data.isPlaceable()) {
                return data;
            }
        }
        return new DispenserData();
    }

    private DispenserData analyzePos(BlockPos pos) {
        DispenserData data = new DispenserData(pos);
        if(pos == null) {
            return data;
        }

        if(!isGoodMaterial(mc.world.getBlockState(pos).getBlock(), onOtherHoppers.getValue())  || !isGoodMaterial(mc.world.getBlockState(pos.up()).getBlock(), false)) {
            return data;
        }

        if(raytrace.getValue() && (!BlockUtil.rayTracePlaceCheck(pos, raytrace.getValue()))) {
            return data;
        }

        if(!mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos)).isEmpty() || !mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.up())).isEmpty()) {
            return data;
        }

        if(hasAdjancedRedstone(pos)) {
            return data;
        }

        BlockPos[] otherPositions = checkForDispenserPos(pos);
        if(otherPositions[0] == null || otherPositions[1] == null || otherPositions[2] == null) {
            return data;
        }

        data.setDispenserPos(otherPositions[0]);
        data.setRedStonePos(otherPositions[1]);
        data.setHelpingPos(otherPositions[2]);
        data.setPlaceable(true);
        return data;
    }

    private BlockPos[] checkForDispenserPos(BlockPos posIn) {
        BlockPos[] pos = new BlockPos[3];
        BlockPos playerPos = new BlockPos(mc.player.getPositionVector());

        if(posIn.getY() < playerPos.down().getY()) {
            return pos;
        }

        List<BlockPos> possiblePositions = getDispenserPositions(posIn);
        if(posIn.getY() < playerPos.getY()) {
            possiblePositions.remove(posIn.up().up());
        } else if(posIn.getY() > playerPos.getY()) {
            possiblePositions.remove(posIn.west().up());
            possiblePositions.remove(posIn.north().up());
            possiblePositions.remove(posIn.south().up());
            possiblePositions.remove(posIn.east().up());
        }

        if(rotate.getValue()) {
            possiblePositions.sort(Comparator.comparingDouble(pos2 -> -mc.player.getDistanceSq(pos2)));

            BlockPos posToCheck = possiblePositions.get(0); //TODO: in some cases(diagonally for example) we can accept more positions

            if(!isGoodMaterial(mc.world.getBlockState(posToCheck).getBlock(), false)) {
                return pos;
            }

            if(mc.player.getDistanceSq(posToCheck) > MathUtil.square(range.getValue())) {
                return pos;
            }

            if(raytrace.getValue() && (!BlockUtil.rayTracePlaceCheck(posToCheck, raytrace.getValue()))) {
                return pos;
            }

            if(!mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(posToCheck)).isEmpty()) {
                return pos;
            }

            if(hasAdjancedRedstone(posToCheck)) {
                return pos;
            }

            List<BlockPos> possibleRedStonePositions = checkRedStone(posToCheck, posIn);
            if(possiblePositions.isEmpty()) {
                return pos;
            }
            BlockPos[] helpingStuff = getHelpingPos(posToCheck, posIn, possibleRedStonePositions);
            if(helpingStuff!= null && helpingStuff[0] != null && helpingStuff[1] != null) {
                pos[0] = posToCheck;
                pos[1] = helpingStuff[1]; //Redstone
                pos[2] = helpingStuff[0]; //Helping
            }
        } else {
            possiblePositions.removeIf(position -> mc.player.getDistanceSq(position) > MathUtil.square(range.getValue()));
            possiblePositions.removeIf(position -> !isGoodMaterial(mc.world.getBlockState(position).getBlock(), false));
            possiblePositions.removeIf(position -> raytrace.getValue() && (!BlockUtil.rayTracePlaceCheck(position, raytrace.getValue())));
            possiblePositions.removeIf(position -> !mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(position)).isEmpty());
            possiblePositions.removeIf(this::hasAdjancedRedstone);
            for(BlockPos position : possiblePositions) {
                List<BlockPos> possibleRedStonePositions = checkRedStone(position, posIn);
                if(possiblePositions.isEmpty()) {
                    continue;
                }
                BlockPos[] helpingStuff = getHelpingPos(position, posIn, possibleRedStonePositions);
                if(helpingStuff!= null && helpingStuff[0] != null && helpingStuff[1] != null) {
                    pos[0] = position;
                    pos[1] = helpingStuff[1]; //Redstone
                    pos[2] = helpingStuff[0]; //Helping
                    break;
                }
            }
        }

        return pos;
    }

    private List<BlockPos> checkRedStone(BlockPos pos, BlockPos hopperPos) {
        List<BlockPos> toCheck = new ArrayList<>();
        for(EnumFacing facing : EnumFacing.values()) {
            toCheck.add(pos.offset(facing));
        }

        toCheck.removeIf(position -> position.equals(hopperPos.up()));
        toCheck.removeIf(position -> mc.player.getDistanceSq(position) > MathUtil.square(range.getValue()));
        toCheck.removeIf(position -> !isGoodMaterial(mc.world.getBlockState(position).getBlock(), false));
        toCheck.removeIf(position -> raytrace.getValue() && (!BlockUtil.rayTracePlaceCheck(position, raytrace.getValue())));
        toCheck.removeIf(position -> !mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(position)).isEmpty());
        toCheck.sort(Comparator.comparingDouble(pos2 -> mc.player.getDistanceSq(pos2)));
        return toCheck;
    }

    private boolean hasAdjancedRedstone(BlockPos pos) {
        for(EnumFacing facing : EnumFacing.values()) {
            BlockPos position = pos.offset(facing);
            //TODO: WEAK CHECK, the methods for checking power of a block seem depreceated but maybe we can find something there
            if(mc.world.getBlockState(position).getBlock() == Blocks.REDSTONE_BLOCK || mc.world.getBlockState(position).getBlock() == Blocks.REDSTONE_TORCH) {
                return true;
            }
        }
        return false;
    }

    private List<BlockPos> getDispenserPositions(BlockPos pos) {
        List<BlockPos> list = new ArrayList<>();
        for(EnumFacing facing : EnumFacing.values()) {
            if(facing != EnumFacing.DOWN) {
                list.add(pos.offset(facing).up());
            }
        }
        return list;
    }

    private BlockPos[] getHelpingPos(BlockPos pos, BlockPos hopperPos, List<BlockPos> redStonePositions) {
        BlockPos[] result = new BlockPos[2];
        List<BlockPos> possiblePositions = new ArrayList<>();
        for(EnumFacing facing : EnumFacing.values()) {
            BlockPos facingPos = pos.offset(facing);
            if(!facingPos.equals(hopperPos) && !facingPos.equals(hopperPos.up())) {
                if(!mc.world.getBlockState(facingPos).getMaterial().isReplaceable()) {
                    if(redStonePositions.contains(facingPos)) {
                        redStonePositions.remove(facingPos);
                        if(redStonePositions.isEmpty()) {
                            redStonePositions.add(facingPos);
                        } else {
                            result[0] = facingPos;
                            result[1] = redStonePositions.get(0);
                            return result;
                        }
                    } else {
                        result[0] = facingPos;
                        result[1] = redStonePositions.get(0);
                        return result;
                    }
                } else {
                    for(EnumFacing facing1 : EnumFacing.values()) {
                        BlockPos facingPos1 = facingPos.offset(facing1);
                        if(!facingPos1.equals(hopperPos) && !facingPos1.equals(hopperPos.up()) && !facingPos1.equals(pos) && !mc.world.getBlockState(facingPos1).getMaterial().isReplaceable()) {
                            if(redStonePositions.contains(facingPos)) {
                                redStonePositions.remove(facingPos);
                                if(redStonePositions.isEmpty()) {
                                    redStonePositions.add(facingPos);
                                } else {
                                    possiblePositions.add(facingPos);
                                }
                            } else {
                                possiblePositions.add(facingPos);
                            }
                        }
                    }
                }
            }
        }
        possiblePositions.removeIf(position -> mc.player.getDistanceSq(position) > MathUtil.square(range.getValue()));
        possiblePositions.sort(Comparator.comparingDouble(position -> mc.player.getDistanceSq(position)));

        if(!possiblePositions.isEmpty()) {
            redStonePositions.remove(possiblePositions.get(0));
            result[0] = possiblePositions.get(0);
            result[1] = redStonePositions.get(0);
            return result;
        }

        return null;
    }


    /*UTILITY*/


    private void rotateToPos(BlockPos pos, Vec3d vec3d) {
        final float[] angle;
        if(vec3d == null) {
            angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(pos.getX() + 0.5f, pos.getY() - 0.5f, pos.getZ() + 0.5f));
        } else {
            angle = RotationUtil.getLegitRotations(vec3d);
        }
        yaw = angle[0];
        pitch = angle[1];
        spoof = true;
    }

    private boolean isGoodMaterial(Block block, boolean allowHopper) {
        return block instanceof BlockAir || block instanceof BlockLiquid || block instanceof BlockTallGrass || block instanceof BlockFire || block instanceof BlockDeadBush || block instanceof BlockSnow || (allowHopper && block instanceof BlockHopper);
    }

    private void resetFields() {
        spoof = false;
        switching = false;
        lastHotbarSlot = -1;
        shulkerSlot = -1;
        hopperSlot = -1;
        hopperPos = null;
        target = null;
        currentStep = Step.PRE;
        obbySlot = -1;
        dispenserSlot = -1;
        redstoneSlot = -1;
        finalDispenserData = null;
        actionsThisTick = 0;
        nextRotCanceled = false;
    }

    public static class DispenserData {

        private BlockPos dispenserPos;
        private BlockPos redStonePos;
        private BlockPos hopperPos;
        private BlockPos helpingPos;
        private boolean isPlaceable;

        public DispenserData() {
            isPlaceable = false;
        }

        public DispenserData(BlockPos pos) {
            isPlaceable = false;
            hopperPos = pos;
        }

        public void setPlaceable(boolean placeable) {
            isPlaceable = placeable;
        }

        public boolean isPlaceable() {
            return dispenserPos != null && hopperPos != null && redStonePos != null && helpingPos != null;
        }

        public BlockPos getDispenserPos() {
            return dispenserPos;
        }

        public void setDispenserPos(BlockPos dispenserPos) {
            dispenserPos = dispenserPos;
        }

        public BlockPos getRedStonePos() {
            return redStonePos;
        }

        public void setRedStonePos(BlockPos redStonePos) {
            redStonePos = redStonePos;
        }

        public BlockPos getHopperPos() {
            return hopperPos;
        }

        public void setHopperPos(BlockPos hopperPos) {
            hopperPos = hopperPos;
        }

        public BlockPos getHelpingPos() {
            return helpingPos;
        }

        public void setHelpingPos(BlockPos helpingPos) {
            helpingPos = helpingPos;
        }
    }

    public enum PlaceType {
        MOUSE,
        CLOSE,
        ENEMY,
        MIDDLE,
        FAR
    }

    public enum Mode {
        NORMAL, DISPENSER
    }

    public enum Step {
        PRE,
        HOPPER,
        SHULKER,
        CLICKHOPPER,
        HOPPERGUI,
        DISPENSER_HELPING,
        DISPENSER_GUI,
        DISPENSER,
        CLICK_DISPENSER,
        REDSTONE
    }
}