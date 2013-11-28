/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ffdYKJisu.nes_emu.system.memory;

import ffdYKJisu.nes_emu.domain.uByte;
import ffdYKJisu.nes_emu.domain.uShort;
import ffdYKJisu.nes_emu.exceptions.AddressException;

/**
 *
 * @author fe01106
 */
public class PPUMemory implements Memory {

	// PPU memory
	uByte[] PatternTable0 = new uByte[PATTERN_TABLE_SIZE];
	uByte[] PatternTable1 = new uByte[PATTERN_TABLE_SIZE];
	
	uByte[] NameTable0 = new uByte[NAME_TABLE_SIZE];
	uByte[] NameTable1 = new uByte[NAME_TABLE_SIZE];
	uByte[] NameTable2 = new uByte[NAME_TABLE_SIZE];
	uByte[] NameTable3 = new uByte[NAME_TABLE_SIZE];
	
	uByte[] AttributeTable0 = new uByte[ATTRIBUTE_TABLE_SIZE];
	uByte[] AttributeTable1 = new uByte[ATTRIBUTE_TABLE_SIZE];
	uByte[] AttributeTable2 = new uByte[ATTRIBUTE_TABLE_SIZE];
	uByte[] AttributeTable3 = new uByte[ATTRIBUTE_TABLE_SIZE];
	
	uByte[] ImagePalette = new uByte[PALETTE_SIZE];
	uByte[] PaletteTable = new uByte[PALETTE_SIZE];
	
	uByte[] SpriteMemory = new uByte[SPRITE_RAM_SIZE];
	
	// Some useful constants
	static final int PATTERN_TABLE_SIZE = 0x1000;
	static final int NAME_TABLE_SIZE = 0x3C0;
	static final int ATTRIBUTE_TABLE_SIZE = 0x40;
	static final int PALETTE_SIZE = 0x10;
	static final int SPRITE_RAM_SIZE = 0x100;

	public uByte read(uShort address) {
		char addr = address.get();
		// Mirror of all PPU memory
		if (addr > 0x4000) {
			addr %= 0x4000;
		}
		// Sprite and Image palette
		if (addr < 0x4000 && addr >= 0x3F00) {
			addr = (char) ((addr % 0x20) + 0x3F00);
			// Image Palette
			if (addr >= 0x3F00 && addr < 0x3F10) {
				return new uByte(this.ImagePalette[addr % PALETTE_SIZE]);
			}
			// Sprite Palette
			if (addr >= 0x3F10 && addr < 0x3F20) {
				return new uByte(this.ImagePalette[addr % PALETTE_SIZE]);
			}
		}
		// Weird mirror
		if (addr < 0x3F00 && addr >= 0x3000) {
			addr -= 0x1000;
		}

		// Attribute and Name tables

		// Attribute 3
		if (addr < 0x3000 && addr >= 0x2FC0) {
			return new uByte(
				this.AttributeTable3[addr % ATTRIBUTE_TABLE_SIZE]);
		}
		// Name 3
		if (addr < 0x2FC0 && addr >= 0x2C00) {
			return new uByte(
				this.NameTable3[addr % NAME_TABLE_SIZE]);
		}
		// Attribute 2
		if (addr < 0x2C00 && addr >= 0x2BC0) {
			return new uByte(
				this.AttributeTable2[addr % ATTRIBUTE_TABLE_SIZE]);
		}
		// Name 2
		if (addr < 0x2BC0 && addr >= 0x2800) {
			return new uByte(
				this.NameTable2[addr % NAME_TABLE_SIZE]);
		}
		// Attribute 1
		if (addr < 0x2800 && addr >= 0x27C0) {
			return new uByte(
				this.AttributeTable1[addr % ATTRIBUTE_TABLE_SIZE]);
		}
		// Name 1
		if (addr < 0x27C0 && addr >= 0x2400) {
			return new uByte(
				this.NameTable1[addr % NAME_TABLE_SIZE]);
		}
		// Attribute 0
		if (addr < 0x2400 && addr >= 0x23C0) {
			return new uByte(
				this.AttributeTable0[addr % ATTRIBUTE_TABLE_SIZE]);
		}
		// Name 0
		if (addr < 0x23C0 && addr >= 0x2000) {
			return new uByte(
				this.NameTable0[addr % NAME_TABLE_SIZE]);
		}
		// Pattern table 1
		if (addr < 0x2000 && addr >= 0x1000) {
			return new uByte(this.PatternTable1[addr & PATTERN_TABLE_SIZE]);
		}
		// Pattern table 0
		if (addr < 0x1000) {
			return new uByte(this.PatternTable1[addr]);
		}
		System.out.println("Unrecognized address " + address);
		return new uByte(0);

	}

	public uByte read(uByte addrH, uByte addrL) {
		uShort address = new uShort(addrH, addrL);
        return this.read(address);
	}

	public uByte read(uByte zeroPageAddress) {
		uShort address =
                new uShort(uShort.toAddress(zeroPageAddress));
        return read(address);
	}

	public void write(uShort address, uByte val) throws AddressException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void write(uByte addrH, uByte addrL, uByte val) throws AddressException {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
