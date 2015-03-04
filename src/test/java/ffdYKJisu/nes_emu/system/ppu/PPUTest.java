package ffdYKJisu.nes_emu.system.ppu;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ffdYKJisu.nes_emu.system.NES;

public class PPUTest {
	
	private static Logger logger = LoggerFactory.getLogger(PPUTest.class);
	
	PPU p;
	
	@Before
	public void initialize() {		
		NES n = mock(NES.class);
		when(n.getImage()).thenReturn(null);
		p = new PPU(n);
	}
	
	@Test
	public void testHorizontalCopy() {
		p.setCurrentVRAMAddress((short)   0b111_1111_1011_1010);
		p.setTemporaryVRAMAddress((short) 0b111_1001_0001_0101);
		
		logger.info("t = {}, v = {}", Integer.toBinaryString(p.getTemporaryVRAMAddress()), Integer.toBinaryString(p.getCurrentVRAMAddress()));
		p.copyHorizontalTtoV();		
		logger.info("t = {}, v = {}", Integer.toBinaryString(p.getTemporaryVRAMAddress()), Integer.toBinaryString(p.getCurrentVRAMAddress()));
		
		assertEquals(0b111_1011_1011_0101, p.getCurrentVRAMAddress());
	}

}
