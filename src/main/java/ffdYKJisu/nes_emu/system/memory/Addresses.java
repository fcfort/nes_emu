package ffdYKJisu.nes_emu.system.memory;

public final class Addresses {

  public short fromZeroPage(byte zeroPageAddress) {
    return (short) zeroPageAddress;
  }

  public short fromHighLowBytes(byte highAddress, byte lowAddress) {
    return (short) (highAddress << 8 + lowAddress);
  }
}
