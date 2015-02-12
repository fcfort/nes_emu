/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ffdYKJisu.nes_emu.system.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Shorts;

import ffdYKJisu.nes_emu.exceptions.BankNotFoundException;
import ffdYKJisu.nes_emu.exceptions.InvalidAddressException;
import ffdYKJisu.nes_emu.system.Cartridge;
import static ffdYKJisu.nes_emu.system.HexUtils.*;

/**
 * New version of memory based on shorts and bytes instead of encapsulated
 * data types uShort and uByte;
 */
public class CPUMemory implements IMemory {

	private static Logger logger = LoggerFactory.getLogger(CPUMemory.class);
	
	private static final int SRAM_LEN = 0x2000;
	private static final int RAM_LEN = 0x2000;
	private static final int PPU_IO_LEN = 8;
	private static final int PPU_IO_OFFSET = 0x4000;	
	private static final int BANK_LEN = 0x4000; // 16kB
	private static final int PRGROM_LEN = BANK_LEN * 2;
	private static final int PRGROM_OFFSET = 0x8000;
	private byte[] PRGROM;
	private byte[] SRAM;
	private byte[] EROM;
	private byte[] PPUio;
	private byte[] RAM;

	public CPUMemory() {
		RAM = new byte[RAM_LEN];
		PRGROM = new byte[PRGROM_LEN];
		SRAM = new byte[SRAM_LEN];
		PPUio = new byte[PPU_IO_LEN];
	}
	
	public void writeCartToMemory(Cartridge cart) {	
		logger.info("Loading cart into memory");
		try {
			byte[] bank = cart.get16PRGBank(0);
			// Copy to lower bank
			for (int i = 0; i < BANK_LEN; i++) {
				PRGROM[i] = bank[i];
				//System.out.print(PRGROM[i] + " ");
			}
			// Copy to upper bank
			for (int i = 0; i < BANK_LEN; i++) {
				PRGROM[i + BANK_LEN] = bank[i];
			}

		} catch (BankNotFoundException ex) {
			logger.warn("Bank not found");
		}
	}
	
	private enum AddressLocation {
		RAM,
		PRGROM,
		SRAM,
		PPUio
	}
	
	private static AddressLocation getAddressLocation(short address) {
		if(compareAddress(address,(short)0x2000) < 0) {
			return AddressLocation.RAM; 
		} else if (compareAddress(address,(short)0x2000) >= 0 && compareAddress(address,(short)0x4000) < 0) {
			return AddressLocation.PPUio;
		} else if (compareAddress(address,(short)0x8000) >= 0) {
			return AddressLocation.PRGROM;
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public byte read(short address) {				
		byte val = 0;
		switch(getAddressLocation(address)) {
		case PPUio:			
			val = PPUio[address - (short)PPU_IO_OFFSET];
			logger.info("PPU/VRAM I/O read value {} at address {}", toHex(val), toHex(address));
			return val;
		case PRGROM:
			int romAddress = address - (short)PRGROM_OFFSET;			
			val = PRGROM[romAddress];
			logger.info("PGR-ROM read value {} at address {} with array index {}", new Object[] { toHex(val), toHex(address), toHex((short)romAddress)});
			return val;
		case RAM:
			logger.info("RAM I/O read at address {}", toHex(address));
			val = RAM[address]; // PPU/VRAM I/O Registers
			logger.info("RAM I/O read value {} at address {}", toHex(val), toHex(address));
			return val;
		case SRAM:
		default:
			throw new UnsupportedOperationException("Unrecognized address " + toHex(address));
		}		
	}

	public byte read(byte addrH, byte addrL) {
		short address = (short) (addrH << 8 + addrL);
		return read(address);
	}

	/**
	 * Used to access zero page address using a byte as the address
	 * @param zeroPageAddress A byte referencing a zero page address
	 * (first page).
	 * @return byte from that address on the zero page.
	 */
	public byte read(byte zeroPageAddress) {
		return read((short)zeroPageAddress);
	}

 	public void write(short address, byte val) throws InvalidAddressException {
 		logger.info("Write at address {} of {}", toHex(address), toHex(val)); 		 	
 		switch(getAddressLocation(address)) {
		case PPUio:
			throw new UnsupportedOperationException();
		case PRGROM:
			throw new InvalidAddressException("In PRGROM");
		case RAM:
			logger.info("Writing " + val + " to " + address);
			//System.out.println("Writing " + val + " to " + address);
			RAMwrite(address, val);
			break;
		case SRAM:			
		default:
			throw new UnsupportedOperationException();
 		} 	
	}

	public void write(byte zeroPageAddress, byte val) throws InvalidAddressException {
		write((short)zeroPageAddress, val);
	}

	public void write(byte addrH, byte addrL, byte val) throws InvalidAddressException {
		write((short)(addrH << 8 + addrL), val);
	}

	private void RAMwrite(short address, byte val) {			
		short zpAddress = (short) (address & 0x07ff);		
		int unsignedAddress = zpAddress & 0xffff;
		RAM[unsignedAddress] = val;
		RAM[unsignedAddress + 0x0800] = val;
		RAM[unsignedAddress + 0x1000] = val;
		RAM[unsignedAddress + 0x1800] = val;
	}

	private static int compareAddress(short address1_, short address2_) {
		return Integer.compare(Short.toUnsignedInt(address1_), Short.toUnsignedInt(address2_));
		// return (int)(short)(address1_ - address2_);
	}
}
