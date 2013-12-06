package ffdYKJisu.nes_emu.system.memory;

import ffdYKJisu.nes_emu.domain.uByte;
import ffdYKJisu.nes_emu.domain.uShort;

/**
 * This takes in the inputs necessary for calculating a final address in memory
 * using the different addressing modes available. The necessary inputs are:
 * - The opcode parameters, passed into the calculator as an address
 * - The CPU memory, for performing the reads
 * - PC, for relative addressing
 * - X & Y registers, for absolute and zero-paged indexed addressing
 * 
 * This interface must only read memory and never write to memory.
 * The result is a byte of memory.
 * @author Frank
 *
 */
public interface IAddressingModeRetriever {

	/*
	 * http://en.wikibooks.org/wiki/6502_Assembly#Memory_Addressing_Modes
	 * 
	 * 1.1 Accumulator: A
	 * 1.2 Implied: i
	 * 1.3 Immediate: #
	 * 1.4 Absolute: a
	 * 1.5 Zero Page: zp
	 * 1.6 Relative: r
	 * 1.7 Absolute Indexed with X: a,x
	 * 1.8 Absolute Indexed with Y: a,y
	 * 1.9 Zero Page Indexed with X: zp,x
	 * 1.10 Zero Page Indexed with Y: zp,y
	 * 1.11 Zero Page Indexed Indirect: (zp,x)
	 * 1.12 Zero Page Indirect Indexed with Y: (zp),y
	 */
	
	public uByte readImmediate(uShort address);
	
	public uByte readAbsolute(uShort address);
	
	public uByte readZeroPageAddress(uShort address);
	
	public uByte readRelative
}
