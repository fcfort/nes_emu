package ffdYKJisu.nes_emu.system.cpu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ffdYKJisu.nes_emu.system.memory.Addressable;
import org.junit.Before;
import org.junit.Test;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.NES;

public class TestBIT {

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
	public void testNegativeSet() {
		// before 
		_c.ADC((byte) 128);
		_c.CLV();
		assertTrue(_c.getNegativeFlag());
		assertTrue(!_c.getOverflowFlag());
		assertTrue(!_c.getZeroFlag());
		
		// op
		_c.BIT((byte) 0xF3);
		
		// after
		assertEquals((byte)128, _c.getA());
		assertTrue(_c.getNegativeFlag());
		assertTrue(_c.getOverflowFlag());
		assertTrue(!_c.getZeroFlag());
	}

	@Test
	public void testNothingSetInitially() {
		// before 
		_c.ADC((byte) 5);
		assertTrue(!_c.getNegativeFlag());
		assertTrue(!_c.getOverflowFlag());
		assertTrue(!_c.getZeroFlag());
		
		// op
		_c.BIT((byte) 0xF3);
		
		// after
		assertEquals(5, _c.getA());
		assertTrue(_c.getNegativeFlag());
		assertTrue(_c.getOverflowFlag());
		assertTrue(!_c.getZeroFlag());
	}

	@Test
	public void testAllSetFinally() {
		// before 
		_c.ADC((byte) 4);
		assertTrue(!_c.getNegativeFlag());
		assertTrue(!_c.getOverflowFlag());
		assertTrue(!_c.getZeroFlag());
		
		// op
		_c.BIT((byte) 0xF3);
		
		// after
		assertEquals(4, _c.getA());
		assertTrue(_c.getNegativeFlag());
		assertTrue(_c.getOverflowFlag());
		assertTrue(_c.getZeroFlag());
	}
	
	@Test
	public void testZeroSetInitially() {
		// before 
		_c.ADC((byte) 3);
		_c.LDX((byte) 0);
		assertTrue(!_c.getNegativeFlag());
		assertTrue(!_c.getOverflowFlag());
		assertTrue(_c.getZeroFlag());
		
		// op
		_c.BIT((byte) 0xF3);
		
		// after
		assertEquals(3, _c.getA());
		assertTrue(_c.getNegativeFlag());
		assertTrue(_c.getOverflowFlag());
		assertTrue(!_c.getZeroFlag());
	}
}
