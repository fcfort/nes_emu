package ffdYKJisu.nes_emu.system.cpu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ffdYKJisu.nes_emu.system.memory.Addressable;
import org.junit.Before;
import org.junit.Test;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.NES;

public class TestASL {

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
	public void testSetCarryAndZero() {
		// before
		_c.ADC((byte) 128);		
		assertTrue(_c.getNegativeFlag());
		assertTrue(!_c.getCarryFlag());
		assertTrue(!_c.getZeroFlag());
		
		// operation
		byte result = _c.ASL(_c.getA());
		
		// after	
		assertEquals(0, result);
		assertTrue(!_c.getNegativeFlag());
		assertTrue(_c.getCarryFlag());
		assertTrue(_c.getZeroFlag());
	}
	
	@Test
	public void testSetNegative() {
		// before
		_c.ADC((byte) 64);		
		assertTrue(!_c.getNegativeFlag());
		assertTrue(!_c.getCarryFlag());
		assertTrue(!_c.getZeroFlag());
		
		// operation
		byte result = _c.ASL(_c.getA());
		
		// after	
		assertEquals((byte)128, result);
		assertTrue(_c.getNegativeFlag());
		assertTrue(!_c.getCarryFlag());
		assertTrue(!_c.getZeroFlag());
	}
	
	@Test
	public void testSetNegativeAndCarry() {
		// before
		_c.ADC((byte) 192);		
		assertTrue(_c.getNegativeFlag());
		assertTrue(!_c.getCarryFlag());
		assertTrue(!_c.getZeroFlag());
		
		// operation
		byte result = _c.ASL(_c.getA());
		
		// after	
		assertEquals((byte)128, result);
		assertTrue(_c.getNegativeFlag());
		assertTrue(_c.getCarryFlag());
		assertTrue(!_c.getZeroFlag());
	}
	
	@Test
	public void testDoubleEight() {
		// before
		_c.ADC((byte) 8);		
		assertTrue(!_c.getNegativeFlag());
		assertTrue(!_c.getCarryFlag());
		assertTrue(!_c.getZeroFlag());
		
		// operation
		byte result = _c.ASL(_c.getA());
		
		// after	
		assertEquals(16, result);
		assertTrue(!_c.getNegativeFlag());
		assertTrue(!_c.getCarryFlag());
		assertTrue(!_c.getZeroFlag());
	}

}
