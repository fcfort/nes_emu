package ffdYKJisu.nes_emu.system.memory;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ffdYKJisu.nes_emu.system.memory.CPUMemory;

public class CPUMemoryTest {

	private static Logger logger = LoggerFactory.getLogger(CPUMemoryTest.class);

	CPUMemory _mem;
	
	@Before
	public void initialize() {
		_mem = new CPUMemory();
	}
	
	@Test
	public void testReadRAM() {
		assertEquals(0, _mem.read((short)0));
	}
	
	@Test
	public void testWriteRAM() {
		_mem.write((short)0,(byte)1);
		assertEquals(1, _mem.read((byte)0));
		assertEquals(1, _mem.read((short)0));
	}
	
	@Test
	public void testWriteRAMHigh() {
		_mem.write((short)0x100,(byte)10);
		assertEquals(10, _mem.read((short)0x100));
	}
	
	@Test
	public void testReadProgramROM() {
		System.out.println(String.format("test 0x%04X 0x%04X", 0x8000, (short)0x8000));
		logger.info(String.format("test %d %d", 0x8000, (short)0x8000));
		
		assertEquals(0, _mem.read((short)0x8000));
	}		
}
