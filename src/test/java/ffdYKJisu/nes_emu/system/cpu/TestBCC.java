package ffdYKJisu.nes_emu.system.cpu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.HexUtils;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.memory.CPUMemory;

public class TestBCC {

	private static Logger logger = LoggerFactory.getLogger(CPUMemoryTest.class);

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
	public void testCarryNotSet() {
		_c.CLC();
		assertTrue(!_c.getCarryFlag());
		short beforePC = _c.getPC();		
		_c.BCC((byte) 1);
		assertEquals(beforePC, _c.getPC());
	}
	
	@Test
	public void testPositiveBranch() {
		_c.SEC();
		assertTrue(_c.getCarryFlag());
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
		
		_c.SEC();
		assertTrue(_c.getCarryFlag());
		short beforePC = _c.getPC();
		
		_c.BCC(branchDistance);
		assertEquals(beforePC + branchDistance, _c.getPC());
		logger.info("PC before {} PC after {} with branch of {}", 
				new Object[] {HexUtils.toHex(beforePC), HexUtils.toHex(_c.getPC()), branchDistance }
		);
	}
	
	@Test
	public void testWrapAround() {
		_c.SEC();
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
