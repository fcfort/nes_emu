package ffdYKJisu.nes_emu.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class UnsignedShortsTest {

	@Test
	public void testLowerTwoBitMask() {
		short a = 0b1010_0110;
		short b = 0b0000_0011;
		
		a = UnsignedShorts.setBitRange(b, a, 1, 0);
		
		assertEquals((short)0b1010_0111, a);
	}

	@Test
	public void testMiddleTwoBitMask() {
		short a = 0b1011_1011;
		short b = 0b1010_1101;
		          //7654_3210 
		a = UnsignedShorts.setBitRange(b, a, 6, 3);
		
		assertEquals((short)0b1010_1011, a);
	}

	
}
