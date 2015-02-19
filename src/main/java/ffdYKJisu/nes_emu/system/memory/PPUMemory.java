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

	private static final short PATTERN_TABLE_0_LOC = 0x0000;
	private static final short PATTERN_TABLE_1_LOC = 0x1000;
	
	private static final short NAME_TABLE_0_LOC = 0x2000;
	private static final short NAME_TABLE_1_LOC = 0x2400;
	private static final short NAME_TABLE_2_LOC = 0x2800;
	private static final short NAME_TABLE_3_LOC = 0x2C00;
	
	private static final short PALETTE_LOC = 0x3F00;
	
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
		
		private final short _startingAddress;
		private final int _size;
		
		AddressLocation(short startingAddress_, int size_) {
			_startingAddress = startingAddress_;
			_size = size_;
		}
		
		public short getStartingAddress() { return _startingAddress; }
		public short getEndingAddress() { return (short) (_startingAddress + _size); }
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
	
	private static AddressLocation getAddressLocation(short address_) {
		for(AddressLocation al : MEMORY_ORDER) {
			if(UnsignedShorts.compare(al.getStartingAddress(), address_) >= 0 && 
					UnsignedShorts.compare(al.getEndingAddress(), address_) < 0)
			{
				return al;
			}
		}
		
		throw new UnsupportedOperationException();
	}
	
	public byte read(short address_) {
		short unmirroredAddress = unmirrorAddress(address_);
		AddressLocation al = getAddressLocation(unmirroredAddress);
		int index = address_ - al.getStartingAddress();
		
		switch(al) {
			case PATTERN_TABLE_0:
				return PatternTable0[index];
			case PATTERN_TABLE_1:
				return PatternTable1[index];
			case NAME_TABLE_0:
				return NameTable0[index];
			case NAME_TABLE_1:
				return NameTable1[index];
			case NAME_TABLE_2:
				return NameTable2[index];
			case NAME_TABLE_3:
				return NameTable3[index];
			case PALETTE:
				return PaletteTable[index];
			default:
				throw new UnsupportedOperationException();
		}
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
