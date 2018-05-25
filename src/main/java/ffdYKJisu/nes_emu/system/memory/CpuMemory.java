package ffdYKJisu.nes_emu.system.memory;

public interface CpuMemory {

  byte read(short address);

  void write(short address, byte value);
}
