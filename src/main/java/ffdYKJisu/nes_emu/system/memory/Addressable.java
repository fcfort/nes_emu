package ffdYKJisu.nes_emu.system.memory;

public interface Addressable {

  byte read(short address);

  void write(short address, byte value);
}
