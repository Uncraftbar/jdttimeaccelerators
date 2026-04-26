package com.uncraftbar.jdttimeaccelerators.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.*;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.entities.TimeWandEntity;
import com.direwolf20.justdirethings.common.items.TimeWand;
import com.direwolf20.justdirethings.setup.Config;
import com.direwolf20.justdirethings.util.MiscTools;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import com.direwolf20.justdirethings.common.capabilities.JustDireFluidTank;
import com.direwolf20.justdirethings.setup.JDTRegistration;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class TimeAcceleratorT1BE extends BaseMachineBE implements PoweredMachineBE, RedstoneControlledBE, FluidMachineBE {
    public RedstoneControlData redstoneControlData = getDefaultRedstoneData();
    public final PoweredMachineContainerData poweredMachineData = new PoweredMachineContainerData(this);
    public final FluidContainerData fluidContainerData = new FluidContainerData(this);
    protected final MachineEnergyStorage energyStorage = new MachineEnergyStorage(getMaxEnergy());
    protected final JustDireFluidTank fluidTank = new JustDireFluidTank(getMaxMB(), fluidStack -> fluidStack.is(JDTRegistration.TIME_FLUID_TYPE.get()));
    protected int speedLevel = 1;

    public TimeAcceleratorT1BE(BlockPos pPos, BlockState pBlockState) { this(com.uncraftbar.jdttimeaccelerators.setup.Registration.TimeAcceleratorT1BE.get(), pPos, pBlockState); }
    protected TimeAcceleratorT1BE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) { super(pType, pPos, pBlockState); MACHINE_SLOTS = 0; }

    @Override public void tickServer() { clearProtectionCache(); if (!level.isClientSide()) accelerateTargets(); }

    public void accelerateTargets() {
        if (level == null || level.isClientSide() || !isActiveRedstone()) return;
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
    public boolean hasEnoughFluid(int fluidCost) {
        if (fluidCost <= 0) return true;
        try (Transaction tx = Transaction.openRoot()) {
            int extracted = getFluidTank().extract(0, FluidResource.of(JDTRegistration.TIME_FLUID_SOURCE.get()), fluidCost, tx);
            return extracted == fluidCost;
        }
    }
    public int extractFluid(int fluidCost) {
        if (fluidCost <= 0) return 0;
        try (Transaction tx = Transaction.openRoot()) {
            int extracted = getFluidTank().extract(0, FluidResource.of(JDTRegistration.TIME_FLUID_SOURCE.get()), fluidCost, tx);
            tx.commit();
            return extracted;
        }
    }

    @Override public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("timeAcceleratorSpeedLevel", speedLevel);
        output.putChild("energyStorage", energyStorage);
        output.putChild("fluidTank", fluidTank);
    }
    @Override public void loadAdditional(ValueInput input) {
        speedLevel = Math.max(1, input.getIntOr("timeAcceleratorSpeedLevel", speedLevel));
        input.readChild("energyStorage", energyStorage);
        input.readChild("fluidTank", fluidTank);
        super.loadAdditional(input);
    }

    @Override public BlockEntity getBlockEntity() { return this; }
    @Override public ContainerData getContainerData() { return poweredMachineData; }
    @Override public MachineEnergyStorage getEnergyStorage() { return energyStorage; }
    @Override public int getStandardEnergyCost() { return getEnergyCost(getAccelerationRate()); }
    @Override public JustDireFluidTank getFluidTank() { return fluidTank; }
    @Override public ContainerData getFluidContainerData() { return fluidContainerData; }
    @Override public int getMaxMB() { return 8000; }
    @Override public int getMaxEnergy() { return 100000; }
    @Override public RedstoneControlData getRedstoneControlData() { return redstoneControlData; }
    @Override public int getTickSpeed() { return 20; }
    @Override public void setTickSpeed(int newTickSpeed) { super.setTickSpeed(20); }
}
