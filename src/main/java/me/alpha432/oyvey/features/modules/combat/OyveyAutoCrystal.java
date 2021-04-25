package me.alpha432.oyvey.features.modules.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.events.PacketEvent;
import me.alpha432.oyvey.event.events.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.modules.client.ClickGui;
import me.alpha432.oyvey.features.modules.misc.AutoGG;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.Timer;
import me.alpha432.oyvey.util.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OyveyAutoCrystal extends Module {
    private final Timer placeTimer = new Timer();
    private final Timer breakTimer = new Timer();
    private final Timer preditTimer = new Timer();
    private final Timer manualTimer = new Timer();
    private final Setting<Integer> attackFactor = register(new Setting<Integer>("PredictDelay", 0, 0, 200));
    private final Setting<Integer> red = register(new Setting<Integer>("Red", 0, 0, 255));
    private final Setting<Integer> green = register(new Setting<Integer>("Green", 255, 0, 255));
    private final Setting<Integer> blue = register(new Setting<Integer>("Blue", 0, 0, 255));
    private final Setting<Integer> alpha = register(new Setting<Integer>("Alpha", 255, 0, 255));
    private final Setting<Integer> boxAlpha = register(new Setting<Integer>("BoxAlpha", 125, 0, 255));
    private final Setting<Float> lineWidth = register(new Setting<Float>("LineWidth", Float.valueOf(1.0f), Float.valueOf(0.1f), Float.valueOf(5.0f)));
    public Setting<Boolean> place = register(new Setting<Boolean>("Place", true));
    public Setting<Float> placeDelay = register(new Setting<Float>("PlaceDelay", Float.valueOf(4.0f), Float.valueOf(0.0f), Float.valueOf(300.0f)));
    public Setting<Float> placeRange = register(new Setting<Float>("PlaceRange", Float.valueOf(4.0f), Float.valueOf(0.1f), Float.valueOf(7.0f)));
    public Setting<Boolean> explode = register(new Setting<Boolean>("Break", true));
    public Setting<Boolean> packetBreak = register(new Setting<Boolean>("PacketBreak", true));
    public Setting<Boolean> predicts = register(new Setting<Boolean>("Predict", true));
    public Setting<Boolean> rotate = register(new Setting<Boolean>("Rotate", true));
    public Setting<Float> breakDelay = register(new Setting<Float>("BreakDelay", Float.valueOf(4.0f), Float.valueOf(0.0f), Float.valueOf(300.0f)));
    public Setting<Float> breakRange = register(new Setting<Float>("BreakRange", Float.valueOf(4.0f), Float.valueOf(0.1f), Float.valueOf(7.0f)));
    public Setting<Float> breakWallRange = register(new Setting<Float>("BreakWallRange", Float.valueOf(4.0f), Float.valueOf(0.1f), Float.valueOf(7.0f)));
    public Setting<Boolean> ecmeplace = register(new Setting<Boolean>("1.13 Place", true));
    public Setting<Boolean> suicide = register(new Setting<Boolean>("AntiSuicide", true));
    public Setting<Boolean> autoswitch = register(new Setting<Boolean>("AutoSwitch", true));
    public Setting<Boolean> ignoreUseAmount = register(new Setting<Boolean>("IgnoreUseAmount", true));
    public Setting<Integer> wasteAmount = register(new Setting<Integer>("UseAmount", 4, 1, 5));
    public Setting<Boolean> facePlaceSword = register(new Setting<Boolean>("FacePlaceSword", true));
    public Setting<Boolean> removeAttack = register(new Setting("EntityRemove", false));
    public Setting<Float> targetRange = register(new Setting<Float>("TargetRange", Float.valueOf(4.0f), Float.valueOf(1.0f), Float.valueOf(12.0f)));
    public Setting<Float> minDamage = register(new Setting<Float>("MinDamage", Float.valueOf(4.0f), Float.valueOf(0.1f), Float.valueOf(20.0f)));
    public Setting<Float> facePlace = register(new Setting<Float>("FacePlaceHP", Float.valueOf(4.0f), Float.valueOf(0.0f), Float.valueOf(36.0f)));
    public Setting<Float> breakMaxSelfDamage = register(new Setting<Float>("BreakMaxSelf", Float.valueOf(4.0f), Float.valueOf(0.1f), Float.valueOf(12.0f)));
    public Setting<Float> breakMinDmg = register(new Setting<Float>("BreakMinDmg", Float.valueOf(4.0f), Float.valueOf(0.1f), Float.valueOf(7.0f)));
    public Setting<Float> minArmor = register(new Setting<Float>("MinArmor", Float.valueOf(4.0f), Float.valueOf(0.1f), Float.valueOf(80.0f)));
    public Setting<SwingMode> swingMode = register(new Setting<SwingMode>("Swing", SwingMode.MainHand));
    public Setting<Boolean> render = register(new Setting<Boolean>("Render", true));
    public Setting<Boolean> renderDmg = register(new Setting<Boolean>("RenderDmg", true));
    public Setting<Boolean> box = register(new Setting<Boolean>("Box", true));
    public Setting<Boolean> outline = register(new Setting<Boolean>("Outline", true));
    private final Setting<Integer> cRed = register(new Setting<Object>("OL-Red", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> outline.getValue()));
    private final Setting<Integer> cGreen = register(new Setting<Object>("OL-Green", Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(255), v -> outline.getValue()));
    private final Setting<Integer> cBlue = register(new Setting<Object>("OL-Blue", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> outline.getValue()));
    private final Setting<Integer> cAlpha = register(new Setting<Object>("OL-Alpha", Integer.valueOf(255), Integer.valueOf(0), Integer.valueOf(255), v -> outline.getValue()));
    EntityEnderCrystal crystal;
    private EntityLivingBase target;
    private BlockPos pos;
    private int hotBarSlot;
    private boolean armor;
    private boolean armorTarget;
    private int crystalCount;
    private int predictWait;
    private int predictPackets;
    private boolean packetCalc;
    private float yaw = 0.0f;
    public EntityLivingBase realTarget;
    private int predict;
    private float pitch = 0.0f;
    private boolean rotating = false;

    public OyveyAutoCrystal() {
        super("OyVeyAutoCrystal", "NiggaHack ac best ac", Module.Category.COMBAT, true, false, false);
    }

    public static List<BlockPos> getSphere(BlockPos loc, float r, int h, boolean hollow, boolean sphere, int plus_y) {
        ArrayList<BlockPos> circleblocks = new ArrayList<BlockPos>();
        int cx = loc.getX();
        int cy = loc.getY();
        int cz = loc.getZ();
        int x = cx - (int) r;
        while ((float) x <= (float) cx + r) {
            int z = cz - (int) r;
            while ((float) z <= (float) cz + r) {
                int y = sphere ? cy - (int) r : cy;
                while (true) {
                    float f;
                    float f2 = f = sphere ? (float) cy + r : (float) (cy + h);
                    if (!((float) y < f)) break;
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
                    if (!(!(dist < (double) (r * r)) || hollow && dist < (double) ((r - 1.0f) * (r - 1.0f)))) {
                        BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                    ++y;
                }
                ++z;
            }
            ++x;
        }
        return circleblocks;
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getStage() == 0 && rotate.getValue().booleanValue() && rotating && event.getPacket() instanceof CPacketPlayer) {
            CPacketPlayer packet = event.getPacket();
            packet.yaw = yaw;
            packet.pitch = pitch;
            rotating = false;
        }
        if (event.getStage() == 0 && event.getPacket() instanceof CPacketUseEntity) {
            CPacketUseEntity packet = event.getPacket();
            if (removeAttack.getValue()) {
                packet.getEntityFromWorld(mc.world).setDead();
                mc.world.removeEntityFromWorld(packet.entityId);

            }
        }
    }

    private void rotateTo(Entity entity) {
        if (rotate.getValue().booleanValue()) {
            float[] angle = MathUtil.calcAngle(OyveyAutoCrystal.mc.player.getPositionEyes(mc.getRenderPartialTicks()), entity.getPositionVector());
            yaw = angle[0];
            pitch = angle[1];
            rotating = true;
        }
    }

    private void rotateToPos(BlockPos pos) {
        if (rotate.getValue().booleanValue()) {
            float[] angle = MathUtil.calcAngle(OyveyAutoCrystal.mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d((float) pos.getX() + 0.5f, (float) pos.getY() - 0.5f, (float) pos.getZ() + 0.5f));
            yaw = angle[0];
            pitch = angle[1];
            rotating = true;
        }
    }

    @Override
    public void onEnable() {
        placeTimer.reset();
        breakTimer.reset();
        predictWait = 0;
        hotBarSlot = -1;
        pos = null;
        crystal = null;
        predict = 0;
        predictPackets = 1;
        target = null;
        packetCalc = false;
        realTarget = null;
        armor = false;
        armorTarget = false;
    }

    @Override
    public void onDisable() {
        rotating = false;
    }

    @Override
    public void onTick() {
        onCrystal();
    }

    @Override
    public String getDisplayInfo() {
        if (realTarget != null) {
            return realTarget.getName();
        }
        return null;
    }

    public void onCrystal() {
        if (OyveyAutoCrystal.mc.world == null || OyveyAutoCrystal.mc.player == null) {
            return;
        }
        realTarget = null;
        manualBreaker();
        crystalCount = 0;
        if (!ignoreUseAmount.getValue().booleanValue()) {
            for (Entity crystal : OyveyAutoCrystal.mc.world.loadedEntityList) {
                if (!(crystal instanceof EntityEnderCrystal) || !IsValidCrystal(crystal)) continue;
                boolean count = false;
                double damage = calculateDamage((double) target.getPosition().getX() + 0.5, (double) target.getPosition().getY() + 1.0, (double) target.getPosition().getZ() + 0.5, target);
                if (damage >= (double) minDamage.getValue().floatValue()) {
                    count = true;
                }
                if (!count) continue;
                ++crystalCount;
            }
        }
        hotBarSlot = -1;
        if (OyveyAutoCrystal.mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL) {
            int crystalSlot;
            int n = crystalSlot = OyveyAutoCrystal.mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL ? OyveyAutoCrystal.mc.player.inventory.currentItem : -1;
            if (crystalSlot == -1) {
                for (int l = 0; l < 9; ++l) {
                    if (OyveyAutoCrystal.mc.player.inventory.getStackInSlot(l).getItem() != Items.END_CRYSTAL) continue;
                    crystalSlot = l;
                    hotBarSlot = l;
                    break;
                }
            }
            if (crystalSlot == -1) {
                pos = null;
                target = null;
                return;
            }
        }
        if (OyveyAutoCrystal.mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE && OyveyAutoCrystal.mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL) {
            pos = null;
            target = null;
            return;
        }
        if (target == null) {
            target = getTarget();
        }
        if (target == null) {
            crystal = null;
            return;
        }
        if (target.getDistance(OyveyAutoCrystal.mc.player) > 12.0f) {
            crystal = null;
            target = null;
        }
        crystal = OyveyAutoCrystal.mc.world.loadedEntityList.stream().filter(this::IsValidCrystal).map(p_Entity -> (EntityEnderCrystal) p_Entity).min(Comparator.comparing(p_Entity -> Float.valueOf(target.getDistance((Entity) p_Entity)))).orElse(null);
        if (crystal != null && explode.getValue().booleanValue() && breakTimer.passedMs(breakDelay.getValue().longValue())) {
            breakTimer.reset();
            if (packetBreak.getValue().booleanValue()) {
                rotateTo(crystal);
                OyveyAutoCrystal.mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
            } else {
                rotateTo(crystal);
                OyveyAutoCrystal.mc.playerController.attackEntity(OyveyAutoCrystal.mc.player, crystal);
            }
            if (swingMode.getValue() == SwingMode.MainHand) {
                OyveyAutoCrystal.mc.player.swingArm(EnumHand.MAIN_HAND);
            } else if (swingMode.getValue() == SwingMode.OffHand) {
                OyveyAutoCrystal.mc.player.swingArm(EnumHand.OFF_HAND);
            }
        }
        if (placeTimer.passedMs(placeDelay.getValue().longValue()) && place.getValue().booleanValue()) {
            placeTimer.reset();
            double damage = 0.5;
            for (BlockPos blockPos : placePostions(placeRange.getValue().floatValue())) {
                double selfDmg;
                double targetRange;
                if (blockPos == null || target == null || !OyveyAutoCrystal.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(blockPos)).isEmpty() || (targetRange = target.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ())) > (double) this.targetRange.getValue().floatValue() || target.isDead || target.getHealth() + target.getAbsorptionAmount() <= 0.0f)
                    continue;
                double targetDmg = calculateDamage((double) blockPos.getX() + 0.5, (double) blockPos.getY() + 1.0, (double) blockPos.getZ() + 0.5, target);
                armor = false;
                for (ItemStack is : target.getArmorInventoryList()) {
                    float green = ((float) is.getMaxDamage() - (float) is.getItemDamage()) / (float) is.getMaxDamage();
                    float red = 1.0f - green;
                    int dmg = 100 - (int) (red * 100.0f);
                    if (!((float) dmg <= minArmor.getValue().floatValue())) continue;
                    armor = true;
                }
                if (targetDmg < (double) minDamage.getValue().floatValue() && (facePlaceSword.getValue() != false ? target.getAbsorptionAmount() + target.getHealth() > facePlace.getValue().floatValue() : OyveyAutoCrystal.mc.player.getHeldItemMainhand().getItem() instanceof ItemSword || target.getAbsorptionAmount() + target.getHealth() > facePlace.getValue().floatValue()) && (facePlaceSword.getValue() != false ? !armor : OyveyAutoCrystal.mc.player.getHeldItemMainhand().getItem() instanceof ItemSword || !armor) || (selfDmg = calculateDamage((double) blockPos.getX() + 0.5, (double) blockPos.getY() + 1.0, (double) blockPos.getZ() + 0.5, OyveyAutoCrystal.mc.player)) + (suicide.getValue() != false ? 2.0 : 0.5) >= (double) (OyveyAutoCrystal.mc.player.getHealth() + OyveyAutoCrystal.mc.player.getAbsorptionAmount()) && selfDmg >= targetDmg && targetDmg < (double) (target.getHealth() + target.getAbsorptionAmount()) || !(damage < targetDmg))
                    continue;
                pos = blockPos;
                damage = targetDmg;
            }
            if (damage == 0.5) {
                pos = null;
                target = null;
                realTarget = null;
                return;
            }
            realTarget = target;
            if (AutoGG.getINSTANCE().isOn()) {
                AutoGG autoGG = (AutoGG) OyVey.moduleManager.getModuleByName("AutoGG");
                autoGG.addTargetedPlayer(target.getName());
            }
            if (hotBarSlot != -1 && autoswitch.getValue().booleanValue() && !OyveyAutoCrystal.mc.player.isPotionActive(MobEffects.WEAKNESS)) {
                OyveyAutoCrystal.mc.player.inventory.currentItem = hotBarSlot;
            }
            if (!ignoreUseAmount.getValue().booleanValue()) {
                int crystalLimit = wasteAmount.getValue();
                if (crystalCount >= crystalLimit) {
                    return;
                }
                if (damage < (double) minDamage.getValue().floatValue()) {
                    crystalLimit = 1;
                }
                if (crystalCount < crystalLimit && pos != null) {
                    rotateToPos(pos);
                    OyveyAutoCrystal.mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, EnumFacing.UP, OyveyAutoCrystal.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
                }
            } else if (pos != null) {
                rotateToPos(pos);
                OyveyAutoCrystal.mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, EnumFacing.UP, OyveyAutoCrystal.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onPacketReceive(PacketEvent.Receive event) {
        SPacketSpawnObject packet;
        if (event.getPacket() instanceof SPacketSpawnObject && (packet = event.getPacket()).getType() == 51 && predicts.getValue().booleanValue() && preditTimer.passedMs(attackFactor.getValue().longValue()) && predicts.getValue().booleanValue() && explode.getValue().booleanValue() && packetBreak.getValue().booleanValue() && target != null) {
            if (!isPredicting(packet)) {
                return;
            }
            CPacketUseEntity predict = new CPacketUseEntity();
            predict.entityId = packet.getEntityID();
            predict.action = CPacketUseEntity.Action.ATTACK;
            OyveyAutoCrystal.mc.player.connection.sendPacket(predict);
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (pos != null && render.getValue().booleanValue() && target != null) {
            RenderUtil.drawBoxESP(pos, ClickGui.getInstance().rainbow.getValue() != false ? ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()) : new Color(red.getValue(), green.getValue(), blue.getValue(), alpha.getValue()), outline.getValue(), ClickGui.getInstance().rainbow.getValue() != false ? ColorUtil.rainbow(ClickGui.getInstance().rainbowHue.getValue()) : new Color(cRed.getValue(), cGreen.getValue(), cBlue.getValue(), cAlpha.getValue()), lineWidth.getValue().floatValue(), outline.getValue(), box.getValue(), boxAlpha.getValue(), true);
            if (renderDmg.getValue().booleanValue()) {
                double renderDamage = calculateDamage((double) pos.getX() + 0.5, (double) pos.getY() + 1.0, (double) pos.getZ() + 0.5, target);
                RenderUtil.drawText(pos, (Math.floor(renderDamage) == renderDamage ? Integer.valueOf((int) renderDamage) : String.format( ChatFormatting.WHITE + "%.1f", renderDamage)) + "");
            }
        }
    }

    private boolean isPredicting(SPacketSpawnObject packet) {
        BlockPos packPos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
        if (OyveyAutoCrystal.mc.player.getDistance(packet.getX(), packet.getY(), packet.getZ()) > (double) breakRange.getValue().floatValue()) {
            return false;
        }
        if (!canSeePos(packPos) && OyveyAutoCrystal.mc.player.getDistance(packet.getX(), packet.getY(), packet.getZ()) > (double) breakWallRange.getValue().floatValue()) {
            return false;
        }
        double targetDmg = calculateDamage(packet.getX() + 0.5, packet.getY() + 1.0, packet.getZ() + 0.5, target);
        if (EntityUtil.isInHole(OyveyAutoCrystal.mc.player) && targetDmg >= 1.0) {
            return true;
        }
        double selfDmg = calculateDamage(packet.getX() + 0.5, packet.getY() + 1.0, packet.getZ() + 0.5, OyveyAutoCrystal.mc.player);
        double d = suicide.getValue() != false ? 2.0 : 0.5;
        if (selfDmg + d < (double) (OyveyAutoCrystal.mc.player.getHealth() + OyveyAutoCrystal.mc.player.getAbsorptionAmount()) && targetDmg >= (double) (target.getAbsorptionAmount() + target.getHealth())) {
            return true;
        }
        armorTarget = false;
        for (ItemStack is : target.getArmorInventoryList()) {
            float green = ((float) is.getMaxDamage() - (float) is.getItemDamage()) / (float) is.getMaxDamage();
            float red = 1.0f - green;
            int dmg = 100 - (int) (red * 100.0f);
            if (!((float) dmg <= minArmor.getValue().floatValue())) continue;
            armorTarget = true;
        }
        if (targetDmg >= (double) breakMinDmg.getValue().floatValue() && selfDmg <= (double) breakMaxSelfDamage.getValue().floatValue()) {
            return true;
        }
        return EntityUtil.isInHole(target) && target.getHealth() + target.getAbsorptionAmount() <= facePlace.getValue().floatValue();
    }

    private boolean IsValidCrystal(Entity p_Entity) {
        if (p_Entity == null) {
            return false;
        }
        if (!(p_Entity instanceof EntityEnderCrystal)) {
            return false;
        }
        if (target == null) {
            return false;
        }
        if (p_Entity.getDistance(OyveyAutoCrystal.mc.player) > breakRange.getValue().floatValue()) {
            return false;
        }
        if (!OyveyAutoCrystal.mc.player.canEntityBeSeen(p_Entity) && p_Entity.getDistance(OyveyAutoCrystal.mc.player) > breakWallRange.getValue().floatValue()) {
            return false;
        }
        if (target.isDead || target.getHealth() + target.getAbsorptionAmount() <= 0.0f) {
            return false;
        }
        double targetDmg = calculateDamage((double) p_Entity.getPosition().getX() + 0.5, (double) p_Entity.getPosition().getY() + 1.0, (double) p_Entity.getPosition().getZ() + 0.5, target);
        if (EntityUtil.isInHole(OyveyAutoCrystal.mc.player) && targetDmg >= 1.0) {
            return true;
        }
        double selfDmg = calculateDamage((double) p_Entity.getPosition().getX() + 0.5, (double) p_Entity.getPosition().getY() + 1.0, (double) p_Entity.getPosition().getZ() + 0.5, OyveyAutoCrystal.mc.player);
        double d = suicide.getValue() != false ? 2.0 : 0.5;
        if (selfDmg + d < (double) (OyveyAutoCrystal.mc.player.getHealth() + OyveyAutoCrystal.mc.player.getAbsorptionAmount()) && targetDmg >= (double) (target.getAbsorptionAmount() + target.getHealth())) {
            return true;
        }
        armorTarget = false;
        for (ItemStack is : target.getArmorInventoryList()) {
            float green = ((float) is.getMaxDamage() - (float) is.getItemDamage()) / (float) is.getMaxDamage();
            float red = 1.0f - green;
            int dmg = 100 - (int) (red * 100.0f);
            if (!((float) dmg <= minArmor.getValue().floatValue())) continue;
            armorTarget = true;
        }
        if (targetDmg >= (double) breakMinDmg.getValue().floatValue() && selfDmg <= (double) breakMaxSelfDamage.getValue().floatValue()) {
            return true;
        }
        return EntityUtil.isInHole(target) && target.getHealth() + target.getAbsorptionAmount() <= facePlace.getValue().floatValue();
    }

    EntityPlayer getTarget() {
        EntityPlayer closestPlayer = null;
        for (EntityPlayer entity : OyveyAutoCrystal.mc.world.playerEntities) {
            if (OyveyAutoCrystal.mc.player == null || OyveyAutoCrystal.mc.player.isDead || entity.isDead || entity == OyveyAutoCrystal.mc.player || OyVey.friendManager.isFriend(entity.getName()) || entity.getDistance(OyveyAutoCrystal.mc.player) > 12.0f)
                continue;
            armorTarget = false;
            for (ItemStack is : entity.getArmorInventoryList()) {
                float green = ((float) is.getMaxDamage() - (float) is.getItemDamage()) / (float) is.getMaxDamage();
                float red = 1.0f - green;
                int dmg = 100 - (int) (red * 100.0f);
                if (!((float) dmg <= minArmor.getValue().floatValue())) continue;
                armorTarget = true;
            }
            if (EntityUtil.isInHole(entity) && entity.getAbsorptionAmount() + entity.getHealth() > facePlace.getValue().floatValue() && !armorTarget && minDamage.getValue().floatValue() > 2.2f)
                continue;
            if (closestPlayer == null) {
                closestPlayer = entity;
                continue;
            }
            if (!(closestPlayer.getDistance(OyveyAutoCrystal.mc.player) > entity.getDistance(OyveyAutoCrystal.mc.player)))
                continue;
            closestPlayer = entity;
        }
        return closestPlayer;
    }

    private void manualBreaker() {
        RayTraceResult result;
        if (manualTimer.passedMs(200L) && OyveyAutoCrystal.mc.gameSettings.keyBindUseItem.isKeyDown() && OyveyAutoCrystal.mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE && OyveyAutoCrystal.mc.player.inventory.getCurrentItem().getItem() != Items.GOLDEN_APPLE && OyveyAutoCrystal.mc.player.inventory.getCurrentItem().getItem() != Items.BOW && OyveyAutoCrystal.mc.player.inventory.getCurrentItem().getItem() != Items.EXPERIENCE_BOTTLE && (result = OyveyAutoCrystal.mc.objectMouseOver) != null) {
            if (result.typeOfHit.equals(RayTraceResult.Type.ENTITY)) {
                Entity entity = result.entityHit;
                if (entity instanceof EntityEnderCrystal) {
                    if (packetBreak.getValue().booleanValue()) {
                        OyveyAutoCrystal.mc.player.connection.sendPacket(new CPacketUseEntity(entity));
                    } else {
                        OyveyAutoCrystal.mc.playerController.attackEntity(OyveyAutoCrystal.mc.player, entity);
                    }
                    manualTimer.reset();
                }
            } else if (result.typeOfHit.equals(RayTraceResult.Type.BLOCK)) {
                BlockPos mousePos = new BlockPos(OyveyAutoCrystal.mc.objectMouseOver.getBlockPos().getX(), (double) OyveyAutoCrystal.mc.objectMouseOver.getBlockPos().getY() + 1.0, OyveyAutoCrystal.mc.objectMouseOver.getBlockPos().getZ());
                for (Entity target : OyveyAutoCrystal.mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(mousePos))) {
                    if (!(target instanceof EntityEnderCrystal)) continue;
                    if (packetBreak.getValue().booleanValue()) {
                        OyveyAutoCrystal.mc.player.connection.sendPacket(new CPacketUseEntity(target));
                    } else {
                        OyveyAutoCrystal.mc.playerController.attackEntity(OyveyAutoCrystal.mc.player, target);
                    }
                    manualTimer.reset();
                }
            }
        }
    }

    private boolean canSeePos(BlockPos pos) {
        return OyveyAutoCrystal.mc.world.rayTraceBlocks(new Vec3d(OyveyAutoCrystal.mc.player.posX, OyveyAutoCrystal.mc.player.posY + (double) OyveyAutoCrystal.mc.player.getEyeHeight(), OyveyAutoCrystal.mc.player.posZ), new Vec3d(pos.getX(), pos.getY(), pos.getZ()), false, true, false) == null;
    }

    private NonNullList<BlockPos> placePostions(float placeRange) {
        NonNullList positions = NonNullList.create();
        positions.addAll(OyveyAutoCrystal.getSphere(new BlockPos(Math.floor(OyveyAutoCrystal.mc.player.posX), Math.floor(OyveyAutoCrystal.mc.player.posY), Math.floor(OyveyAutoCrystal.mc.player.posZ)), placeRange, (int) placeRange, false, true, 0).stream().filter(pos -> canPlaceCrystal(pos, true)).collect(Collectors.toList()));
        return positions;
    }

    private boolean canPlaceCrystal(BlockPos blockPos, boolean specialEntityCheck) {
        BlockPos boost = blockPos.add(0, 1, 0);
        BlockPos boost2 = blockPos.add(0, 2, 0);
        try {
            if (!ecmeplace.getValue().booleanValue()) {
                if (OyveyAutoCrystal.mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK && OyveyAutoCrystal.mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN) {
                    return false;
                }
                if (OyveyAutoCrystal.mc.world.getBlockState(boost).getBlock() != Blocks.AIR || OyveyAutoCrystal.mc.world.getBlockState(boost2).getBlock() != Blocks.AIR) {
                    return false;
                }
                if (!specialEntityCheck) {
                    return OyveyAutoCrystal.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty() && OyveyAutoCrystal.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty();
                }
                for (Entity entity : OyveyAutoCrystal.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost))) {
                    if (entity instanceof EntityEnderCrystal) continue;
                    return false;
                }
                for (Entity entity : OyveyAutoCrystal.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2))) {
                    if (entity instanceof EntityEnderCrystal) continue;
                    return false;
                }
            } else {
                if (OyveyAutoCrystal.mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK && OyveyAutoCrystal.mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN) {
                    return false;
                }
                if (OyveyAutoCrystal.mc.world.getBlockState(boost).getBlock() != Blocks.AIR) {
                    return false;
                }
                if (!specialEntityCheck) {
                    return OyveyAutoCrystal.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty();
                }
                for (Entity entity : OyveyAutoCrystal.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost))) {
                    if (entity instanceof EntityEnderCrystal) continue;
                    return false;
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    private float calculateDamage(double posX, double posY, double posZ, Entity entity) {
        float doubleExplosionSize = 12.0f;
        double distancedsize = entity.getDistance(posX, posY, posZ) / 12.0;
        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = 0.0;
        try {
            blockDensity = entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
        } catch (Exception exception) {
            // empty catch block
        }
        double v = (1.0 - distancedsize) * blockDensity;
        float damage = (int) ((v * v + v) / 2.0 * 7.0 * 12.0 + 1.0);
        double finald = 1.0;
        if (entity instanceof EntityLivingBase) {
            finald = getBlastReduction((EntityLivingBase) entity, getDamageMultiplied(damage), new Explosion(OyveyAutoCrystal.mc.world, null, posX, posY, posZ, 6.0f, false, true));
        }
        return (float) finald;
    }

    private float getBlastReduction(EntityLivingBase entity, float damageI, Explosion explosion) {
        float damage = damageI;
        if (entity instanceof EntityPlayer) {
            EntityPlayer ep = (EntityPlayer) entity;
            DamageSource ds = DamageSource.causeExplosionDamage(explosion);
            damage = CombatRules.getDamageAfterAbsorb(damage, (float) ep.getTotalArmorValue(), (float) ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
            int k = 0;
            try {
                k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
            } catch (Exception exception) {
                // empty catch block
            }
            float f = MathHelper.clamp((float) k, 0.0f, 20.0f);
            damage *= 1.0f - f / 25.0f;
            if (entity.isPotionActive(MobEffects.RESISTANCE)) {
                damage -= damage / 4.0f;
            }
            damage = Math.max(damage, 0.0f);
            return damage;
        }
        damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getTotalArmorValue(), (float) entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
        return damage;
    }

    private float getDamageMultiplied(float damage) {
        int diff = OyveyAutoCrystal.mc.world.getDifficulty().getId();
        return damage * (diff == 0 ? 0.0f : (diff == 2 ? 1.0f : (diff == 1 ? 0.5f : 1.5f)));
    }

    public enum SwingMode {
        MainHand,
        OffHand,
        None

    }
}

