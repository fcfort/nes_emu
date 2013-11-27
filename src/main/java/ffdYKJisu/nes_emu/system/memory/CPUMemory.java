/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ffdYKJisu.nes_emu.system.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ffdYKJisu.nes_emu.domain.uByte;
import ffdYKJisu.nes_emu.domain.uShort;
import ffdYKJisu.nes_emu.exceptions.addressException;
import ffdYKJisu.nes_emu.exceptions.bankNotFoundException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.NES;

/**
 *
 * @author Administrator
 */
public class CPUMemory implements Memory {

	private static Logger logger = LoggerFactory.getLogger(CPUMemory.class);
	
	private final int PRGROMlen = 2 * Cartridge.Bank.PRG16.length;
	private final int SRAMlen = 0x2000;
	private final int RAMlen = 0x2000;
	private NES nes;
	private uByte[] PRGROM;
	private uByte[] SRAM;
	private uByte[] EROM;
	private uByte[] PPUio;
	private static final int PPUiolen = 8;
	private uByte[] RAM;

	public CPUMemory(NES nes) {
		this.nes = nes;
		RAM = new uByte[RAMlen];
		PRGROM = new uByte[PRGROMlen];
		SRAM = new uByte[SRAMlen];
		PPUio = new uByte[PPUiolen];
	}
	
	public void writeCartToMemory(Cartridge cart) {		
		try {
			Byte[] bank = cart.get16PRGBank(0);
			// Copy to lower bank
			for (int i = 0; i < bank.length; i++) {
				PRGROM[i] = new uByte(bank[i]);
			//System.out.print(PRGROM[i] + " ");
			}
			// Copy to upper bank
			for (int i = 0; i < bank.length; i++) {
				PRGROM[i + 0x4000] = new uByte(bank[i]);
			}

		} catch (bankNotFoundException ex) {
			logger.warn("Bank not found");
		}
	}

	public uByte read(uShort address) {
		logger.info("Read at address {}", address);
		
		char addr = address.get();
		uByte temp;
		// PRGROM
		if (addr >= 0x8000 && addr < 0x10000)
			/*
			System.out.println("Accessing memory location of PRGROM: " + 
			address.toString() + " a " + 
			String.format("0x%X",(int)(addr - 0x8000)));
			 */
			temp = PRGROM[(int) addr - 0x8000];
		// RAM
		else if (addr < 0x2000)
			temp = RAM[addr]; // PPU/VRAM I/O Registers
		else if (addr >= 0x2000 && addr < 0x4000)
			temp = PPUio[addr % 8];
		else {
			System.out.println("Unrecognized address " + address.toString());
			return new uByte(0);
		}
		// return a copy of what was in memory
		return new uByte(temp);
	}

	public uByte read(uByte addrH, uByte addrL) {
		uShort address = new uShort(addrH, addrL);
		return read(address);
	}

	/**
	 * Used to access zero page address using a uByte as the address
	 * @param zeroPageAddress A byte referencing a zero page address
	 * (first page).
	 * @return byte from that address on the zero page.
	 */
	public uByte read(uByte zeroPageAddress) {
		uShort address =
			new uShort(uShort.toAddress(zeroPageAddress));
		return read(address);
	}

 	public void write(uShort address, uByte val) throws addressException {
		char addr = address.get();
		if (addr > 0xFFFF)
			throw new addressException("Over 0xFFFF");
		if (addr >= 0x8000)
			throw new addressException("In PRGROM");
		if (addr >= 0x0000 && addr < 0x2000) {
			//Logger.getLogger(CPUMemory.class.getName()).log(Level.INFO, 
			//	"Writing " + val + " to " + address);
			logger.info("Writing " + val + " to " + address);
			//System.out.println("Writing " + val + " to " + address);
			RAMwrite(addr, val);
		}
	}

	public void write(uByte zeroPageAddress, uByte val) throws addressException {
		write(new uShort(zeroPageAddress), val);
	}

	public void write(uByte addrH, uByte addrL, uByte val) throws addressException {
		write(new uShort(addrH, addrL), val);
	}

	private void RAMwrite(char address, uByte val) {
		char zpAddress = (char) (address & 0x07ff);
		RAM[zpAddress] = val;
		RAM[zpAddress + 0x0800] = val;
		RAM[zpAddress + 0x1000] = val;
		RAM[zpAddress + 0x1800] = val;
	}
}
