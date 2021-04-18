package me.alpha432.oyvey.features.modules.movement;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class ReverseStep extends Module {
    private static ReverseStep INSTANCE = new ReverseStep();

    public ReverseStep() {
        super("ReverseStep", "ReverseStep.", Module.Category.MOVEMENT, true, false, false);
        setInstance();
    }

    public static ReverseStep getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ReverseStep();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if (mc.player == null || mc.world == null || mc.player.isInWater() || mc.player.isInLava()) {
            return;
        }
        if (mc.player.onGround) {
            --mc.player.motionY;
        }
    }
}