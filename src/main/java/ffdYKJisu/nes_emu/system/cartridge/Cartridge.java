package ffdYKJisu.nes_emu.system.cartridge;

import com.google.common.base.Preconditions;
import ffdYKJisu.nes_emu.system.memory.Addressable;

import java.util.List;

public class Cartridge implements Addressable {

  private static final int DEFAULT_PRG_BANK = 0;
  private static final int NROM_START = 0x8000;
  private static final int NROM_END = 0xFFFF;
  private static final int NROM_16BIT_MASK = 0xBFFF;

  private RomHeader header;
  private List<byte[]> programRomBanks;
  private List<byte[]> characterRomBanks;

  Cartridge(RomHeader header, List<byte[]> programRomBanks, List<byte[]> characterRomBanks) {
    this.header = header;
    this.programRomBanks = programRomBanks;
    this.characterRomBanks = characterRomBanks;
  }

  public RomHeader getHeader() {
    return header;
  }

  @Override
  public byte read(short address) {
    assertIsValidAddress(address);
    return programRomBanks.get(DEFAULT_PRG_BANK)[toIndex(address)];
  }

  @Override
  public void write(short address, byte value) {
    assertIsValidAddress(address);
    programRomBanks.get(DEFAULT_PRG_BANK)[toIndex(address)] = value;
  }

  private static void assertIsValidAddress(short address) {
    int intAddress = Short.toUnsignedInt(address);
    Preconditions.checkArgument(intAddress >= NROM_START && intAddress <= NROM_END);
  }

  private static int toIndex(short address) {
    return (Short.toUnsignedInt(address) & NROM_16BIT_MASK) - NROM_START;
  }
}
