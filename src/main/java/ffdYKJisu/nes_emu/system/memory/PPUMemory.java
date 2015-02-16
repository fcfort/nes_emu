/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ffdYKJisu.nes_emu.system.memory;

import com.google.common.primitives.Shorts;

import ffdYKJisu.nes_emu.exceptions.InvalidAddressException;
import ffdYKJisu.nes_emu.system.ppu.PPU;

public class PPUMemory implements IMemory {

	// PPU memory
	byte[] PatternTable0 = new byte[PATTERN_TABLE_SIZE];
	byte[] PatternTable1 = new byte[PATTERN_TABLE_SIZE];
	
	byte[] NameTable0 = new byte[NAME_TABLE_SIZE];
	byte[] NameTable1 = new byte[NAME_TABLE_SIZE];
	byte[] NameTable2 = new byte[NAME_TABLE_SIZE];
	byte[] NameTable3 = new byte[NAME_TABLE_SIZE];
	
	byte[] AttributeTable0 = new byte[ATTRIBUTE_TABLE_SIZE];
	byte[] AttributeTable1 = new byte[ATTRIBUTE_TABLE_SIZE];
	byte[] AttributeTable2 = new byte[ATTRIBUTE_TABLE_SIZE];
	byte[] AttributeTable3 = new byte[ATTRIBUTE_TABLE_SIZE];
	
	byte[] ImagePalette = new byte[PALETTE_SIZE];
	byte[] PaletteTable = new byte[PALETTE_SIZE];
	
	byte[] SpriteMemory = new byte[SPRITE_RAM_SIZE];
	
	// Some useful constants
	static final int PATTERN_TABLE_SIZE = 0x1000;
	static final int NAME_TABLE_SIZE = 0x3C0;
	static final int ATTRIBUTE_TABLE_SIZE = 0x40;
	static final int PALETTE_SIZE = 0x10;
	static final int SPRITE_RAM_SIZE = 0x100;

	private final PPU _ppu;
	
	public PPUMemory(PPU ppu_) {
		_ppu = ppu_;
	}

	public byte read(short address) {
		
		char addr = (char) address;
		// Mirror of all PPU memory
		if (addr > 0x4000) {
			addr %= 0x4000;
		}
		// Sprite and Image palette
		if (addr < 0x4000 && addr >= 0x3F00) {
			addr = (char) ((addr % 0x20) + 0x3F00);
			// Image Palette
			if (addr >= 0x3F00 && addr < 0x3F10) {
				return this.ImagePalette[addr % PALETTE_SIZE];
			}
			// Sprite Palette
			if (addr >= 0x3F10 && addr < 0x3F20) {
				return this.ImagePalette[addr % PALETTE_SIZE];
			}
		}
		// Weird mirror
		if (addr < 0x3F00 && addr >= 0x3000) {
			addr -= 0x1000;
		}

		// Attribute and Name tables

		// Attribute 3
		if (addr < 0x3000 && addr >= 0x2FC0) {
			return 
				this.AttributeTable3[addr % ATTRIBUTE_TABLE_SIZE];
		}
		// Name 3
		if (addr < 0x2FC0 && addr >= 0x2C00) {
			return 
				this.NameTable3[addr % NAME_TABLE_SIZE];
		}
		// Attribute 2
		if (addr < 0x2C00 && addr >= 0x2BC0) {
			return this.AttributeTable2[addr % ATTRIBUTE_TABLE_SIZE];
		}
		// Name 2
		if (addr < 0x2BC0 && addr >= 0x2800) {
			return this.NameTable2[addr % NAME_TABLE_SIZE];
		}
		// Attribute 1
		if (addr < 0x2800 && addr >= 0x27C0) {
			return this.AttributeTable1[addr % ATTRIBUTE_TABLE_SIZE];
		}
		// Name 1
		if (addr < 0x27C0 && addr >= 0x2400) {
			return this.NameTable1[addr % NAME_TABLE_SIZE];
		}
		// Attribute 0
		if (addr < 0x2400 && addr >= 0x23C0) {
			return this.AttributeTable0[addr % ATTRIBUTE_TABLE_SIZE];
		}
		// Name 0
		if (addr < 0x23C0 && addr >= 0x2000) {
			return 
				this.NameTable0[addr % NAME_TABLE_SIZE];
		}
		// Pattern table 1
		if (addr < 0x2000 && addr >= 0x1000) {
			return this.PatternTable1[addr & PATTERN_TABLE_SIZE];
		}
		// Pattern table 0
		if (addr < 0x1000) {
			return this.PatternTable1[addr];
		}
		System.out.println("Unrecognized address " + address);
		return 0;

	}

	public byte read(byte addrH, byte addrL) {
		short address = Shorts.fromBytes(addrH, addrL);
        return this.read(address);
	}

	public byte read(byte zeroPageAddress) {
		short address = zeroPageAddress;
        return read(address);
	}

	public void write(short address, byte val) throws InvalidAddressException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void write(byte addrH, byte addrL, byte val) throws InvalidAddressException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
