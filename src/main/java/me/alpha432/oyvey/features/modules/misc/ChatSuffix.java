package me.alpha432.oyvey.features.modules.misc;

import me.alpha432.oyvey.features.modules.Module;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatSuffix extends Module {
    public ChatSuffix() {
        super("ChatSuffix", "Appends your message", Module.Category.MISC, false, false, false);
    }

    @SubscribeEvent
    public void onChat(ClientChatEvent event) {
        String OnPointSuffix = "\u23d0 \u1d0f\u0274\u1d18\u1d0f\u026a\u0274\u1d1b\u002e\u026a\u1d07"; // onpoint.ie suffix
        if (event.getMessage().startsWith("/") || event.getMessage().startsWith(".")
                || event.getMessage().startsWith(",") || event.getMessage().startsWith("-")
                || event.getMessage().startsWith("$") || event.getMessage().startsWith("*")) return;
        event.setMessage(event.getMessage() + OnPointSuffix); // Adds the suffix to the end of the message
    }
}