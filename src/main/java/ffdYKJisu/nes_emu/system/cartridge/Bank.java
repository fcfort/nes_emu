package ffdYKJisu.nes_emu.system.cartridge;

/**
 * Defines the various kinds of banks possible in a cartridge
 * <p>
 * <code>ProgramRom</code> - 16KB Program code
 * <p>
 * <code>CharacterRom</code> - 8KB Character data
 */
public enum Bank {
  ProgramRom(0x4000), // 16KB
  CharacterRom(0x2000); // 8KB

  public final int length;

  Bank(double size) {
    this.length = (int) size;
  }
}
