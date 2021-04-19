package me.alpha432.oyvey.features.modules.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;

public class PopCounter extends Module {
    public static HashMap<String, Integer> TotemPopContainer = new HashMap();
    public static PopCounter INSTANCE = new PopCounter();

    public PopCounter() {
        super("PopCounter", "Counts other players totem pops.", Module.Category.MISC, true, false, false);
        setInstance();
    }

    public final Setting<String> clientname = register(new Setting<Object>("Name", "onpoint.ie"));

    public static PopCounter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PopCounter();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        TotemPopContainer.clear();
    }

    public void onDeath(EntityPlayer player) {
        if (TotemPopContainer.containsKey(player.getName())) {
            int l_Count = TotemPopContainer.get(player.getName());
            TotemPopContainer.remove(player.getName());
            if (l_Count == 1) {
                Command.sendSilentMessage(ChatFormatting.RED + player.getName() + " died after popping " + ChatFormatting.GRAY + l_Count + ChatFormatting.RED + ChatFormatting.RED + " totem, thanks to " + clientname.getValueAsString());
            } else {
                Command.sendSilentMessage(ChatFormatting.RED + player.getName() + " died after popping " + ChatFormatting.GRAY + l_Count + ChatFormatting.RED + ChatFormatting.RED + " totems, " + "thanks to " + clientname.getValueAsString());
            }
        }
    }

    public void onTotemPop(EntityPlayer player) {
        if (PopCounter.fullNullCheck()) {
            return;
        }
        if (PopCounter.mc.player.equals(player)) {
            return;
        }
        int l_Count = 1;
        if (TotemPopContainer.containsKey(player.getName())) {
            l_Count = TotemPopContainer.get(player.getName());
            TotemPopContainer.put(player.getName(), ++l_Count);
        } else {
            TotemPopContainer.put(player.getName(), l_Count);
        }
        if (l_Count == 1) {
            Command.sendSilentMessage(ChatFormatting.RED + player.getName() + " popped " + ChatFormatting.GRAY + l_Count + ChatFormatting.RED + " totem, " + ChatFormatting.RED + "thanks to " + clientname.getValueAsString());
        } else {
            Command.sendSilentMessage(ChatFormatting.RED + player.getName() + " popped " + ChatFormatting.GRAY + l_Count + ChatFormatting.RED + " totems, " + ChatFormatting.RED + "thanks to " + clientname.getValueAsString());
        }
    }
}