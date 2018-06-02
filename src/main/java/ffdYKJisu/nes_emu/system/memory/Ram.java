package ffdYKJisu.nes_emu.system.memory;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

public class Ram implements Addressable {

  private static final int RAM_START = 0;
  private static final int RAM_END = 0x1FFF;
  private static final int RAM_LENGTH = 0x800;

  private final byte[] data;

  @Inject
  Ram() {
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

  private static void assertIsValidAddress(short address) {
    Preconditions.checkArgument(address >= RAM_START && address <= RAM_END);
  }

  private static int toCanonicalAddress(short address) {
    return Short.toUnsignedInt(address) & (RAM_LENGTH - 1);
  }
}
