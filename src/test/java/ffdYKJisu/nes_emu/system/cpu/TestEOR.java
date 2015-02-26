package ffdYKJisu.nes_emu.system.cpu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.memory.CPUMemory;

public class TestEOR {

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
	public void test133_186() {
		_c.LDA((byte) 133);		
		_c.EOR((byte) 186);
		assertEquals(63, _c.getA());
		assertTrue(!_c.getNegativeFlag());
	}
	
	@Test
	public void test143_Complement() {
		_c.LDA((byte) 143);		
		_c.EOR((byte) 255);
		assertEquals(112, _c.getA());
		assertTrue(!_c.getNegativeFlag());
	}
}
