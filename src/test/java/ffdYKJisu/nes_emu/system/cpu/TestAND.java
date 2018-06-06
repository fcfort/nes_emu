package ffdYKJisu.nes_emu.system.cpu;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.cartridge.Cartridge;
import ffdYKJisu.nes_emu.system.cartridge.CartridgeFactory;
import ffdYKJisu.nes_emu.system.memory.Addressable;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestAND {

	NES _n;
	CPU _c;
	Addressable _mem;
	
	@Before
	public void initialize() throws UnableToLoadRomException {
    Cartridge c =
        new CartridgeFactory()
            .fromInputStream(ClassLoader.getSystemResourceAsStream("Pac-Man (U) [!].nes"));
    NES _nes = new NES(c);
		_c = _nes.getCPU();
		_mem = _c.getMemory();
		_c.reset();
	}
	
	@Test
	public void testSetZero() {
		_c.ADC((byte) 5);
		assertTrue(!_c.getZeroFlag());
		assertTrue(!_c.getNegativeFlag());
		_c.AND((byte) 8);
		assertEquals(0, _c.getA());
		assertTrue(_c.getZeroFlag());
		assertTrue(!_c.getNegativeFlag());
	}
	
	@Test
	public void testResetNegative() {
		_c.ADC((byte) 0xFE);
		assertTrue(!_c.getZeroFlag());
		assertTrue(_c.getNegativeFlag());
		_c.AND((byte) 0x5F);
		assertEquals(0x5E, _c.getA());
		assertTrue(!_c.getZeroFlag());
		assertTrue(!_c.getNegativeFlag());
	}
}
