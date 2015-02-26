package ffdYKJisu.nes_emu.system.cpu;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.memory.CPUMemory;

public class TestCMP {

	NES _n;
	CPU _c;
	CPUMemory _mem; 
	
	@Before
	public void initialize() throws UnableToLoadRomException {
		Cartridge c = new Cartridge(ClassLoader.getSystemResourceAsStream("Pac-Man (U) [!].nes"));
		NES _nes = new NES();
		_nes.setCart(c);
		_c = _nes.getCPU();
		_mem = _c.getMemory();
		_c.reset();
	}
	
	@Test
	public void testSameNumber() {
		_c.LDA((byte) 26);
		
		// op
		_c.CMP((byte) 26);
		
		// after
		assertTrue(_c.getZeroFlag());
		assertTrue(!_c.getNegativeFlag());
		assertTrue(_c.getCarryFlag());
	}
	
	@Test
	public void testLargeA() {
		_c.LDA((byte) 48);
		
		// op
		_c.CMP((byte) 26);
		
		// after
		assertTrue(!_c.getZeroFlag());
		assertTrue(!_c.getNegativeFlag());
		assertTrue(_c.getCarryFlag());
	}
	
	@Test
	public void testLargeNegativeA() {
		_c.LDA((byte) 130);
		
		// op
		_c.CMP((byte) 26);
		
		// after
		assertTrue(!_c.getZeroFlag());
		assertTrue(!_c.getNegativeFlag());
		assertTrue(_c.getCarryFlag());
	}
	
	@Test
	public void testSmallA() {
		_c.LDA((byte) 8);
		
		// op
		_c.CMP((byte) 26);
		
		// after
		assertTrue(!_c.getZeroFlag());
		assertTrue(_c.getNegativeFlag());
		assertTrue(!_c.getCarryFlag());
	}
}
