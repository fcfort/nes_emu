package ffdYKJisu.nes_emu.system.cpu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.memory.CPUMemory;

public class TestASL {

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
	public void testSetCarryAndZero() {
		// before
		_c.ADC((byte) 128);		
		assertTrue(_c.getNegativeFlag());
		assertTrue(!_c.getCarryFlag());
		assertTrue(!_c.getZeroFlag());
		
		// operation
		_c.ASL();
		
		// after	
		assertEquals(0, _c.getA());
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
		_c.ASL();
		
		// after	
		assertEquals((byte)128, _c.getA());
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
		_c.ASL();
		
		// after	
		assertEquals((byte)128, _c.getA());
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
		_c.ASL();
		
		// after	
		assertEquals(16, _c.getA());
		assertTrue(!_c.getNegativeFlag());
		assertTrue(!_c.getCarryFlag());
		assertTrue(!_c.getZeroFlag());
	}

}
