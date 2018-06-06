package ffdYKJisu.nes_emu.system.cpu;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.cartridge.Cartridge;
import ffdYKJisu.nes_emu.system.cartridge.CartridgeFactory;
import ffdYKJisu.nes_emu.system.memory.Addressable;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestCMP {

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
