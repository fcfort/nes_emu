package ffdYKJisu.nes_emu.system.cpu;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.cartridge.Cartridge;
import ffdYKJisu.nes_emu.system.cartridge.CartridgeFactory;
import ffdYKJisu.nes_emu.system.memory.Addressable;
import ffdYKJisu.nes_emu.util.HexUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestBCC {

	private static Logger logger = LoggerFactory.getLogger(TestBCC.class);

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
	public void testCarrySet() {
		_c.SEC();
		assertTrue(_c.getCarryFlag());
		short beforePC = _c.getPC();		
		_c.BCC((byte) 1);
		assertEquals(beforePC, _c.getPC());
	}
	
	
	@Test
	public void testPositiveBranch() {
		_c.CLC();
		assertTrue(!_c.getCarryFlag());
		short beforePC = _c.getPC();		
		_c.BCC((byte) 1);
		assertEquals(beforePC + 1, _c.getPC());
		logger.info("PC before {} PC after {} with branch of {}", 
				new Object[] {HexUtils.toHex(beforePC), HexUtils.toHex(_c.getPC()), 1 }
		);
	}
	
	@Test
	public void testNegativeBranch() {
		byte branchDistance = -16;
		
		_c.CLC();
		assertTrue(!_c.getCarryFlag());
		short beforePC = _c.getPC();
		
		_c.BCC(branchDistance);
		assertEquals(beforePC + branchDistance, _c.getPC());
		logger.info("PC before {} PC after {} with branch of {}", 
				new Object[] {HexUtils.toHex(beforePC), HexUtils.toHex(_c.getPC()), branchDistance }
		);
	}
	
	@Test
	public void testWrapAround() {
		_c.CLC();
		short beforePC = _c.getPC();
		byte branchDistance = (byte) 127;		
		_c.BCC(branchDistance);
		_c.BCC(branchDistance);
		_c.BCC(branchDistance);
		
		assertEquals(beforePC + branchDistance*3, _c.getPC());
		logger.info("PC before {} PC after {} with branch of {}", 
				new Object[] {HexUtils.toHex(beforePC), HexUtils.toHex(_c.getPC()), branchDistance*3 }
		);
	}
	
}
