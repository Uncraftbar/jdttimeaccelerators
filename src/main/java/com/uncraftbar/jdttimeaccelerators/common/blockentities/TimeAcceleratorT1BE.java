package com.uncraftbar.jdttimeaccelerators.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.*;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.entities.TimeWandEntity;
import com.direwolf20.justdirethings.common.items.TimeWand;
import com.direwolf20.justdirethings.setup.Config;
import com.direwolf20.justdirethings.util.MiscTools;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.uncraftbar.jdttimeaccelerators.common.capabilities.TimeAcceleratorFluidTank;
import com.uncraftbar.jdttimeaccelerators.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class TimeAcceleratorT1BE extends BaseMachineBE implements PoweredMachineBE, RedstoneControlledBE, FluidMachineBE {
    public RedstoneControlData redstoneControlData = getDefaultRedstoneData();
    public final PoweredMachineContainerData poweredMachineData = new PoweredMachineContainerData(this);
    public final FluidContainerData fluidContainerData = new FluidContainerData(this);
    protected final MachineEnergyStorage energyStorage = new MachineEnergyStorage(getMaxEnergy());
    protected final TimeAcceleratorFluidTank fluidTank = new TimeAcceleratorFluidTank(getMaxMB());
    protected int speedLevel = 1;

    public TimeAcceleratorT1BE(BlockPos pPos, BlockState pBlockState) { this(Registration.TimeAcceleratorT1BE.get(), pPos, pBlockState); }
    protected TimeAcceleratorT1BE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) { super(pType, pPos, pBlockState); MACHINE_SLOTS = 0; }

    @Override public void tickServer() { clearProtectionCache(); if (!level.isClientSide) accelerateTargets(); }

    public void accelerateTargets() {
        if (level == null || level.isClientSide || !isActiveRedstone()) return;
        accelerateBlock(getBlockPos().relative(getBlockState().getValue(BlockStateProperties.FACING)));
    }

    protected boolean accelerateBlock(BlockPos targetPos) {
        if (!(level instanceof ServerLevel serverLevel) || targetPos.equals(getBlockPos())) return false;
        BlockState targetState = level.getBlockState(targetPos);
        BlockEntity targetBE = level.getBlockEntity(targetPos);
        if (!MiscTools.isValidTickAccelBlock(serverLevel, targetState, targetBE)) return false;
        int rate = getAccelerationRate();
        int feCost = getEnergyCost(rate);
        int fluidCost = getFluidCost(rate);
        if (!hasEnoughPower(feCost) || !hasEnoughFluid(fluidCost)) return false;
        extractEnergy(feCost, false);
        extractFluid(fluidCost);
        MiscTools.doExtraTicks(serverLevel, targetPos, rate);
        return true;
    }

    public int getAccelerationRate() { return getAccelerationRateForSpeedLevel(speedLevel); }
    public int getAccelerationRateForSpeedLevel(int speedLevel) { return Math.min((int) TimeWandEntity.calculateAccelRate(speedLevel), getMaxAllowedMultiplier()); }
    public int getMaxAllowedMultiplier() { return Math.max(1, highestPowerOfTwoAtMost(Config.TIME_WAND_MAX_MULTIPLIER.get() / 4)); }
    protected int highestPowerOfTwoAtMost(int value) { int result = 1; while (result * 2 <= value) result *= 2; return result; }
    public int getMaxSpeedLevel() { return Math.max(0, (int)(Math.log(getMaxAllowedMultiplier()) / Math.log(2))); }
    public int getSpeedLevel() { return speedLevel; }
    public void setSpeedLevel(int speedLevel) { this.speedLevel = Math.max(1, Math.min(speedLevel, getMaxSpeedLevel())); markDirtyClient(); }
    public int getEnergyCost(int rate) { return rate * TimeWand.getFEPerRate(); }
    public int getFluidCost(int rate) { return (int)(rate * TimeWand.getMBPerRate()); }
    public boolean hasEnoughFluid(int fluidCost) { return fluidCost <= 0 || getFluidTank().drain(fluidCost, IFluidHandler.FluidAction.SIMULATE).getAmount() == fluidCost; }
    public int extractFluid(int fluidCost) { return fluidCost <= 0 ? 0 : getFluidTank().drain(fluidCost, IFluidHandler.FluidAction.EXECUTE).getAmount(); }

    @Override public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("timeAcceleratorSpeedLevel", speedLevel);
        tag.put("energyStorage", energyStorage.serializeNBT(provider));
        tag.put("fluidTank", fluidTank.serializeNBT(provider));
    }
    @Override public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        if (tag.contains("timeAcceleratorSpeedLevel")) speedLevel = Math.max(1, tag.getInt("timeAcceleratorSpeedLevel"));
        if (tag.contains("energyStorage")) energyStorage.deserializeNBT(provider, tag.get("energyStorage"));
        if (tag.contains("fluidTank")) fluidTank.deserializeNBT(provider, tag.getCompound("fluidTank"));
        super.loadAdditional(tag, provider);
    }

    @Override public BlockEntity getBlockEntity() { return this; }
    @Override public ContainerData getContainerData() { return poweredMachineData; }
    @Override public MachineEnergyStorage getEnergyStorage() { return energyStorage; }
    @Override public int getStandardEnergyCost() { return getEnergyCost(getAccelerationRate()); }
    @Override public TimeAcceleratorFluidTank getFluidTank() { return fluidTank; }
    @Override public ContainerData getFluidContainerData() { return fluidContainerData; }
    @Override public int getMaxMB() { return 8000; }
    @Override public int getMaxEnergy() { return 100000; }
    @Override public RedstoneControlData getRedstoneControlData() { return redstoneControlData; }
    @Override public int getTickSpeed() { return 20; }
    @Override public void setTickSpeed(int newTickSpeed) { super.setTickSpeed(20); }
}
