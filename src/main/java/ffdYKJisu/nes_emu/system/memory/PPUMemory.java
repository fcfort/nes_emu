/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ffdYKJisu.nes_emu.system.memory;

import com.google.common.primitives.Shorts;

import ffdYKJisu.nes_emu.exceptions.InvalidAddressException;
import ffdYKJisu.nes_emu.system.ppu.PPU;
import ffdYKJisu.nes_emu.util.UnsignedShorts;

public class PPUMemory implements IMemory {

	
	// Some useful constants
	private static final int PATTERN_TABLE_SIZE = 0x1000;
	private static final int NAME_TABLE_SIZE = 0x3C0;
	private static final int ATTRIBUTE_TABLE_SIZE = 0x40;
	private static final int PALETTE_SIZE = 0x10;
	private static final int SPRITE_RAM_SIZE = 0x100;

	private static final int PATTERN_TABLE_0_LOC = 0x0000;
	private static final int PATTERN_TABLE_1_LOC = 0x1000;
	
	private static final int NAME_TABLE_0_LOC = 0x2000;
	private static final int NAME_TABLE_1_LOC = 0x2400;
	private static final int NAME_TABLE_2_LOC = 0x2800;
	private static final int NAME_TABLE_3_LOC = 0x2C00;
	
	private static final int PALETTE_LOC = 0x3F00;
	
	// Pattern Tables
	byte[] PatternTable0 = new byte[PATTERN_TABLE_SIZE];
	byte[] PatternTable1 = new byte[PATTERN_TABLE_SIZE];
	
	// Name Tables
	byte[] NameTable0 = new byte[NAME_TABLE_SIZE];
	byte[] NameTable1 = new byte[NAME_TABLE_SIZE];
	byte[] NameTable2 = new byte[NAME_TABLE_SIZE];
	byte[] NameTable3 = new byte[NAME_TABLE_SIZE];
	
	// OAM - Object Attribute Memory
	byte[] AttributeTable0 = new byte[ATTRIBUTE_TABLE_SIZE];
	byte[] AttributeTable1 = new byte[ATTRIBUTE_TABLE_SIZE];
	byte[] AttributeTable2 = new byte[ATTRIBUTE_TABLE_SIZE];
	byte[] AttributeTable3 = new byte[ATTRIBUTE_TABLE_SIZE];
	
	byte[] ImagePalette = new byte[PALETTE_SIZE];
	byte[] PaletteTable = new byte[PALETTE_SIZE];
	
	byte[] SpriteMemory = new byte[SPRITE_RAM_SIZE];

	private static final AddressLocation[] MEMORY_ORDER = {
		AddressLocation.PATTERN_TABLE_0,
		AddressLocation.PATTERN_TABLE_1,
		AddressLocation.NAME_TABLE_0,
		AddressLocation.NAME_TABLE_1,
		AddressLocation.NAME_TABLE_2,
		AddressLocation.NAME_TABLE_3,
		AddressLocation.PALETTE
	};
	
	private final PPU _ppu;
	
	public PPUMemory(PPU ppu_) {
		_ppu = ppu_;
	}
	
	private static enum AddressLocation {
		PATTERN_TABLE_0(PATTERN_TABLE_0_LOC, PATTERN_TABLE_SIZE),
		PATTERN_TABLE_1(PATTERN_TABLE_1_LOC, PATTERN_TABLE_SIZE),
		NAME_TABLE_0(NAME_TABLE_0_LOC, NAME_TABLE_SIZE),
		NAME_TABLE_1(NAME_TABLE_1_LOC, NAME_TABLE_SIZE),
		NAME_TABLE_2(NAME_TABLE_2_LOC, NAME_TABLE_SIZE),
		NAME_TABLE_3(NAME_TABLE_3_LOC, NAME_TABLE_SIZE),
		PALETTE(PALETTE_LOC, PALETTE_SIZE);
		
		private final int _startingAddress;
		private final int _size;
		
		AddressLocation(int startingAddress_, int size_) {
			_startingAddress = startingAddress_;
			_size = size_;
		}		
	}

	/** Changing mirroring locations to read unmirrored locations */
	private static short unmirrorAddress(short address_) {
		// Pattern/name table mirroring 
		if(UnsignedShorts.compare(address_, (short) 0x3000) >= 0 &&
			UnsignedShorts.compare(address_, (short) 0x3EFF) < 0) 
		{
			return (short) (address_ - 0x1000);
		} else if(UnsignedShorts.compare(address_, (short) 0x3F20) >= 0 &&
				UnsignedShorts.compare(address_, (short) 0x3F1F) < 0) 
		{
			int offset = (Short.toUnsignedInt(address_) - 0x3F00) % PALETTE_SIZE;
			return (short) (PALETTE_LOC + offset);
		} else {
			return address_;
		}
	}
	
	public byte read(short address) {
		short unmirroredAddress = unmirrorAddress(address);
		
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
