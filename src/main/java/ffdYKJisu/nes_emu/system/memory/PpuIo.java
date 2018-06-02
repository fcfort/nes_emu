package ffdYKJisu.nes_emu.system.memory;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

public class PpuIo implements Addressable {

  private static final int PPU_START = 0x2000;
  private static final int PPU_END = 0x3FFF;
  private static final int PPU_IO_LENGTH = 0x8;

  private final byte[] data;

  @Inject
  PpuIo() {
    data = new byte[PPU_IO_LENGTH];
  }

  @Override
  public byte read(short address) {
    assertIsValidAddress(address);
    return data[toArrayIndex(address)];
  }

  @Override
  public void write(short address, byte value) {
    assertIsValidAddress(address);
    data[toArrayIndex(address)] = value;
  }

  private static void assertIsValidAddress(short address) {
    Preconditions.checkArgument(address >= PPU_START && address <= PPU_END);
  }

  private static int toArrayIndex(short address) {
    return (Short.toUnsignedInt(address) - PPU_START) & (PPU_IO_LENGTH - 1);
  }
}
