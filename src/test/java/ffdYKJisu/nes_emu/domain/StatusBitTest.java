package ffdYKJisu.nes_emu.domain;

import static org.junit.Assert.*;

import org.junit.Test;

public class StatusBitTest {

	@Test
	public void testAsByteAllFalse() {
		StatusBit s = new StatusBit();
		// only fifth bit should be set
		assertEquals(1 << 5, s.asByte());
	}
	
	@Test
	public void testAsByteAllTrue() {
		StatusBit s = new StatusBit();
		s.setCarry();
		s.setDecimal();
		s.setInterruptDisable();
		s.setNegative();
		s.setOverflow();
		s.setZero();
		assertEquals((byte)0xEF, s.asByte());
	}

	@Test
	public void testAsByteMix() {
		StatusBit s = new StatusBit();
		s.clearNegative(); // 0
		s.setOverflow(); // 1
		// 1
		s.setDecimal(); // 1
		s.setInterruptDisable(); // 1
		s.setZero(); // 1
		s.clearCarry(); // 0
		
		// 0b0111_1110 = 0x7E
		
		assertEquals((byte)0b0110_1110, s.asByte());
	}
	
}
