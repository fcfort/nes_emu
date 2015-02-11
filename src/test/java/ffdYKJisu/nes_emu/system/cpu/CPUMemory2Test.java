package ffdYKJisu.nes_emu.system.cpu;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ffdYKJisu.nes_emu.system.memory.CPUMemory2;

public class CPUMemory2Test {

	private static Logger logger = LoggerFactory.getLogger(CPUMemory2Test.class);

	CPUMemory2 _mem;
	
	@Before
	public void initialize() {
		_mem = new CPUMemory2(null);
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
