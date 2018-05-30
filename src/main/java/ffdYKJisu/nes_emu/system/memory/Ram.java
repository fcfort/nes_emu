package ffdYKJisu.nes_emu.system.memory;

import com.google.common.base.Preconditions;

public class Ram implements CpuMemory {

    private static final int RAM_LENGTH = 0x800;

    private final byte[] data;

    public Ram() {
        data = new byte[RAM_LENGTH];
    }

    @Override
    public byte read(short address) {
        assertIsValidAddress(address);
        return data[toCanonicalAddress(address)];
    }

    @Override
    public void write(short address, byte value) {
        assertIsValidAddress(address);
        data[toCanonicalAddress(address)] = value;
    }

    private static boolean assertIsValidAddress(short address) {
        Preconditions.checkArgument(address >= 0 && address <= 0x1FFF);
    }

    private static int toCanonicalAddress(short address) {
        return Short.toUnsignedInt(address) & RAM_LENGTH;
    }
}
