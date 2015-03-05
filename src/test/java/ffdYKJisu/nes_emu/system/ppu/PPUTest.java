package ffdYKJisu.nes_emu.system.ppu;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.screen.Image;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.cpu.CPU;
import ffdYKJisu.nes_emu.util.HexUtils;

public class PPUTest {
	
	private static Logger logger = LoggerFactory.getLogger(PPUTest.class);
	
	PPU p;
	
	@Before
	public void initialize() throws UnableToLoadRomException {
		Cartridge c = new Cartridge(ClassLoader.getSystemResourceAsStream("Pac-Man (U) [!].nes"));
		NES _nes = new NES();
		_nes.setCart(c);
		p = _nes.getPPU();
		_nes.reset();
		/*
		NES n = mock(NES.class);
		CPU c = mock(CPU.class);
		Image i = mock(Image.class);
		when(n.getImage()).thenReturn(i);
		when(n.getCPU()).thenReturn(c);
		p = new PPU(n);
		p.reset();*/
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

	
	@Test
	public void testVerticalBlank() {
		// run until vertical blank
		
		logger.info(HexUtils.toHex(p.getStatusRegister()));
		while((p.getStatusRegister() & (1 << 7)) == 0) {
			p.runStep();
		}
		
		assertEquals(1, p.getHorizontalScroll());
		assertEquals(241, p.getVerticalScroll());
		
		// run until vblank cleared
		while((p.getStatusRegister() & (1 << 7)) != 0) {
			p.runStep();
		}
		
		assertEquals(1, p.getHorizontalScroll());
		assertEquals(261, p.getVerticalScroll());
	}
	
	@Test
	public void testEnableBackgroundRendering() {
		// only turn on background rendering
		p.write((short) 0x2001, (byte) 0b0000_1000);
		assertTrue(p.isBackgroundRenderingEnabled());
		assertTrue(p.isRenderingEnabled());
		
		while((p.getStatusRegister() & (1 << 7)) == 0) {
			p.runStep();
		}
	}
}
