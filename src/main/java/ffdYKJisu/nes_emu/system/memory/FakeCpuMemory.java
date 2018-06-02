package ffdYKJisu.nes_emu.system.memory;

import java.util.HashMap;
import java.util.Map;

public final class FakeCpuMemory implements Addressable {

  private final Map<Short, Byte> memoryMap;

  public FakeCpuMemory() {
    memoryMap = new HashMap<>();
  }

  @Override
  public byte read(short address) {
    return memoryMap.get(address);
  }

  @Override
  public void write(short address, byte value) {
    memoryMap.put(address, value);
  }
}
