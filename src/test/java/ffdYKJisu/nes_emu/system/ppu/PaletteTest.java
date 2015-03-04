package ffdYKJisu.nes_emu.system.ppu;

import static org.junit.Assert.*;

import org.junit.Test;

public class PaletteTest {

	@Test
	public void testBlackerThanBlack() {
		assertEquals(0x0, Palette.getRGBValue((byte) 0x0D));
	}
	
	@Test
	public void testColorSix() {
		// {92,0,48}
		// 0x5C, 0x00, 0x30
		assertEquals(0x5C0030, Palette.getRGBValue((byte) 5));
	}
	
	@Test
	public void testMirroredBlacks() {
		assertEquals(0x0, Palette.getRGBValue((byte) 0x0E));
		assertEquals(0x0, Palette.getRGBValue((byte) 0x0F));
		assertEquals(0x0, Palette.getRGBValue((byte) 0x1E));
		assertEquals(0x0, Palette.getRGBValue((byte) 0x1F));
		assertEquals(0x0, Palette.getRGBValue((byte) 0x2E));
		assertEquals(0x0, Palette.getRGBValue((byte) 0x2F));
		assertEquals(0x0, Palette.getRGBValue((byte) 0x3E));
		assertEquals(0x0, Palette.getRGBValue((byte) 0x3F));
	}

}
