package ffdYKJisu.nes_emu.system.cpu;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.memory.CPUMemory;

public class TestADC {

	NES _n;
	CPU _c;
	CPUMemory _mem; 
	
	@Before
	public void initialize() throws UnableToLoadRomException {
		Cartridge c = new Cartridge(ClassLoader.getSystemResourceAsStream("Pac-Man (U) [!].nes"));
		NES _nes = new NES();
		_nes.setCart(c);
		_c = _nes.getCPU();
		_c.reset();
	}
	
	@Test
	public void testADCAddOne() {
		_c.ADC((byte) 1);
		assertEquals(1, _c.getA());
	}
	
	@Test
	public void testADCAddTwoAndThree() {
		_c.ADC((byte) 2);
		_c.ADC((byte) 3);
		assertEquals(5, _c.getA());
		assertTrue(!_c.getZeroFlag());
	}
	
	@Test
	public void testADCZeroFlagUnSet() {
		_c.ADC((byte) 1);
		assertTrue(!_c.getZeroFlag());
	}
	
	@Test
	public void testADCZeroFlagSet() {
		_c.ADC((byte) 0);
		assertTrue(_c.getZeroFlag());
	}
	
	@Test
	public void testADCCarryFlag() {
		_c.ADC((byte) 0xff);
		_c.ADC((byte) 0x01);
		assertTrue(_c.getCarryFlag());
	}
	
	@Test
	public void testADCCarryFlagSet() {
		_c.ADC((byte) 2);
		_c.SEC();
		_c.ADC((byte) 3);
		assertEquals(6, _c.getA());
	}
	
	@Test
	public void testADCSetCarryAndZero() {
		_c.ADC((byte) 2);
		_c.ADC((byte) 254);
		assertEquals(0, _c.getA());
		assertTrue(_c.getCarryFlag());
		assertTrue(_c.getZeroFlag());
	}
	
	@Test
	public void testADCSetNegativeAndOverflow() {
		_c.ADC((byte) 2);
		_c.ADC((byte) 253);
		assertEquals((byte)0xFF, _c.getA());
		assertTrue(_c.getNegativeFlag());
		assertTrue(!_c.getOverflowFlag());
	}
	
	@Test
	public void testADCSetCarryOverflowUnsetNegative() {
		_c.ADC((byte) 253);
		_c.CLV();
		assertTrue(_c.getNegativeFlag());
		assertTrue(!_c.getOverflowFlag());
		_c.ADC((byte) 6);
		assertEquals((byte) 3, _c.getA());
		assertTrue(!_c.getNegativeFlag());
		assertTrue(_c.getCarryFlag());
		assertTrue(!_c.getOverflowFlag());
	}
	
	@Test
	public void testADCNegativeOverflowSetCarryUnset() {		
		_c.ADC((byte) 125);
		_c.SEC();
		assertTrue(_c.getCarryFlag());
		assertTrue(!_c.getOverflowFlag());
		_c.ADC((byte) 2);
		assertEquals((byte) 128, _c.getA());
		assertTrue(_c.getNegativeFlag());
		assertTrue(!_c.getCarryFlag());
		assertTrue(_c.getOverflowFlag());
	}

	
	@Test
	public void testADCOverflow() {
		_c.LDA((byte) 0xE4);
		_c.PHA();
		_c.PLP();
		_c.LDA((byte) 0x7F);
		_c.ADC((byte) 0x80);
		assertEquals((byte)0xFF, _c.getA());
		assertFalse(_c.getOverflowFlag());
		assertEquals((byte)0xA4, _c.getSR());
	}
	
}
