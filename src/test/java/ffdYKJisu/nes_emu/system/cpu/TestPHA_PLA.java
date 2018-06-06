package ffdYKJisu.nes_emu.system.cpu;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.cartridge.Cartridge;
import ffdYKJisu.nes_emu.system.cartridge.CartridgeFactory;
import ffdYKJisu.nes_emu.system.memory.Addressable;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestPHA_PLA {

  CPU cpu;
  Addressable mem;

  @Before
  public void initialize() throws UnableToLoadRomException {
    Cartridge c =
        new CartridgeFactory()
            .fromInputStream(ClassLoader.getSystemResourceAsStream("Pac-Man (U) [!].nes"));
    cpu = new CPU(c);
    mem = cpu.getMemory();
    cpu.reset();
  }

  @Test
  public void testPushPopOneValue() {
    cpu.LDA((byte) 124);
    assertEquals((byte) 124, cpu.getA());
    cpu.PHA();
    cpu.LDA((byte) 100);
    assertEquals((byte) 100, cpu.getA());
    cpu.PLA();
    assertEquals((byte) 124, cpu.getA());
  }

  @Test
  public void testPushPopTwoValues() {
    cpu.LDA((byte) 124);
    cpu.PHA();
    cpu.LDA((byte) 100);
    cpu.PHA();
    assertEquals((byte) 100, cpu.getA());
    cpu.PLA();
    assertEquals((byte) 100, cpu.getA());
    cpu.PLA();
    assertEquals((byte) 124, cpu.getA());
  }
}
