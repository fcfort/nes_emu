package ffdYKJisu.nes_emu.system.cpu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.HexUtils;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.memory.CPUMemory;

public class TestROL {

	NES _n;
	CPU _c;
	CPUMemory _mem; 
	
	@Before
	public void initialize() throws UnableToLoadRomException {
		Cartridge c = new Cartridge(ClassLoader.getSystemResourceAsStream("Pac-Man (U) [!].nes"));
		NES _nes = new NES();
		_c = _nes.getCPU();
		_mem = _c.getCPUMemory();
		_mem.writeCartToMemory(c);
		_c.reset();
	}
		
	@Test
	public void testRotateLeftOnceNoCarry() {
		// before
		_c.LDA((byte) 0b0101_0101);
		
		byte result = _c.ROL(_c.getA());
				
		// after	
		assertEquals((byte)0b1010_1010, result);
		assertTrue(_c.getNegativeFlag());
		assertTrue(!_c.getCarryFlag());
		assertTrue(!_c.getZeroFlag());
	}
	
	@Test
	public void testRotateLeftOnceCarry() {
		// before
		_c.LDA((byte) 0b1101_0100);
		
		byte result = _c.ROL(_c.getA());
				
		// 0xA9 = 1010 1001
		System.out.println(HexUtils.toHex(result));
		System.out.println(HexUtils.toHex((byte)0b1010_0101));
		
		// after	
		assertEquals((byte)0b1010_1001, result); 
		assertTrue(_c.getNegativeFlag());
		assertTrue(_c.getCarryFlag());
		assertTrue(!_c.getZeroFlag());
	}
	
	@Test
	public void testRotateEightTimes() {
		// before
		_c.LDA((byte) 0b1101_0100);
		
		byte result = rotate(8);
		
		// after	
		assertEquals((byte)0b1101_0100, result);
	}
	
	@Test
	public void testRotateZero() {
		byte result = _c.ROL((byte) 0);
		// after	
		assertEquals(0, result);
		assertTrue(_c.getZeroFlag());
	}
	
	private byte rotate(int rotateCount_) {
		for(int i = 0; i < rotateCount_; i++) {
			_c.LDA(_c.ROL(_c.getA()));	
		}
		
		return _c.getA();
		
	}
	
	
}
