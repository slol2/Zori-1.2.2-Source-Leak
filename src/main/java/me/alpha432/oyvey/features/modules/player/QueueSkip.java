package me.alpha432.oyvey.features.modules.player;

import me.alpha432.oyvey.features.command.Command;
import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;


public class QueueSkip extends Module {
    private Setting<Queue> QueueRank;
    private Setting<ServerMode> Server;
    private Setting<SkipMode> Mode;
    private Setting<Integer> Factor;
    private Setting<Integer> Retrys;

    public QueueSkip() {
        super("QueueSkip", "Skips Queue", Category.PLAYER, true, false, false);
        this.QueueRank = (Setting<Queue>) this.register(new Setting("QueueRank", Queue.NORM));
        this.Server = (Setting<ServerMode>) this.register(new Setting("Server", ServerMode.NORMAL));
        this.Mode = (Setting<SkipMode>) this.register(new Setting("Mode", SkipMode.NEW));
        this.Factor = (Setting<Integer>) this.register(new Setting("Factor", 0, 0, 60));
        this.Retrys = (Setting<Integer>) this.register(new Setting("Retries", 0, 0, 100));
    }

    @Override
    public void onEnable() {
        Command.sendMessage("Skipping the queue...");

        try {
            Thread.sleep(100000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        Command.sendMessage("Skipped the queue!");

        this.disable();

    }

    public void onDisable() {
    }


    private enum Queue {
        PRIO,
        NORM;
    }
    private enum SkipMode {
        NEW,
        OLD,
        FAST,
        BYPASS,
        UNDETECTIBLE;
    }
    private enum ServerMode {
        NORMAL,
        OLDFAG;
    }
}