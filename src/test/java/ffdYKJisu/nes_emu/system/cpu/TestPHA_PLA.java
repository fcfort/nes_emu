package ffdYKJisu.nes_emu.system.cpu;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.memory.ArrayCpuMemory;

public class TestPHA_PLA {

	NES _n;
	CPU _c;
	ArrayCpuMemory _mem;
	
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
