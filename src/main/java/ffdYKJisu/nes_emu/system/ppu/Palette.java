package ffdYKJisu.nes_emu.system.ppu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ffdYKJisu.nes_emu.util.HexUtils;


/**
 * http://wiki.nesdev.com/w/index.php/PPU_palettes#RGB_palette_values
 */
public class Palette {
	
	private static Logger logger = LoggerFactory.getLogger(Palette.class);
	
	/*  84  84  84     0  30 116    8  16 144   48   0 136   68   0 100   92   0  48   84   4   0   60  24   0   32  42   0    8  58   0    0  64   0    0  60   0    0  50  60    0   0   0
		152 150 152    8  76 196   48  50 236   92  30 228  136  20 176  160  20 100  152  34  32  120  60   0   84  90   0   40 114   0    8 124   0    0 118  40    0 102 120    0   0   0
		236 238 236   76 154 236  120 124 236  176  98 236  228  84 236  236  88 180  236 106 100  212 136  32  160 170   0  116 196   0   76 208  32   56 204 108   56 180 204   60  60  60
		236 238 236  168 204 236  188 188 236  212 178 236  236 174 236  236 174 212  236 180 176  228 196 144  204 210 120  180 222 120  168 226 144  152 226 180  160 214 228  160 162 160
	 */
	private static final int[][] PALETTE_RGB_VALUES = {
		// 0x00
		{84,84,84}, // 1
		{0,30,116},
		{8,16,144},
		{48,0,136},
		{68,0,100}, // 5
		{92,0,48},
		{84,4,0},
		{60,24,0},
		{32,42,0},
		{8,58,0}, // 10
		{0,64,0},
		{0,60,0},
		{0,50,60}, // 13		
		{0,0,0}, // "blacker than black", do not use
		{-1, -1, -1}, // dummy values
		{-1, -1, -1}, // dummy values

		// 0x10
		{152,150,152},
		{8,76,196},
		{48,50,236},
		{92,30,228},
		{136,20,176},
		{160,20,100},
		{152,34,32},
		{120,60,0},
		{84,90,0},
		{40,114,0},
		{8,124,0},
		{0,118,40},
		{0,102,120},
		{0,0,0}, // unmirrored black
		{-1, -1, -1}, // dummy values
		{-1, -1, -1}, // dummy values
		
		{236,238,236},
		{76,154,236},
		{120,124,236},
		{176,98,236},
		{228,84,236},
		{236,88,180},
		{236,106,100},
		{212,136,32},
		{160,170,0},
		{116,196,0},
		{76,208,32},
		{56,204,108},
		{56,180,204},
		{60,60,60},
		{-1, -1, -1}, // dummy values
		{-1, -1, -1}, // dummy values
		
		{236,238,236},
		{168,204,236},
		{188,188,236},
		{212,178,236},
		{236,174,236},
		{236,174,212},
		{236,180,176},
		{228,196,144},
		{204,210,120},
		{180,222,120},
		{168,226,144},
		{152,226,180},
		{160,214,228},
		{160,162,160}
		// dummy values not needed here
	};
	
	public static int getRGBValue(byte paletteValue_) {
		byte unmirroredPaletteValue = paletteValue_;
		
		// hues $E and $F are mirrors of $1D		
		if((paletteValue_ & 0xE) == 0xE) {
			unmirroredPaletteValue = 0x1D;
		}
		
		int[] rgbValues = PALETTE_RGB_VALUES[Byte.toUnsignedInt(unmirroredPaletteValue)];
		byte r = (byte) rgbValues[2];
		byte g = (byte) rgbValues[1];
		byte b = (byte) rgbValues[0];
		
		logger.info("For value {} got unmirrored value {} with r, g, b {}, {}, {}", new Object[] {
			HexUtils.toHex(paletteValue_), HexUtils.toHex(unmirroredPaletteValue), HexUtils.toHex(r), HexUtils.toHex(g), HexUtils.toHex(b)
		});
		
		int rgbValue = (rgbValues[0] << 16) | (rgbValues[1] << 8) | rgbValues[2];
		
		return rgbValue; 
	}
}
