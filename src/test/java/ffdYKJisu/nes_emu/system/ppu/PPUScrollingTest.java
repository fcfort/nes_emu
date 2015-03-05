package ffdYKJisu.nes_emu.system.ppu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.cpu.CPU;

public class PPUScrollingTest {

	private static Logger logger = LoggerFactory.getLogger(PPUScrollingTest.class);
	
	PPU p;
	
	@Before
	public void initialize() {		
		NES n = mock(NES.class);
		CPU c = mock(CPU.class);
		when(n.getImage()).thenReturn(null);
		when(n.getCPU()).thenReturn(c);
		p = new PPU(n);
	}
	
	@Test
	public void testPPUControlWrite() {
		p.write((short)0x2000, (byte)0b1001_1011);
		logger.info("t : 0b{}", Integer.toBinaryString(p.getTemporaryVRAMAddress() & 0xFFFF));
		assertEquals(0b0000_1100_0000_0000, p.getTemporaryVRAMAddress());
	}
	
	@Test
	public void testPPUScrollWrite() {
		assertTrue(p.isFirstWrite());
		
		p.write((short)0x2005, (byte)0b1001_1011);
		
		logger.info("t : 0b{}", Integer.toBinaryString(p.getTemporaryVRAMAddress() & 0xFFFF));
		
		// assert high five bits of the write value are in the low five bits of the temp vram address
		assertEquals(0b0000_0000_0001_0011, p.getTemporaryVRAMAddress());
		// assert that fine x scroll is set to the low 3 bits of write value
		assertEquals(0b011, p.getFineXScroll());
		// assert that we've flipped the write flag
		assertFalse(p.isFirstWrite());
	}
	
	@Test
	public void testPPUScrollSecondWrite() {
		assertTrue(p.isFirstWrite());
		
		p.write((short)0x2005, (byte)0b0);

		assertFalse(p.isFirstWrite());
		
		p.write((short)0x2005, (byte)0b1111_1111);
		
		logger.info("t : 0b{}", Integer.toBinaryString(p.getTemporaryVRAMAddress() & 0xFFFF));
			
		assertEquals(0b0111_0011_1110_0000, p.getTemporaryVRAMAddress());
		// assert that we've flipped the write flag
		assertTrue(p.isFirstWrite());
	}
	
	@Test
	public void testPPUAddrWrite() {
		assertTrue(p.isFirstWrite());
		
		p.write((short)0x2006, (byte)0b1111_1111);
		
		logger.info("t : 0b{}", Integer.toBinaryString(p.getTemporaryVRAMAddress() & 0xFFFF));
		 
		assertEquals(0b0011_1111_0000_0000, p.getTemporaryVRAMAddress());
		// assert that we've flipped the write flag
		assertFalse(p.isFirstWrite());
	}
	
	@Test
	public void testPPUAddrSecondWrite() {
		assertTrue(p.isFirstWrite());
		
		p.write((short)0x2006, (byte)0b0);
		
		assertFalse(p.isFirstWrite());
		
		p.write((short)0x2006, (byte)0b1111_1111);
		
		logger.info("t : 0b{}", Integer.toBinaryString(p.getTemporaryVRAMAddress() & 0xFFFF));
		
		// copy value to lower byte of t
		assertEquals(0b0000_0000_1111_1111, p.getTemporaryVRAMAddress());
		// copy t to v		
		assertEquals(0b0000_0000_1111_1111, p.getCurrentVRAMAddress());
		// assert that we've flipped the write flag
		assertTrue(p.isFirstWrite());
	}
}
