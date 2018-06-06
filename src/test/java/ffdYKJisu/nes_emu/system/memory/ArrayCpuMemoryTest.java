package ffdYKJisu.nes_emu.system.memory;

import ffdYKJisu.nes_emu.system.cartridge.CartridgeFactory;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

/** Tests for {@link ArrayCpuMemory}. */
public class ArrayCpuMemoryTest {

  private static Logger logger = LoggerFactory.getLogger(ArrayCpuMemoryTest.class);

  ArrayCpuMemory memory;

  @Before
  public void initialize() {
    memory = new ArrayCpuMemory(new CartridgeFactory().fakeCartridge());
  }

  @Test
  public void testReadRAM() {
    assertEquals(0, memory.read((short) 0));
  }

  @Test
  public void testWriteRAM() {
    memory.write((short) 0, (byte) 1);
    assertEquals(1, memory.read((byte) 0));
    assertEquals(1, memory.read((short) 0));
  }

  @Test
  public void testWriteRAMHigh() {
    memory.write((short) 0x100, (byte) 10);
    assertEquals(10, memory.read((short) 0x100));
  }

  @Test
  public void testReadProgramROM() {
    System.out.println(String.format("test 0x%04X 0x%04X", 0x8000, (short) 0x8000));
    logger.info(String.format("test %d %d", 0x8000, (short) 0x8000));

    assertEquals(0, memory.read((short) 0x8000));
  }
}
