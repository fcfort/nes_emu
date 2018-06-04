package ffdYKJisu.nes_emu.system.cartridge;

import ffdYKJisu.nes_emu.system.memory.Addressable;

import java.util.List;

public class Cartridge implements Addressable {

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
    return 0;
  }

  @Override
  public void write(short address, byte value) {}
}
