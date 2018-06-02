package ffdYKJisu.nes_emu.system.cpu;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ffdYKJisu.nes_emu.system.memory.Addressable;
import org.junit.Before;
import org.junit.Test;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.NES;

/**
 * http://www.6502.org/tutorials/vflag.html
 */
public class TestOverflowFlag {
	
	NES _n;
	CPU _c;
	Addressable _mem;
	
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
	public void testPositivePositiveNoOverflow() {
		_c.CLC();
		_c.LDA((byte) 0x01);
		_c.ADC((byte) 0x01);
		assertFalse(_c.getOverflowFlag());
	}
	
	@Test 
	public void testPositiveNegativeNoOverflow() {
		_c.CLC();
		_c.LDA((byte) 0x01);
		_c.ADC((byte) 0xFF);
		assertFalse(_c.getOverflowFlag());
	}
	
	@Test
	public void testPositivePositiveOverflow() {
		_c.CLC();
		_c.LDA((byte) 0x7F);
		_c.ADC((byte) 0x01);
		assertTrue(_c.getOverflowFlag());
	}
	
	@Test
	public void testNegativeNegativeOverflow() {
		_c.CLC();
		_c.LDA((byte) 0x80);
		_c.ADC((byte) 0xFF);
		assertTrue(_c.getOverflowFlag());
	}
	
	@Test
	public void testZeroNegativeSubtractionNoOverflow() {
		_c.SEC();
		_c.LDA((byte) 0x00);
		_c.SBC((byte) 0x01);
		assertFalse(_c.getOverflowFlag());
	}
	
	@Test
	public void testNegativeNegativeSubtractionOverflow() {
		_c.SEC();
		_c.LDA((byte) 0x80);
		_c.SBC((byte) 0x01);
		assertTrue(_c.getOverflowFlag());
	}
	
	@Test
	public void testNegativePositiveSubtractionOverflow() {
		_c.SEC();
		_c.LDA((byte) 0x7F);
		_c.SBC((byte) 0xFF);
		assertTrue(_c.getOverflowFlag());
	}

}
