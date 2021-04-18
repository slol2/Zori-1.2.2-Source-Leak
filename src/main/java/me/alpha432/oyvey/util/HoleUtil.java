package me.alpha432.oyvey.util;

import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.List;

public class HoleUtil {

    public static final List<BlockPos> holeBlocks = Arrays.asList(new BlockPos(0, -1, 0), new BlockPos(0, 0, -1), new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0), new BlockPos(0, 0, 1));

    private static Minecraft mc = Minecraft.getMinecraft();

    public static final Vec3d[] cityOffsets = {
            new Vec3d(1, 0, 0),
            new Vec3d(0, 0, 1),
            new Vec3d(-1, 0, 0),
            new Vec3d(0, 0, -1),
    };


    public static boolean isInHole() {
        Vec3d playerPos = CombatUtil.interpolateEntity(mc.player);
        BlockPos blockpos = new BlockPos(playerPos.x, playerPos.y, playerPos.z);
        int size = 0;
        for(BlockPos bPos : holeBlocks) {
            if(CombatUtil.isHard(mc.world.getBlockState(blockpos.add(bPos)).getBlock())) {
                size++;
            }
        }
        return (size == 5);
    }

}
