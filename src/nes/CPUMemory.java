/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nes;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author Administrator
 */
class CPUMemory implements Memory {

	private static Logger cpuMemoryLogger = Logger.getLogger("nes.CPUMemory");
	
	void initLogger() {
		String logName = "cpuMemoryLog.txt";
		try {

			ConsoleHandler c = new ConsoleHandler();
			c.setFormatter(new NesFormatter());
			c.setLevel(Level.ALL);
			cpuMemoryLogger.addHandler(c);
			
			FileHandler fh = new FileHandler(logName);
			fh.setFormatter(new NesFormatter());
			//cpuLogger.addHandler(fh);
		} catch (IOException ex) {
			Logger.getLogger(CPUMemory.class.getName()).log(Level.SEVERE, null, ex);
		} catch (SecurityException ex) {
			Logger.getLogger(CPUMemory.class.getName()).log(Level.SEVERE, null, ex);
		}
		cpuMemoryLogger.setLevel(Level.ALL);
	}
	
	
	private final int PRGROMlen = 2 * Cartridge.Bank.PRG16.length;
	private final int SRAMlen = 0x2000;
	private final int RAMlen = 0x2000;
	private Cartridge cart;
	private uByte[] PRGROM;
	private uByte[] SRAM;
	private uByte[] EROM;
	private uByte[] PPUio;
	private static final int PPUiolen = 8;
	private uByte[] RAM;

	CPUMemory(Cartridge cart) {
		this.setCart(cart);
		this.zeroAllRam();
		this.writeCartToMemory();
	}

	CPUMemory() {
		this.zeroAllRam();
	}

	private void zeroAllRam() {
		// SRAM
		SRAM = new uByte[SRAMlen];
		for (int i = 0; i < SRAMlen; i++) {
			SRAM[i] = new uByte(0);
		}
		// RAM
		RAM = new uByte[RAMlen];
		for (int i = 0; i < RAMlen; i++) {
			RAM[i] = new uByte(0);
		}
		// PPU I/O Registers
		PPUio = new uByte[PPUiolen];
		for (int i = 0; i < PPUiolen; i++) {
			PPUio[i] = new uByte(0);
		}
	}

	public void setCart(Cartridge cart) {
		this.cart = cart;
	}

	void writeCartToMemory() {
		PRGROM = new uByte[PRGROMlen];
		try {
			Byte[] bank = this.cart.get16PRGBank(0);
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
			Logger.getLogger(CPUMemory.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public uByte read(uShort address) {
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
			cpuMemoryLogger.log( Level.INFO, 
				"Writing " + val + " to " + address);
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
