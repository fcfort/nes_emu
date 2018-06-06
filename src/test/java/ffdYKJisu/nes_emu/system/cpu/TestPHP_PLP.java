package ffdYKJisu.nes_emu.system.cpu;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.cartridge.Cartridge;
import ffdYKJisu.nes_emu.system.cartridge.CartridgeFactory;
import ffdYKJisu.nes_emu.system.memory.Addressable;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestPHP_PLP {

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
	public void testPushPopOneValue() {
		/* Bit No.       7   6   5   4   3   2   1   0
                         S   V       B   D   I   Z   C */
		_c.CLC();
		_c.CLV();
		_c.SEI();
		_c.SED();
		assertEquals((byte)0b00101100, _c.getSR());
		_c.PHP();
		_c.SEC();
		_c.CLD();
		assertEquals((byte)0b00100101, _c.getSR());
		_c.PLP();
		assertEquals((byte)0b00101100, _c.getSR());				
	}

}
