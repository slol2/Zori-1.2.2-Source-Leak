package me.alpha432.oyvey.features.modules.misc;

import com.mojang.text2speech.Narrator;
import me.alpha432.oyvey.event.events.DeathEvent;
import me.alpha432.oyvey.event.events.TotemPopEvent;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.concurrent.ConcurrentHashMap;

public class NarratorTweaks extends Module {
    public Setting<Boolean> pop = register(new Setting("OwnTotemPop", true));
    public Setting<Boolean> death = register(new Setting("Death", true));
    public Setting<Boolean> totemPop = register(new Setting("EnemyTotemPop", true));
    public Setting<Boolean> killsay = register(new Setting("Killsay", true));
    public Setting<String> totemPopMessage = register(new Setting("PopMessage", "<player> bro stop popping ", v -> pop.getValue()));
    public Setting<String> deathMessages = register(new Setting("DeathMessage", "<player> ayt bro its calm just come back innit", v -> death.getValue()));
    public Setting<String> popEnemyMessage = register(new Setting("PopEnemyMessage", "<player> YOU'RE POPPING KID", v -> totemPop.getValue()));
    public Setting<String> killsayMsg = register(new Setting("KillsayMessage", "1 sit no name dog!", v -> killsay.getValue()));
    private final Narrator narrator = Narrator.getNarrator();
    private ConcurrentHashMap<String, Integer> targetedPlayers = null;

    public NarratorTweaks() {
        super("NarratorTweaks", "Sends messages through narrator", Module.Category.MISC, true, false, false);
    }

    @SubscribeEvent
    public void onTotemPop(TotemPopEvent event) {
        if(event.getEntity() == mc.player) {
            narrator.say(totemPopMessage.getValue().replaceAll("<player>", mc.player.getName()));
        }
    }

    public void onTotemPop(EntityPlayer player) {
        narrator.say(popEnemyMessage.getValue().replaceAll("<player>", String.valueOf(PopCounter.TotemPopContainer.get(player.getName()))));
    }

    @SubscribeEvent
    public void onDeath(DeathEvent event) {
        if (event.player == mc.player) {
            narrator.say(deathMessages.getValue().replaceAll("<player>", mc.player.getName()));
        }
    }

    @Override
    public void onUpdate() {
        if (nullCheck()) {
            return;
        }
        if (targetedPlayers == null) {
            targetedPlayers = new ConcurrentHashMap();
        }
        for (Entity entity : mc.world.getLoadedEntityList()) {
            String name2;
            EntityPlayer player;
            if (!(entity instanceof EntityPlayer) || (player = (EntityPlayer) entity).getHealth() > 0.0f || !shouldAnnounce(name2 = player.getName()))
                continue;
            doAnnounce(name2);
            break;
        }
        targetedPlayers.forEach((name, timeout) -> {
            if (timeout <= 0) {
                targetedPlayers.remove(name);
            } else {
                targetedPlayers.put(name, timeout - 1);
            }
        });
    }

    private boolean shouldAnnounce(String name) {
        return targetedPlayers.containsKey(name);
    }

    private void doAnnounce(String name) {
        targetedPlayers.remove(name);
        narrator.say(killsayMsg.getValue());
        int u = 0;
        for (int i = 0; i < 10; ++i) {
            ++u;
        }
    }

    @SubscribeEvent
    public void onLeavingDeathEvent(LivingDeathEvent event) {
        EntityLivingBase entity;
        if (AutoGG.mc.player == null) {
            return;
        }
        if (targetedPlayers == null) {
            targetedPlayers = new ConcurrentHashMap();
        }
        if ((entity = event.getEntityLiving()) == null) {
            return;
        }
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;
        if (player.getHealth() > 0.0f) {
            return;
        }
        String name = player.getName();
        if (shouldAnnounce(name)) {
            doAnnounce(name);
        }
    }
}
