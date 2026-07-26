package com.uncraftbar.jdttimeaccelerators.common.blockentities;

import com.direwolf20.justdirethings.common.blockentities.basebe.*;
import com.direwolf20.justdirethings.common.capabilities.MachineEnergyStorage;
import com.direwolf20.justdirethings.common.entities.TimeWandEntity;
import com.direwolf20.justdirethings.common.items.TimeWand;
import com.direwolf20.justdirethings.setup.Config;
import com.direwolf20.justdirethings.util.MiscTools;
import com.direwolf20.justdirethings.util.interfacehelpers.RedstoneControlData;
import com.uncraftbar.jdttimeaccelerators.common.acceleration.AccelerationCoordinator;
import com.uncraftbar.jdttimeaccelerators.common.capabilities.TimeAcceleratorFluidTank;
import com.uncraftbar.jdttimeaccelerators.config.JDTTAConfig;
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
    /** The upstream Time Wand pays once for its default 30-second (600-tick) effect. */
    private static final int TIME_WAND_DURATION_TICKS = 600;
    public RedstoneControlData redstoneControlData = getDefaultRedstoneData();
    public final PoweredMachineContainerData poweredMachineData = new PoweredMachineContainerData(this);
    public final FluidContainerData fluidContainerData = new FluidContainerData(this);
    protected final MachineEnergyStorage energyStorage = new MachineEnergyStorage(getMaxEnergy());
    protected final TimeAcceleratorFluidTank fluidTank = new TimeAcceleratorFluidTank(getMaxMB());
    protected int speedLevel = 1;
    // Start one unit short so a non-zero cost is charged immediately, while the
    // total over every 600 successful target ticks still exactly matches the wand.
    protected int fluidCostRemainder = TIME_WAND_DURATION_TICKS - 1;

    public TimeAcceleratorT1BE(BlockPos pPos, BlockState pBlockState) { this(Registration.TimeAcceleratorT1BE.get(), pPos, pBlockState); }
    protected TimeAcceleratorT1BE(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) { super(pType, pPos, pBlockState); MACHINE_SLOTS = 0; }

    @Override public void tickServer() {
        // BaseMachineBE performs the redstone input evaluation. Skipping it left
        // receivingRedstone stuck at its saved/default value forever.
        super.tickServer();
        if (level != null && !level.isClientSide) accelerateTargets();
    }

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
        int fluidCost = getFluidCostForNextAcceleration(rate);
        if (!hasEnoughPower(feCost) || !hasEnoughFluid(fluidCost)) return false;
        if (!AccelerationCoordinator.tryClaim(serverLevel, targetPos)) return false;

        int extractedEnergy = extractEnergy(feCost, false);
        if (extractedEnergy != feCost) {
            if (extractedEnergy > 0) getEnergyStorage().receiveEnergy(extractedEnergy, false);
            AccelerationCoordinator.release(serverLevel, targetPos);
            return false;
        }

        int extractedFluid = extractFluid(fluidCost);
        if (extractedFluid != fluidCost) {
            if (extractedFluid > 0) {
                getFluidTank().fill(
                        new net.neoforged.neoforge.fluids.FluidStack(
                                com.direwolf20.justdirethings.setup.Registration.TIME_FLUID_SOURCE.get(),
                                extractedFluid),
                        IFluidHandler.FluidAction.EXECUTE);
            }
            getEnergyStorage().receiveEnergy(extractedEnergy, false);
            AccelerationCoordinator.release(serverLevel, targetPos);
            return false;
        }

        advanceFluidCostRemainder(rate);
        MiscTools.doExtraTicks(serverLevel, targetPos, rate);
        return true;
    }

    public int getAccelerationRate() { return getAccelerationRateForSpeedLevel(speedLevel); }
    public int getAccelerationRateForSpeedLevel(int speedLevel) { return Math.min((int) TimeWandEntity.calculateAccelRate(speedLevel), getMaxAllowedMultiplier()); }
    public int getMaxAllowedMultiplier() {
        int simpleMachineMaximum = JDTTAConfig.highestPowerOfTwoAtMost(
                Math.max(1, Config.TIME_WAND_MAX_MULTIPLIER.get() / 4));
        return Math.min(simpleMachineMaximum, JDTTAConfig.maxAccelerationMultiplier());
    }
    public int getMaxSpeedLevel() { return Math.max(0, (int)(Math.log(getMaxAllowedMultiplier()) / Math.log(2))); }
    public int getSpeedLevel() { return speedLevel; }
    public void setSpeedLevel(int speedLevel) { this.speedLevel = Math.max(1, Math.min(speedLevel, getMaxSpeedLevel())); markDirtyClient(); }
    public int getEnergyCost(int rate) { return rate * TimeWand.getFEPerRate(); }
    /**
     * Fluid the upstream wand consumes while stepping one target up to this rate.
     * Reaching 256x requires wand activations at 2x, 4x, ... 256x, so the cost is
     * cumulative rather than only the final 256x activation (255 mB at defaults).
     */
    public int getFluidCost(int rate) {
        int totalCost = 0;
        for (int wandRate = 2; wandRate <= rate; wandRate *= 2) {
            totalCost += (int) (wandRate * TimeWand.getMBPerRate());
            if (wandRate > Integer.MAX_VALUE / 2) break;
        }
        return totalCost;
    }
    /** Amortize that one-time wand cost across this machine's per-tick operation. */
    public int getFluidCostForNextAcceleration(int rate) {
        return (fluidCostRemainder + getFluidCost(rate)) / TIME_WAND_DURATION_TICKS;
    }
    protected void advanceFluidCostRemainder(int rate) {
        fluidCostRemainder = (fluidCostRemainder + getFluidCost(rate)) % TIME_WAND_DURATION_TICKS;
    }
    public boolean hasEnoughFluid(int fluidCost) { return fluidCost <= 0 || getFluidTank().drain(fluidCost, IFluidHandler.FluidAction.SIMULATE).getAmount() == fluidCost; }
    public int extractFluid(int fluidCost) { return fluidCost <= 0 ? 0 : getFluidTank().drain(fluidCost, IFluidHandler.FluidAction.EXECUTE).getAmount(); }

    @Override public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("timeAcceleratorSpeedLevel", speedLevel);
        tag.putInt("timeAcceleratorFluidCostRemainder", fluidCostRemainder);
        tag.put("energyStorage", energyStorage.serializeNBT(provider));
        tag.put("fluidTank", fluidTank.serializeNBT(provider));
    }
    @Override public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        if (tag.contains("timeAcceleratorSpeedLevel")) speedLevel = Math.max(1, tag.getInt("timeAcceleratorSpeedLevel"));
        if (tag.contains("timeAcceleratorFluidCostRemainder")) fluidCostRemainder = Math.max(0, Math.min(tag.getInt("timeAcceleratorFluidCostRemainder"), TIME_WAND_DURATION_TICKS - 1));
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
