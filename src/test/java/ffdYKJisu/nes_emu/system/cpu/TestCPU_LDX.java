package ffdYKJisu.nes_emu.system.cpu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.memory.CPUMemory;

public class TestCPU_LDX {

	NES _n;
	CPU _c;
	CPUMemory _mem; 
	
	@Before
	public void initialize() throws UnableToLoadRomException {
		Cartridge c = new Cartridge(ClassLoader.getSystemResourceAsStream("Pac-Man (U) [!].nes"));
		_mem = new CPUMemory();
		_c = new CPU(_mem);
		_mem.writeCartToMemory(c);
		_c.reset();
	}
	
	@Test
	public void testSetZero() {
		_c.LDX((byte) 0);
		assertEquals((byte) 0, _c.getX());
		assertTrue(_c.getZeroFlag());	
	}

	@Test
	public void testResetZero() {
		_c.LDX((byte) 1);
		assertEquals((byte) 1, _c.getX());
		assertTrue(!_c.getZeroFlag());
	}

	@Test
	public void testSetNegative() {
		_c.LDX((byte) 0xFF);
		assertEquals((byte) 0xFF, _c.getX());
		assertTrue(_c.getNegativeFlag());
	}
	
	@Test
	public void testResetNegative() {
		_c.LDX((byte) 10);
		assertEquals((byte) 10, _c.getX());
		assertTrue(!_c.getNegativeFlag());
	}
	
}
