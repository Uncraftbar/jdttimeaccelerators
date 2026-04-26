package com.uncraftbar.jdttimeaccelerators.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.AreaAffectingBE;
import com.direwolf20.justdirethings.setup.Config;
import com.direwolf20.justdirethings.util.interfacehelpers.AreaAffectingData;
import com.uncraftbar.jdttimeaccelerators.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TimeAcceleratorT2BE extends TimeAcceleratorT1BE implements AreaAffectingBE {
    public AreaAffectingData areaAffectingData = new AreaAffectingData(getBlockState().getValue(BlockStateProperties.FACING));

    public TimeAcceleratorT2BE(BlockPos pPos, BlockState pBlockState) { super(Registration.TimeAcceleratorT2BE.get(), pPos, pBlockState); }

    @Override public void accelerateTargets() {
        if (level == null || level.isClientSide || !isActiveRedstone()) return;
        for (BlockPos targetPos : findTargets()) accelerateBlock(targetPos);
    }

    public List<BlockPos> findTargets() {
        AABB area = getAABB(getBlockPos());
        return BlockPos.betweenClosedStream((int) area.minX, (int) area.minY, (int) area.minZ, (int) area.maxX - 1, (int) area.maxY - 1, (int) area.maxZ - 1)
                .filter(blockPos -> !blockPos.equals(getBlockPos()))
                .map(BlockPos::immutable)
                .sorted(Comparator.comparingDouble(x -> x.distSqr(getBlockPos())))
                .collect(Collectors.toList());
    }

    @Override public int getMaxAllowedMultiplier() { return Math.max(1, Config.TIME_WAND_MAX_MULTIPLIER.get()); }
    @Override public int getMaxMB() { return 16000; }
    @Override public int getMaxEnergy() { return 1000000; }
    @Override public AreaAffectingData getAreaAffectingData() { return areaAffectingData; }

    @Override public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        saveAreaSettings(tag);
    }

    @Override public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        loadAreaSettings(tag);
        areaAffectingData.area = null;
    }
}
