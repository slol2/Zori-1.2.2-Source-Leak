package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.event.events.Render3DEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import me.alpha432.oyvey.util.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;

/**
 * @Author Novola & zopac
 * @Version v1.0.4/b4
 * @Date 28/1/2021 (1/28/2021)
 */

public class NameTags extends Module {

    private final Setting<Boolean> rect = register(new Setting("Rectangle", true));
    private final Setting<Boolean> armor = register(new Setting("Armor", true));
    private final Setting<Boolean> reversed = register(new Setting("ArmorReversed", false, v -> armor.getValue()));
    private final Setting<Boolean> health = register(new Setting("Health", true));
    private final Setting<Boolean> ping = register(new Setting("Ping", true));
    private final Setting<Boolean> gamemode = register(new Setting("Gamemode", false));
    private final Setting<Boolean> entityID = register(new Setting("EntityID", false));
    private final Setting<Boolean> heldStackName = register(new Setting("StackName", true));

    private final Setting<Boolean> max = register(new Setting("Max", true));
    private final Setting<Boolean> maxText = register(new Setting("NoMaxText", false, v -> max.getValue()));
    private final Setting<Integer> Mred = register(new Setting("Max-Red", 178, 0, 255, v -> max.getValue()));
    private final Setting<Integer> Mgreen = register(new Setting("Max-Green", 52, 0, 255, v -> max.getValue()));
    private final Setting<Integer> Mblue = register(new Setting("Max-Blue", 57, 0, 255, v -> max.getValue()));

    private final Setting<Float> size = register(new Setting("Size", 0.3f, 0.1f, 20.0f));
    private final Setting<Boolean> scaleing = register(new Setting("Scale", false));
    private final Setting<Boolean> smartScale = register(new Setting("SmartScale", false, v -> scaleing.getValue()));
    private final Setting<Float> factor = register(new Setting("Factor", 0.3f, 0.1f, 1.0f, v -> scaleing.getValue()));

    private final Setting<Boolean> textcolor = register(new Setting("TextColor", true));
    private final Setting<Boolean> NCRainbow = register(new Setting("Text-Rainbow", false, v -> textcolor.getValue()));
    private final Setting<Integer> NCred = register(new Setting("Text-Red", 255, 0, 255, v -> textcolor.getValue()));
    private final Setting<Integer> NCgreen = register(new Setting("Text-Green", 255, 0, 255, v -> textcolor.getValue()));
    private final Setting<Integer> NCblue = register(new Setting("Text-Blue", 255, 0, 255, v -> textcolor.getValue()));

    private final Setting<Boolean> outline = register(new Setting("Outline", true));
    private final Setting<Boolean> ORainbow = register(new Setting("Outline-Rainbow", false, v -> outline.getValue()));
    private final Setting<Float> Owidth = register(new Setting("Outline-Width", 1.3f, 0f, 5f, v -> outline.getValue()));
    private final Setting<Integer> Ored = register(new Setting("Outline-Red", 255, 0, 255, v -> outline.getValue()));
    private final Setting<Integer> Ogreen = register(new Setting("Outline-Green", 255, 0, 255, v -> outline.getValue()));
    private final Setting<Integer> Oblue = register(new Setting("Outline-Blue", 255, 0, 255, v -> outline.getValue()));

    private final Setting<Boolean> friendcolor = register(new Setting("FriendColor", true));
    private final Setting<Boolean> FCRainbow = register(new Setting("Friend-Rainbow", false, v -> friendcolor.getValue()));
    private final Setting<Integer> FCred = register(new Setting("Friend-Red", 0, 0, 255, v -> friendcolor.getValue()));
    private final Setting<Integer> FCgreen = register(new Setting("Friend-Green", 213, 0, 255, v -> friendcolor.getValue()));
    private final Setting<Integer> FCblue = register(new Setting("Friend-Blue", 255, 0, 255, v -> friendcolor.getValue()));
    private final Setting<Boolean> FORainbow = register(new Setting("FriendOutline-Rainbow", false, v -> outline.getValue() && friendcolor.getValue()));
    private final Setting<Integer> FOred = register(new Setting("FriendOutline-Red", 0, 0, 255, v -> outline.getValue() && friendcolor.getValue()));
    private final Setting<Integer> FOgreen = register(new Setting("FriendOutline-Green", 213, 0, 255, v -> outline.getValue() && friendcolor.getValue()));
    private final Setting<Integer> FOblue = register(new Setting("FriendOutline-Blue", 255, 0, 255, v -> outline.getValue() && friendcolor.getValue()));

    private final Setting<Boolean> sneakcolor = register(new Setting("Sneak", false));
    private final Setting<Boolean> sneak = register(new Setting("EnableSneak", true, v -> sneakcolor.getValue()));
    private final Setting<Boolean> SCRainbow = register(new Setting("Sneak-Rainbow", false, v -> sneakcolor.getValue()));
    private final Setting<Integer> SCred = register(new Setting("Sneak-Red", 245, 0, 255, v -> sneakcolor.getValue()));
    private final Setting<Integer> SCgreen = register(new Setting("Sneak-Green", 0, 0, 255, v -> sneakcolor.getValue()));
    private final Setting<Integer> SCblue = register(new Setting("Sneak-Blue", 122, 0, 255, v -> sneakcolor.getValue()));
    private final Setting<Boolean> SORainbow = register(new Setting("SneakOutline-Rainbow", false, v -> outline.getValue() && sneakcolor.getValue()));
    private final Setting<Integer> SOred = register(new Setting("SneakOutline-Red", 245, 0, 255, v -> outline.getValue() && sneakcolor.getValue()));
    private final Setting<Integer> SOgreen = register(new Setting("SneakOutline-Green", 0, 0, 255, v -> outline.getValue() && sneakcolor.getValue()));
    private final Setting<Integer> SOblue = register(new Setting("SneakOutline-Blue", 122, 0, 255, v -> outline.getValue() && sneakcolor.getValue()));

    private final Setting<Boolean> invisiblescolor = register(new Setting("InvisiblesColor", false));
    private final Setting<Boolean> invisibles = register(new Setting("EnableInvisibles", true, v -> invisiblescolor.getValue()));
    private final Setting<Boolean> ICRainbow = register(new Setting("Invisible-Rainbow", false, v -> invisiblescolor.getValue()));
    private final Setting<Integer> ICred = register(new Setting("Invisible-Red", 148, 0, 255, v -> invisiblescolor.getValue()));
    private final Setting<Integer> ICgreen = register(new Setting("Invisible-Green", 148, 0, 255, v -> invisiblescolor.getValue()));
    private final Setting<Integer> ICblue = register(new Setting("Invisible-Blue", 148, 0, 255, v -> invisiblescolor.getValue()));
    private final Setting<Boolean> IORainbow = register(new Setting("InvisibleOutline-Rainbow", false, v -> outline.getValue() && invisiblescolor.getValue()));
    private final Setting<Integer> IOred = register(new Setting("InvisibleOutline-Red", 148, 0, 255, v -> outline.getValue() && invisiblescolor.getValue()));
    private final Setting<Integer> IOgreen = register(new Setting("InvisibleOutline-Green", 148, 0, 255, v -> outline.getValue() && invisiblescolor.getValue()));
    private final Setting<Integer> IOblue = register(new Setting("InvisibleOutline-Blue", 148, 0, 255, v -> outline.getValue() && invisiblescolor.getValue()));

    private static NameTags INSTANCE = new NameTags();

    public NameTags() {
        super("NameTags", "Renders info about the player on a NameTag", Module.Category.RENDER, false, false, false);
    }

    public static NameTags getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NameTags();
        }
        return INSTANCE;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        for (EntityPlayer player : mc.world.playerEntities) {
            if (player != null && !player.equals(mc.player) && player.isEntityAlive() && (!player.isInvisible() || invisibles.getValue())) {
                double x = interpolate(player.lastTickPosX, player.posX, event.getPartialTicks()) - mc.getRenderManager().renderPosX;
                double y = interpolate(player.lastTickPosY, player.posY, event.getPartialTicks()) - mc.getRenderManager().renderPosY;
                double z = interpolate(player.lastTickPosZ, player.posZ, event.getPartialTicks()) - mc.getRenderManager().renderPosZ;
                renderNameTag(player, x, y, z, event.getPartialTicks());
            }
        }
    }

    private void renderNameTag(EntityPlayer player, double x, double y, double z, float delta) {
        double tempY = y;
        tempY += (player.isSneaking() ? 0.5D : 0.7D);
        Entity camera = mc.getRenderViewEntity();
        assert camera != null;
        double originalPositionX = camera.posX;
        double originalPositionY = camera.posY;
        double originalPositionZ = camera.posZ;
        camera.posX = interpolate(camera.prevPosX, camera.posX, delta);
        camera.posY = interpolate(camera.prevPosY, camera.posY, delta);
        camera.posZ = interpolate(camera.prevPosZ, camera.posZ, delta);

        String displayTag = getDisplayTag(player);
        double distance = camera.getDistance(x + mc.getRenderManager().viewerPosX, y + mc.getRenderManager().viewerPosY, z + mc.getRenderManager().viewerPosZ);
        int width = renderer.getStringWidth(displayTag) / 2;
        double scale = (0.0018 + size.getValue() * (distance * factor.getValue())) / 1000.0;

        if (distance <= 8 && smartScale.getValue()) {
            scale = 0.0245D;
        }

        if (!scaleing.getValue()) {
            scale = size.getValue() / 100.0;
        }

        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1, -1500000);
        GlStateManager.disableLighting();
        GlStateManager.translate((float) x, (float) tempY + 1.4F, (float) z);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.enableBlend();
        if (this.rect.getValue()) {
            this.drawRect(-width - 2, -(NameTags.mc.fontRenderer.FONT_HEIGHT + 1), (float) width + 2.0f, 1.5f, 0x55000000);
        }
        if (outline.getValue()) {
            this.drawOutlineRect(-width - 2, -(NameTags.mc.fontRenderer.FONT_HEIGHT + 1), (float) width + 2.0f, 1.5f, getOutlineColor(player));
        }
        GlStateManager.disableBlend();
        ItemStack renderMainHand = player.getHeldItemMainhand().copy();
        if (renderMainHand.hasEffect() && (renderMainHand.getItem() instanceof ItemTool || renderMainHand.getItem() instanceof ItemArmor)) {
            renderMainHand.stackSize = 1;
        }

        if (heldStackName.getValue() && !renderMainHand.isEmpty && renderMainHand.getItem() != Items.AIR) {
            String stackName = renderMainHand.getDisplayName();
            int stackNameWidth = renderer.getStringWidth(stackName) / 2;
            GL11.glPushMatrix();
            GL11.glScalef(0.75f, 0.75f, 0);
            renderer.drawStringWithShadow(stackName, -stackNameWidth, -(getBiggestArmorTag(player) + 20), 0xFFFFFFFF);
            GL11.glScalef(1.5f, 1.5f, 1);
            GL11.glPopMatrix();
        }

        if (armor.getValue()) {
            GlStateManager.pushMatrix();
            int xOffset = -6;
            int count = 0;
            for (ItemStack armourStack : player.inventory.armorInventory) {
                if (armourStack != null) {
                    xOffset -= 8;
                    if (armourStack.getItem() != Items.AIR) ++count;
                }
            }

            xOffset -= 8;
            ItemStack renderOffhand = player.getHeldItemOffhand().copy();
            if (renderOffhand.hasEffect() && (renderOffhand.getItem() instanceof ItemTool || renderOffhand.getItem() instanceof ItemArmor)) {
                renderOffhand.stackSize = 1;
            }

            renderItemStack(renderOffhand, xOffset, -26);
            xOffset += 16;

            if (reversed.getValue()) {
                for (int index = 0; index <= 3; ++index) {
                    ItemStack armourStack = player.inventory.armorInventory.get(index);
                    if (armourStack != null && armourStack.getItem() != Items.AIR) {
                        ItemStack renderStack1 = armourStack.copy();

                        renderItemStack(armourStack, xOffset, -26);
                        xOffset += 16;
                    }
                }
            } else {
                for (int index = 3; index >= 0; --index) {
                    ItemStack armourStack = player.inventory.armorInventory.get(index);
                    if (armourStack != null && armourStack.getItem() != Items.AIR) {
                        ItemStack renderStack1 = armourStack.copy();

                        renderItemStack(armourStack, xOffset, -26);
                        xOffset += 16;
                    }
                }
            }

            renderItemStack(renderMainHand, xOffset, -26);

            GlStateManager.popMatrix();
        }

        renderer.drawStringWithShadow(displayTag, -width, -(renderer.getFontHeight() - 1), getDisplayColor(player));

        camera.posX = originalPositionX;
        camera.posY = originalPositionY;
        camera.posZ = originalPositionZ;
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1, 1500000);
        GlStateManager.popMatrix();
    }

    private int getDisplayColor(EntityPlayer player) {
        int displaycolor = ColorHolder.toHex(NCred.getValue(), NCgreen.getValue(), NCblue.getValue());
        if (OyVey.friendManager.isFriend(player) ) {
            return ColorHolder.toHex(FCred.getValue(), FCgreen.getValue(), FCblue.getValue());
        } else if (player.isInvisible() && invisibles.getValue()) {
            displaycolor = ColorHolder.toHex(ICred.getValue(), ICgreen.getValue(), ICblue.getValue());
        } else if (player.isSneaking() && sneak.getValue()) {
            displaycolor = ColorHolder.toHex(SCred.getValue(), SCgreen.getValue(), SCblue.getValue());
        }
        return displaycolor;
    }

    private int getOutlineColor(EntityPlayer player) {
        int outlinecolor = ColorHolder.toHex(Ored.getValue(), Ogreen.getValue(), Oblue.getValue());
        if (OyVey.friendManager.isFriend(player)) {
            outlinecolor = ColorHolder.toHex(FOred.getValue(), FOgreen.getValue(), FOblue.getValue());
        } else if (player.isInvisible() && invisibles.getValue()) {
            outlinecolor = ColorHolder.toHex(IOred.getValue(), IOgreen.getValue(), IOblue.getValue());
        } else if (player.isSneaking() && sneak.getValue()) {
            outlinecolor = ColorHolder.toHex(SOred.getValue(), SOgreen.getValue(), SOblue.getValue());
        }
        return outlinecolor;
    }

    private void renderItemStack(ItemStack stack, int x, int y) {
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.clear(GL11.GL_ACCUM);

        RenderHelper.enableStandardItemLighting();
        mc.getRenderItem().zLevel = -150.0F;
        GlStateManager.disableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.disableCull();

        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        mc.getRenderItem().renderItemOverlays(mc.fontRenderer, stack, x, y);

        mc.getRenderItem().zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();

        GlStateManager.enableCull();
        GlStateManager.enableAlpha();

        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        GlStateManager.disableDepth();
        renderEnchantmentText(stack, x, y);
        GlStateManager.enableDepth();
        GlStateManager.scale(2F, 2F, 2F);
        GlStateManager.popMatrix();
    }

    private void renderEnchantmentText(ItemStack stack, int x, int y) {
        int enchantmentY = y - 8;
        int yCount = y;

        if (stack.getItem() == Items.GOLDEN_APPLE && stack.hasEffect()) {
            renderer.drawStringWithShadow("god", x * 2, enchantmentY, 0xFFc34d41);
            enchantmentY -= 8;
        }

        NBTTagList enchants = stack.getEnchantmentTagList();
        if (enchants.tagCount() > 2 && max.getValue()) {
            if (maxText.getValue()) {
                renderer.drawStringWithShadow("",(float)(x * 2), (float)enchantmentY, ColorHolder.toHex(Mred.getValue(), Mgreen.getValue(), Mblue.getValue()));
                enchantmentY -= 8;
            } else {
                renderer.drawStringWithShadow("max", (float) (x * 2), (float) enchantmentY, ColorHolder.toHex(Mred.getValue(), Mgreen.getValue(), Mblue.getValue()));
                enchantmentY -= 8;
            }
        } else {
            for (int index = 0; index < enchants.tagCount(); ++index) {
                short id = enchants.getCompoundTagAt(index).getShort("id");
                short level = enchants.getCompoundTagAt(index).getShort("lvl");
                Enchantment enc = Enchantment.getEnchantmentByID(id);
                if (enc != null) {
                    String encName = enc.isCurse()
                            ? TextFormatting.RED
                            + enc.getTranslatedName(level).substring(11).substring(0, 1).toLowerCase()
                            : enc.getTranslatedName(level).substring(0, 1).toLowerCase();
                    encName = encName + level;
                    renderer.drawStringWithShadow(encName, x * 2, enchantmentY, -1);
                    enchantmentY -= 8;
                }
            }
        }

        if (DamageUtil.hasDurability(stack)) {
            int percent = DamageUtil.getRoundedDamage(stack);
            String color;
            if (percent >= 60) {
                color = TextUtil.GREEN;
            } else if (percent >= 25) {
                color = TextUtil.YELLOW;
            } else {
                color = TextUtil.RED;
            }
            renderer.drawStringWithShadow(color + percent + "%", x * 2, enchantmentY, 0xFFFFFFFF);
        }
    }

    private float getBiggestArmorTag(EntityPlayer player) {
        float enchantmentY = 0;
        boolean arm = false;
        for (ItemStack stack : player.inventory.armorInventory) {
            float encY = 0;
            if (stack != null) {
                NBTTagList enchants = stack.getEnchantmentTagList();
                for (int index = 0; index < enchants.tagCount(); ++index) {
                    short id = enchants.getCompoundTagAt(index).getShort("id");
                    Enchantment enc = Enchantment.getEnchantmentByID(id);
                    if (enc != null) {
                        encY += 8;
                        arm = true;
                    }
                }
            }
            if (encY > enchantmentY) enchantmentY = encY;
        }
        ItemStack renderMainHand = player.getHeldItemMainhand().copy();
        if (renderMainHand.hasEffect()) {
            float encY = 0;
            NBTTagList enchants = renderMainHand.getEnchantmentTagList();
            for (int index = 0; index < enchants.tagCount(); ++index) {
                short id = enchants.getCompoundTagAt(index).getShort("id");
                Enchantment enc = Enchantment.getEnchantmentByID(id);
                if (enc != null) {
                    encY += 8;
                    arm = true;
                }
            }
            if (encY > enchantmentY) enchantmentY = encY;
        }
        ItemStack renderOffHand = player.getHeldItemOffhand().copy();
        if (renderOffHand.hasEffect()) {
            float encY = 0;
            NBTTagList enchants = renderOffHand.getEnchantmentTagList();
            for (int index = 0; index < enchants.tagCount(); ++index) {
                short id = enchants.getCompoundTagAt(index).getShort("id");
                Enchantment enc = Enchantment.getEnchantmentByID(id);
                if (enc != null) {
                    encY += 8;
                    arm = true;
                }
            }
            if (encY > enchantmentY) enchantmentY = encY;
        }
        return (arm ? 0 : 20) + enchantmentY;
    }

    private String getDisplayTag(EntityPlayer player) {
        String name = player.getDisplayName().getFormattedText();
        if (name.contains(mc.getSession().getUsername())) {
            name = "You";
        }

        if (!health.getValue()) {
            return name;
        }

        float health = EntityUtil.getHealth(player);
        String color;

        if (health > 18) {
            color = TextUtil.GREEN;
        } else if (health > 16) {
            color = TextUtil.DARK_GREEN;
        } else if (health > 12) {
            color = TextUtil.YELLOW;
        } else if (health > 8) {
            color = TextUtil.RED;
        } else if (health > 5) {
            color = TextUtil.DARK_RED;
        } else {
            color = TextUtil.DARK_RED;
        }

        String pingStr = "";
        if (ping.getValue()) {
            try {
                final int responseTime = Objects.requireNonNull(mc.getConnection()).getPlayerInfo(player.getUniqueID()).getResponseTime();
                pingStr += responseTime + "ms ";
            } catch (Exception ignored) {}
        }

        String idString = "";
        if (entityID.getValue()) {
            idString += "ID: " + player.getEntityId() + " ";
        }

        String gameModeStr = "";
        if (gamemode.getValue()) {
            if (player.isCreative()) {
                gameModeStr += "[C] ";
            } else if (player.isSpectator() || player.isInvisible()) {
                gameModeStr += "[I] ";
            } else {
                gameModeStr += "[S] ";
            }
        }

        if (Math.floor(health) == health) {
            name = name + color + " " + (health > 0 ? (int) Math.floor(health) : "dead");
        } else {
            name = name + color + " " + (health > 0 ? (int) health : "dead");
        }
        return " " + pingStr + idString + gameModeStr + name + " ";
    }

    private double interpolate(double previous, double current, float delta) {
        return (previous + (current - previous) * delta);
    }

    public void drawOutlineRect(float x, float y, float w, float h, int color) {
        float alpha = (float)(color >> 24 & 0xFF) / 255.0f;
        float red = (float)(color >> 16 & 0xFF) / 255.0f;
        float green = (float)(color >> 8 & 0xFF) / 255.0f;
        float blue = (float)(color & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.glLineWidth((float)this.Owidth.getValue().floatValue());
        GlStateManager.tryBlendFuncSeparate((int)770, (int)771, (int)1, (int)0);
        bufferbuilder.begin(2, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos((double)x, (double)h, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos((double)w, (double)h, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos((double)w, (double)y, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos((double)x, (double)y, 0.0).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public void drawRect(float x, float y, float w, float h, int color) {
        float alpha = (float)(color >> 24 & 0xFF) / 255.0f;
        float red = (float)(color >> 16 & 0xFF) / 255.0f;
        float green = (float)(color >> 8 & 0xFF) / 255.0f;
        float blue = (float)(color & 0xFF) / 255.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.glLineWidth((float)this.Owidth.getValue().floatValue());
        GlStateManager.tryBlendFuncSeparate((int)770, (int)771, (int)1, (int)0);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos((double)x, (double)h, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos((double)w, (double)h, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos((double)w, (double)y, 0.0).color(red, green, blue, alpha).endVertex();
        bufferbuilder.pos((double)x, (double)y, 0.0).color(red, green, blue, alpha).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    @Override
    public void onUpdate() {
        if (outline.getValue().equals(false)) {
            rect.setValue(true);
        } else if (rect.getValue().equals(false)) {
            outline.setValue(true);
        }
        if (ORainbow.getValue()) {
            OutlineRainbow();
        }
        if (NCRainbow.getValue()) {
            TextRainbow();
        }
        if (FCRainbow.getValue()) {
            FriendRainbow();
        }
        if (SCRainbow.getValue()) {
            SneakColorRainbow();
        }
        if (ICRainbow.getValue()) {
            InvisibleRainbow();
        }
        if (FORainbow.getValue()) {
            FriendOutlineRainbow();
        }
        if (IORainbow.getValue()) {
            InvisibleOutlineRainbow();
        }
        if (SORainbow.getValue()) {
            SneakOutlineRainbow();
        }

    }

    public void OutlineRainbow() {

        float[] tick_color = {
                (System.currentTimeMillis() % (360 * 32)) / (360f * 32)
        };

        int color_rgb_o = Color.HSBtoRGB(tick_color[0], 0.8f, 0.8f);

        Ored.setValue((color_rgb_o >> 16) & 0xFF);
        Ogreen.setValue((color_rgb_o >> 8) & 0xFF);
        Oblue.setValue(color_rgb_o & 0xFF);
    }

    public void TextRainbow() {
        float[] tick_color = {
                (System.currentTimeMillis() % (360 * 32)) / (360f * 32)
        };

        int color_rgb_o = Color.HSBtoRGB(tick_color[0], 0.8f, 0.8f);

        NCred.setValue((color_rgb_o >> 16) & 0xFF);
        NCgreen.setValue((color_rgb_o >> 8) & 0xFF);
        NCblue.setValue(color_rgb_o & 0xFF);
    }

    public void FriendRainbow() {
        float[] tick_color = {
                (System.currentTimeMillis() % (360 * 32)) / (360f * 32)
        };

        int color_rgb_o = Color.HSBtoRGB(tick_color[0], 0.8f, 0.8f);

        FCred.setValue((color_rgb_o >> 16) & 0xFF);
        FCgreen.setValue((color_rgb_o >> 8) & 0xFF);
        FCblue.setValue(color_rgb_o & 0xFF);
    }

    public void SneakColorRainbow() {
        float[] tick_color = {
                (System.currentTimeMillis() % (360 * 32)) / (360f * 32)
        };

        int color_rgb_o = Color.HSBtoRGB(tick_color[0], 0.8f, 0.8f);

        SCred.setValue((color_rgb_o >> 16) & 0xFF);
        SCgreen.setValue((color_rgb_o >> 8) & 0xFF);
        SCblue.setValue(color_rgb_o & 0xFF);
    }

    public void InvisibleRainbow() {
        float[] tick_color = {
                (System.currentTimeMillis() % (360 * 32)) / (360f * 32)
        };

        int color_rgb_o = Color.HSBtoRGB(tick_color[0], 0.8f, 0.8f);

        ICred.setValue((color_rgb_o >> 16) & 0xFF);
        ICgreen.setValue((color_rgb_o >> 8) & 0xFF);
        ICblue.setValue(color_rgb_o & 0xFF);
    }

    public void InvisibleOutlineRainbow() {
        float[] tick_color = {
                (System.currentTimeMillis() % (360 * 32)) / (360f * 32)
        };

        int color_rgb_o = Color.HSBtoRGB(tick_color[0], 0.8f, 0.8f);

        IOred.setValue((color_rgb_o >> 16) & 0xFF);
        IOgreen.setValue((color_rgb_o >> 8) & 0xFF);
        IOblue.setValue(color_rgb_o & 0xFF);
    }

    public void FriendOutlineRainbow() {
        float[] tick_color = {
                (System.currentTimeMillis() % (360 * 32)) / (360f * 32)
        };

        int color_rgb_o = Color.HSBtoRGB(tick_color[0], 0.8f, 0.8f);

        FOred.setValue((color_rgb_o >> 16) & 0xFF);
        FOgreen.setValue((color_rgb_o >> 8) & 0xFF);
        FOblue.setValue(color_rgb_o & 0xFF);
    }

    public void SneakOutlineRainbow() {
        float[] tick_color = {
                (System.currentTimeMillis() % (360 * 32)) / (360f * 32)
        };

        int color_rgb_o = Color.HSBtoRGB(tick_color[0], 0.8f, 0.8f);

        SOred.setValue((color_rgb_o >> 16) & 0xFF);
        SOgreen.setValue((color_rgb_o >> 8) & 0xFF);
        SOblue.setValue(color_rgb_o & 0xFF);
    }
}