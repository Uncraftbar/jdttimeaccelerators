package com.uncraftbar.jdttimeaccelerators.common.capabilities;

import com.direwolf20.justdirethings.setup.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class TimeAcceleratorFluidTank extends FluidTank implements INBTSerializable<CompoundTag> {
    public TimeAcceleratorFluidTank(int capacity) {
        super(capacity, fluidStack -> fluidStack.is(Registration.TIME_FLUID_TYPE.get()));
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return writeToNBT(provider, new CompoundTag());
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        fluid = readFromNBT(provider, nbt).getFluid();
    }
}
