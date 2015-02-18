package ffdYKJisu.nes_emu.system.cpu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.memory.CPUMemory;

public class TestPHA_PLA {

	NES _n;
	CPU _c;
	CPUMemory _mem; 
	
	@Before
	public void initialize() throws UnableToLoadRomException {
		Cartridge c = new Cartridge(ClassLoader.getSystemResourceAsStream("Pac-Man (U) [!].nes"));
		NES _nes = new NES();
		_c = _nes.getCPU();
		_mem = _c.getCPUMemory();
		_mem.writeCartToMemory(c);
		_c.reset();
	}
	
	@Test
	public void testPushPopOneValue() {
		_c.LDA((byte) 124);
		assertEquals((byte)124, _c.getA());
		_c.PHA();
		_c.LDA((byte) 100);
		assertEquals((byte)100, _c.getA());
		_c.PLA();
		assertEquals((byte)124, _c.getA());		
	}
	
	@Test
	public void testPushPopTwoValues() {
		_c.LDA((byte) 124);
		_c.PHA();
		_c.LDA((byte) 100);
		_c.PHA();		
		assertEquals((byte)100, _c.getA());
		_c.PLA();
		assertEquals((byte)100, _c.getA());
		_c.PLA();
		assertEquals((byte)124, _c.getA());		
	}
}
