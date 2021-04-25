package me.alpha432.oyvey.util;

import com.google.common.util.concurrent.AtomicDouble;
import me.alpha432.oyvey.OyVey;
import me.alpha432.oyvey.features.command.Command;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class BlockUtilll
        implements Util {
    public static final List<Block> blackList = Arrays.asList(Blocks.ENDER_CHEST, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE, Blocks.ANVIL, Blocks.BREWING_STAND, Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER, Blocks.TRAPDOOR, Blocks.ENCHANTING_TABLE);
    public static final List<Block> shulkerList = Arrays.asList(Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.SILVER_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX);
    public static List<Block> unSolidBlocks = Arrays.asList(Blocks.FLOWING_LAVA, Blocks.FLOWER_POT, Blocks.SNOW, Blocks.CARPET, Blocks.END_ROD, Blocks.SKULL, Blocks.FLOWER_POT, Blocks.TRIPWIRE, Blocks.TRIPWIRE_HOOK, Blocks.WOODEN_BUTTON, Blocks.LEVER, Blocks.STONE_BUTTON, Blocks.LADDER, Blocks.UNPOWERED_COMPARATOR, Blocks.POWERED_COMPARATOR, Blocks.UNPOWERED_REPEATER, Blocks.POWERED_REPEATER, Blocks.UNLIT_REDSTONE_TORCH, Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WIRE, Blocks.AIR, Blocks.PORTAL, Blocks.END_PORTAL, Blocks.WATER, Blocks.FLOWING_WATER, Blocks.LAVA, Blocks.FLOWING_LAVA, Blocks.SAPLING, Blocks.RED_FLOWER, Blocks.YELLOW_FLOWER, Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS, Blocks.REEDS, Blocks.PUMPKIN_STEM, Blocks.MELON_STEM, Blocks.WATERLILY, Blocks.NETHER_WART, Blocks.COCOA, Blocks.CHORUS_FLOWER, Blocks.CHORUS_PLANT, Blocks.TALLGRASS, Blocks.DEADBUSH, Blocks.VINE, Blocks.FIRE, Blocks.RAIL, Blocks.ACTIVATOR_RAIL, Blocks.DETECTOR_RAIL, Blocks.GOLDEN_RAIL, Blocks.TORCH);


    public static List<BlockPos> getBlockSphere(float breakRange, Class clazz) {
        NonNullList positions = NonNullList.create();
        positions.addAll(BlockUtilll.getSphere(EntityUtil.getPlayerPos(BlockUtilll.mc.player), breakRange, (int) breakRange, false, true, 0).stream().filter(pos -> clazz.isInstance(BlockUtilll.mc.world.getBlockState(pos).getBlock())).collect(Collectors.toList()));
        return positions;
    }

    public static List<EnumFacing> getPossibleSides(BlockPos pos) {
        ArrayList<EnumFacing> facings = new ArrayList<EnumFacing>();
        if (BlockUtilll.mc.world == null || pos == null) {
            return facings;
        }
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbour = pos.offset(side);
            IBlockState blockState = BlockUtilll.mc.world.getBlockState(neighbour);
            if (blockState == null || !blockState.getBlock().canCollideCheck(blockState, false) || blockState.getMaterial().isReplaceable())
                continue;
            facings.add(side);
        }
        return facings;
    }

    public static EnumFacing getFirstFacing(BlockPos pos) {
        Iterator<EnumFacing> iterator = BlockUtilll.getPossibleSides(pos).iterator();
        if (iterator.hasNext()) {
            EnumFacing facing = iterator.next();
            return facing;
        }
        return null;
    }

    public static EnumFacing getRayTraceFacing(BlockPos pos) {
        RayTraceResult result = BlockUtilll.mc.world.rayTraceBlocks(new Vec3d(BlockUtilll.mc.player.posX, BlockUtilll.mc.player.posY + (double) BlockUtilll.mc.player.getEyeHeight(), BlockUtilll.mc.player.posZ), new Vec3d((double) pos.getX() + 0.5, (double) pos.getX() - 0.5, (double) pos.getX() + 0.5));
        if (result == null || result.sideHit == null) {
            return EnumFacing.UP;
        }
        return result.sideHit;
    }

    public static int isPositionPlaceable(BlockPos pos, boolean rayTrace) {
        return BlockUtilll.isPositionPlaceable(pos, rayTrace, true);
    }

    public static int isPositionPlaceable(BlockPos pos, boolean rayTrace, boolean entityCheck) {
        Block block = BlockUtilll.mc.world.getBlockState(pos).getBlock();
        if (!(block instanceof BlockAir || block instanceof BlockLiquid || block instanceof BlockTallGrass || block instanceof BlockFire || block instanceof BlockDeadBush || block instanceof BlockSnow)) {
            return 0;
        }
        if (!BlockUtilll.rayTracePlaceCheck(pos, rayTrace, 0.0f)) {
            return -1;
        }
        if (entityCheck) {
            for (Entity entity : BlockUtilll.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
                if (entity instanceof EntityItem || entity instanceof EntityXPOrb) continue;
                return 1;
            }
        }
        for (EnumFacing side : BlockUtilll.getPossibleSides(pos)) {
            if (!BlockUtilll.canBeClicked(pos.offset(side))) continue;
            return 3;
        }
        return 2;
    }

    public static void rightClickBlock(BlockPos pos, Vec3d vec, EnumHand hand, EnumFacing direction, boolean packet) {
        if (packet) {
            float f = (float) (vec.x - (double) pos.getX());
            float f1 = (float) (vec.y - (double) pos.getY());
            float f2 = (float) (vec.z - (double) pos.getZ());
            BlockUtilll.mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f1, f2));
        } else {
            BlockUtilll.mc.playerController.processRightClickBlock(BlockUtilll.mc.player, BlockUtilll.mc.world, pos, direction, vec, hand);
        }
        BlockUtilll.mc.player.swingArm(EnumHand.MAIN_HAND);
        BlockUtilll.mc.rightClickDelayTimer = 4;
    }

    public static void rightClickBlockLegit(BlockPos pos, float range, boolean rotate, EnumHand hand, AtomicDouble Yaw2, AtomicDouble Pitch, AtomicBoolean rotating, boolean packet) {
        Vec3d eyesPos = RotationUtil.getEyesPos();
        Vec3d posVec = new Vec3d(pos).add(0.5, 0.5, 0.5);
        double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);
        for (EnumFacing side : EnumFacing.values()) {
            Vec3d hitVec = posVec.add(new Vec3d(side.getDirectionVec()).scale(0.5));
            double distanceSqHitVec = eyesPos.squareDistanceTo(hitVec);
            if (distanceSqHitVec > MathUtil.square(range) || distanceSqHitVec >= distanceSqPosVec || BlockUtilll.mc.world.rayTraceBlocks(eyesPos, hitVec, false, true, false) != null)
                continue;
            if (rotate) {
                float[] rotations = RotationUtil.getLegitRotations(hitVec);
                Yaw2.set(rotations[0]);
                Pitch.set(rotations[1]);
                rotating.set(true);
            }
            BlockUtilll.rightClickBlock(pos, hitVec, hand, side, packet);
            BlockUtilll.mc.player.swingArm(hand);
            BlockUtilll.mc.rightClickDelayTimer = 4;
            break;
        }
    }

    public static boolean placeBlock(BlockPos pos, EnumHand hand, boolean rotate, boolean packet, boolean isSneaking) {
        boolean sneaking = false;
        EnumFacing side = BlockUtilll.getFirstFacing(pos);
        if (side == null) {
            return isSneaking;
        }
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = BlockUtilll.mc.world.getBlockState(neighbour).getBlock();
        if (!BlockUtilll.mc.player.isSneaking() && (blackList.contains(neighbourBlock) || shulkerList.contains(neighbourBlock))) {
            BlockUtilll.mc.player.connection.sendPacket(new CPacketEntityAction(BlockUtilll.mc.player, CPacketEntityAction.Action.START_SNEAKING));
            BlockUtilll.mc.player.setSneaking(true);
            sneaking = true;
        }
        if (rotate) {
            RotationUtil.faceVector(hitVec, true);
        }
        BlockUtilll.rightClickBlock(neighbour, hitVec, hand, opposite, packet);
        BlockUtilll.mc.player.swingArm(EnumHand.MAIN_HAND);
        BlockUtilll.mc.rightClickDelayTimer = 4;
        return sneaking || isSneaking;
    }

    public static boolean placeBlockSmartRotate(BlockPos pos, EnumHand hand, boolean rotate, boolean packet, boolean isSneaking) {
        boolean sneaking = false;
        EnumFacing side = BlockUtilll.getFirstFacing(pos);
        if (side == null) {
            return isSneaking;
        }
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = BlockUtilll.mc.world.getBlockState(neighbour).getBlock();
        if (!BlockUtilll.mc.player.isSneaking() && (blackList.contains(neighbourBlock) || shulkerList.contains(neighbourBlock))) {
            BlockUtilll.mc.player.connection.sendPacket(new CPacketEntityAction(BlockUtilll.mc.player, CPacketEntityAction.Action.START_SNEAKING));
            sneaking = true;
        }
        if (rotate) {
            OyVey.rotationManager.lookAtVec3d(hitVec);
        }
        BlockUtilll.rightClickBlock(neighbour, hitVec, hand, opposite, packet);
        BlockUtilll.mc.player.swingArm(EnumHand.MAIN_HAND);
        BlockUtilll.mc.rightClickDelayTimer = 4;
        return sneaking || isSneaking;
    }

    public static void placeBlockStopSneaking(BlockPos pos, EnumHand hand, boolean rotate, boolean packet, boolean isSneaking) {
        boolean sneaking = BlockUtilll.placeBlockSmartRotate(pos, hand, rotate, packet, isSneaking);
        if (!isSneaking && sneaking) {
            BlockUtilll.mc.player.connection.sendPacket(new CPacketEntityAction(BlockUtilll.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
    }

    public static Vec3d[] getHelpingBlocks(Vec3d vec3d) {
        return new Vec3d[]{new Vec3d(vec3d.x, vec3d.y - 1.0, vec3d.z), new Vec3d(vec3d.x != 0.0 ? vec3d.x * 2.0 : vec3d.x, vec3d.y, vec3d.x != 0.0 ? vec3d.z : vec3d.z * 2.0), new Vec3d(vec3d.x == 0.0 ? vec3d.x + 1.0 : vec3d.x, vec3d.y, vec3d.x == 0.0 ? vec3d.z : vec3d.z + 1.0), new Vec3d(vec3d.x == 0.0 ? vec3d.x - 1.0 : vec3d.x, vec3d.y, vec3d.x == 0.0 ? vec3d.z : vec3d.z - 1.0), new Vec3d(vec3d.x, vec3d.y + 1.0, vec3d.z)};
    }

    public static List<BlockPos> possiblePlacePositions(float placeRange) {
        NonNullList positions = NonNullList.create();
        positions.addAll(BlockUtilll.getSphere(EntityUtil.getPlayerPos(BlockUtilll.mc.player), placeRange, (int) placeRange, false, true, 0).stream().filter(BlockUtilll::canPlaceCrystal).collect(Collectors.toList()));
        return positions;
    }

    public static List<BlockPos> getSphere(BlockPos pos, float r, int h, boolean hollow, boolean sphere, int plus_y) {
        ArrayList<BlockPos> circleblocks = new ArrayList<BlockPos>();
        int cx = pos.getX();
        int cy = pos.getY();
        int cz = pos.getZ();
        int x = cx - (int) r;
        while ((float) x <= (float) cx + r) {
            int z = cz - (int) r;
            while ((float) z <= (float) cz + r) {
                int y = sphere ? cy - (int) r : cy;
                while (true) {
                    float f = y;
                    float f2 = sphere ? (float) cy + r : (float) (cy + h);
                    if (!(f < f2)) break;
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

    public static List<BlockPos> getDisc(BlockPos pos, float r) {
        ArrayList<BlockPos> circleblocks = new ArrayList<BlockPos>();
        int cx = pos.getX();
        int cy = pos.getY();
        int cz = pos.getZ();
        int x = cx - (int) r;
        while ((float) x <= (float) cx + r) {
            int z = cz - (int) r;
            while ((float) z <= (float) cz + r) {
                double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z);
                if (dist < (double) (r * r)) {
                    BlockPos position = new BlockPos(x, cy, z);
                    circleblocks.add(position);
                }
                ++z;
            }
            ++x;
        }
        return circleblocks;
    }

    public static boolean canPlaceCrystal(BlockPos blockPos) {
        BlockPos boost = blockPos.add(0, 1, 0);
        BlockPos boost2 = blockPos.add(0, 2, 0);
        try {
            return (BlockUtilll.mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || BlockUtilll.mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && BlockUtilll.mc.world.getBlockState(boost).getBlock() == Blocks.AIR && BlockUtilll.mc.world.getBlockState(boost2).getBlock() == Blocks.AIR && BlockUtilll.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty() && BlockUtilll.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public static List<BlockPos> possiblePlacePositions(float placeRange, boolean specialEntityCheck, boolean oneDot15) {
        NonNullList positions = NonNullList.create();
        positions.addAll(BlockUtilll.getSphere(EntityUtil.getPlayerPos(BlockUtilll.mc.player), placeRange, (int) placeRange, false, true, 0).stream().filter(pos -> BlockUtilll.canPlaceCrystal(pos, specialEntityCheck, oneDot15)).collect(Collectors.toList()));
        return positions;
    }

    public static boolean canPlaceCrystal(BlockPos blockPos, boolean specialEntityCheck, boolean oneDot15) {
        BlockPos boost = blockPos.add(0, 1, 0);
        BlockPos boost2 = blockPos.add(0, 2, 0);
        try {
            if (BlockUtilll.mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK && BlockUtilll.mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN) {
                return false;
            }
            if (!oneDot15 && BlockUtilll.mc.world.getBlockState(boost2).getBlock() != Blocks.AIR || BlockUtilll.mc.world.getBlockState(boost).getBlock() != Blocks.AIR) {
                return false;
            }
            for (Entity entity : BlockUtilll.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost))) {
                if (entity.isDead || specialEntityCheck && entity instanceof EntityEnderCrystal) continue;
                return false;
            }
            if (!oneDot15) {
                for (Entity entity : BlockUtilll.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2))) {
                    if (entity.isDead || specialEntityCheck && entity instanceof EntityEnderCrystal) continue;
                    return false;
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    public static boolean canBeClicked(BlockPos pos) {
        return BlockUtilll.getBlock(pos).canCollideCheck(BlockUtilll.getState(pos), false);
    }

    private static Block getBlock(BlockPos pos) {
        return BlockUtilll.getState(pos).getBlock();
    }

    private static IBlockState getState(BlockPos pos) {
        return BlockUtilll.mc.world.getBlockState(pos);
    }

    public static boolean isBlockAboveEntitySolid(Entity entity) {
        if (entity != null) {
            BlockPos pos = new BlockPos(entity.posX, entity.posY + 2.0, entity.posZ);
            return BlockUtilll.isBlockSolid(pos);
        }
        return false;
    }

    public static void debugPos(String message, BlockPos pos) {
        Command.sendMessage(message + pos.getX() + "x, " + pos.getY() + "y, " + pos.getZ() + "z");
    }

    public static void placeCrystalOnBlock(BlockPos pos, EnumHand hand, boolean swing, boolean exactHand) {
        RayTraceResult result = BlockUtilll.mc.world.rayTraceBlocks(new Vec3d(BlockUtilll.mc.player.posX, BlockUtilll.mc.player.posY + (double) BlockUtilll.mc.player.getEyeHeight(), BlockUtilll.mc.player.posZ), new Vec3d((double) pos.getX() + 0.5, (double) pos.getY() - 0.5, (double) pos.getZ() + 0.5));
        EnumFacing facing = result == null || result.sideHit == null ? EnumFacing.UP : result.sideHit;
        BlockUtilll.mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, facing, hand, 0.0f, 0.0f, 0.0f));
        if (swing) {
            BlockUtilll.mc.player.connection.sendPacket(new CPacketAnimation(exactHand ? hand : EnumHand.MAIN_HAND));
        }
    }

    public static BlockPos[] toBlockPos(Vec3d[] vec3ds) {
        BlockPos[] list = new BlockPos[vec3ds.length];
        for (int i = 0; i < vec3ds.length; ++i) {
            list[i] = new BlockPos(vec3ds[i]);
        }
        return list;
    }

    public static Vec3d posToVec3d(BlockPos pos) {
        return new Vec3d(pos);
    }

    public static BlockPos vec3dToPos(Vec3d vec3d) {
        return new BlockPos(vec3d);
    }

    public static Boolean isPosInFov(BlockPos pos) {
        int dirnumber = RotationUtil.getDirection4D();
        if (dirnumber == 0 && (double) pos.getZ() - BlockUtilll.mc.player.getPositionVector().z < 0.0) {
            return false;
        }
        if (dirnumber == 1 && (double) pos.getX() - BlockUtilll.mc.player.getPositionVector().x > 0.0) {
            return false;
        }
        if (dirnumber == 2 && (double) pos.getZ() - BlockUtilll.mc.player.getPositionVector().z > 0.0) {
            return false;
        }
        return dirnumber != 3 || !((double) pos.getX() - BlockUtilll.mc.player.getPositionVector().x < 0.0);
    }

    public static boolean isBlockBelowEntitySolid(Entity entity) {
        if (entity != null) {
            BlockPos pos = new BlockPos(entity.posX, entity.posY - 1.0, entity.posZ);
            return BlockUtilll.isBlockSolid(pos);
        }
        return false;
    }

    public static boolean isBlockSolid(BlockPos pos) {
        return !BlockUtilll.isBlockUnSolid(pos);
    }

    public static boolean isBlockUnSolid(BlockPos pos) {
        return BlockUtilll.isBlockUnSolid(BlockUtilll.mc.world.getBlockState(pos).getBlock());
    }

    public static boolean isBlockUnSolid(Block block) {
        return unSolidBlocks.contains(block);
    }

    public static Vec3d[] convertVec3ds(Vec3d vec3d, Vec3d[] input) {
        Vec3d[] output = new Vec3d[input.length];
        for (int i = 0; i < input.length; ++i) {
            output[i] = vec3d.add(input[i]);
        }
        return output;
    }

    public static Vec3d[] convertVec3ds(EntityPlayer entity, Vec3d[] input) {
        return BlockUtilll.convertVec3ds(entity.getPositionVector(), input);
    }

    public static boolean canBreak(BlockPos pos) {
        IBlockState blockState = BlockUtilll.mc.world.getBlockState(pos);
        Block block = blockState.getBlock();
        return block.getBlockHardness(blockState, BlockUtilll.mc.world, pos) != -1.0f;
    }

    public static boolean isValidBlock(BlockPos pos) {
        Block block = BlockUtilll.mc.world.getBlockState(pos).getBlock();
        return !(block instanceof BlockLiquid) && block.getMaterial(null) != Material.AIR;
    }

    public static boolean isScaffoldPos(BlockPos pos) {
        return BlockUtilll.mc.world.isAirBlock(pos) || BlockUtilll.mc.world.getBlockState(pos).getBlock() == Blocks.SNOW_LAYER || BlockUtilll.mc.world.getBlockState(pos).getBlock() == Blocks.TALLGRASS || BlockUtilll.mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid;
    }

    public static boolean rayTracePlaceCheck(BlockPos pos, boolean shouldCheck, float height) {
        return !shouldCheck || BlockUtilll.mc.world.rayTraceBlocks(new Vec3d(BlockUtilll.mc.player.posX, BlockUtilll.mc.player.posY + (double) BlockUtilll.mc.player.getEyeHeight(), BlockUtilll.mc.player.posZ), new Vec3d(pos.getX(), (float) pos.getY() + height, pos.getZ()), false, true, false) == null;
    }

    public static boolean rayTracePlaceCheck(BlockPos pos, boolean shouldCheck) {
        return BlockUtilll.rayTracePlaceCheck(pos, shouldCheck, 1.0f);
    }

    public static boolean rayTracePlaceCheck(BlockPos pos) {
        return BlockUtilll.rayTracePlaceCheck(pos, true);
    }

    public static boolean isInHole() {
        BlockPos blockPos = new BlockPos(BlockUtilll.mc.player.posX, BlockUtilll.mc.player.posY, BlockUtilll.mc.player.posZ);
        IBlockState blockState = BlockUtilll.mc.world.getBlockState(blockPos);
        return BlockUtilll.isBlockValid(blockState, blockPos);
    }

    public static double getNearestBlockBelow() {
        for (double y = BlockUtilll.mc.player.posY; y > 0.0; y -= 0.001) {
            if (BlockUtilll.mc.world.getBlockState(new BlockPos(BlockUtilll.mc.player.posX, y, BlockUtilll.mc.player.posZ)).getBlock() instanceof BlockSlab || BlockUtilll.mc.world.getBlockState(new BlockPos(BlockUtilll.mc.player.posX, y, BlockUtilll.mc.player.posZ)).getBlock().getDefaultState().getCollisionBoundingBox(BlockUtilll.mc.world, new BlockPos(0, 0, 0)) == null)
                continue;
            return y;
        }
        return -1.0;
    }

    public static boolean isBlockValid(IBlockState blockState, BlockPos blockPos) {
        if (blockState.getBlock() != Blocks.AIR) {
            return false;
        }
        if (BlockUtilll.mc.player.getDistanceSq(blockPos) < 1.0) {
            return false;
        }
        if (BlockUtilll.mc.world.getBlockState(blockPos.up()).getBlock() != Blocks.AIR) {
            return false;
        }
        if (BlockUtilll.mc.world.getBlockState(blockPos.up(2)).getBlock() != Blocks.AIR) {
            return false;
        }
        return BlockUtilll.isBedrockHole(blockPos) || BlockUtilll.isObbyHole(blockPos) || BlockUtilll.isBothHole(blockPos) || BlockUtilll.isElseHole(blockPos);
    }

    public static boolean isObbyHole(BlockPos blockPos) {
        for (BlockPos pos : BlockUtilll.getTouchingBlocks(blockPos)) {
            IBlockState touchingState = BlockUtilll.mc.world.getBlockState(pos);
            if (touchingState.getBlock() != Blocks.AIR && touchingState.getBlock() == Blocks.OBSIDIAN) continue;
            return false;
        }
        return true;
    }

    public static boolean isBedrockHole(BlockPos blockPos) {
        for (BlockPos pos : BlockUtilll.getTouchingBlocks(blockPos)) {
            IBlockState touchingState = BlockUtilll.mc.world.getBlockState(pos);
            if (touchingState.getBlock() != Blocks.AIR && touchingState.getBlock() == Blocks.BEDROCK) continue;
            return false;
        }
        return true;
    }

    public static boolean isBothHole(BlockPos blockPos) {
        for (BlockPos pos : BlockUtilll.getTouchingBlocks(blockPos)) {
            IBlockState touchingState = BlockUtilll.mc.world.getBlockState(pos);
            if (touchingState.getBlock() != Blocks.AIR && (touchingState.getBlock() == Blocks.BEDROCK || touchingState.getBlock() == Blocks.OBSIDIAN))
                continue;
            return false;
        }
        return true;
    }

    public static boolean isElseHole(BlockPos blockPos) {
        for (BlockPos pos : BlockUtilll.getTouchingBlocks(blockPos)) {
            IBlockState touchingState = BlockUtilll.mc.world.getBlockState(pos);
            if (touchingState.getBlock() != Blocks.AIR && touchingState.isFullBlock()) continue;
            return false;
        }
        return true;
    }

    public static BlockPos[] getTouchingBlocks(BlockPos blockPos) {
        return new BlockPos[]{blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down()};
    }
}

