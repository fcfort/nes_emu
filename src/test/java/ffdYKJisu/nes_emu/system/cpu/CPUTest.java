package ffdYKJisu.nes_emu.system.cpu;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.memory.CPUMemory;

public class CPUTest {

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
		assertEquals(0xFF, _c.getA());
		assertTrue(_c.getNegativeFlag());
		assertTrue(_c.getOverflowFlag());
	}

}
