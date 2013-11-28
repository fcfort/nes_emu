 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ffdYKJisu.nes_emu.system.cpu;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ffdYKJisu.nes_emu.domain.AddressingMode;
import ffdYKJisu.nes_emu.domain.Opcode;
import ffdYKJisu.nes_emu.domain.StatusBit;
import ffdYKJisu.nes_emu.domain.uByte;
import ffdYKJisu.nes_emu.domain.uShort;
import ffdYKJisu.nes_emu.exceptions.AddressException;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.memory.CPUMemory;

/**
 * Controls all functions of the main CPU of the NES.
 * Handles all opcode processing and registers of the cpu.
 * The cart is loaded and restarted and emulation begins in this class.
 * @author fe01106
 */
public class CPU {

	private static Logger logger = LoggerFactory.getLogger(CPU.class);

	/**
	 * Program counter, holds memory location of current position
	 */
	private uShort PC;
	/** Accumulator */
	private uByte A;
	/** Index register X */
	private uByte X;
	/** Index register X */
	private uByte Y;
	/** Holds the bits of the status byte for the processor */
	private StatusBit P;
	private final CPUMemory memory;
	private boolean cpuIsRunning;
	/** Holds private Instruction class */
	Instruction i;
	/** Holds Stack object for stack instructions to use */
	Stack S;	
	private final NES nes;
	
	public CPU(NES nes) {		
		logger.info("CPU has been reinitiated");
		this.nes = nes;
		// Initialize instruction class
		i = new Instruction();
		// Initialize stack
		S = new Stack();
		// Set up State registers
		initStateRegisters();
		// Load cart into memory
		memory = new CPUMemory(nes);
		// Loads cartridge banks to cpu memory banks
		//memory.writeCartToMemory(cart);
		// Start the cpu
		cpuIsRunning = false;
	}

	// Getters/Setters for CPU registers
	public StatusBit getP() {
		return P;
	}

	public void setP(StatusBit P) {
		this.P = P;
	}

	public uShort getPC() {
		return PC;
	}

	public void setPC(uShort PC) {
		this.PC = PC;
	}

	public uByte getA() {
		return A;
	}

	public void setA(uByte A) {
		this.A = A;
	}

	public uByte getX() {
		return X;
	}

	public void setX(uByte X) {
		this.X = X;
	}

	public uByte getY() {
		return Y;
	}

	public void setY(uByte Y) {
		this.Y = Y;
	}

	public uByte getSP() {
		return this.S.get();
	}

	public void setSP(uByte SP) {
		this.S.set(SP);
	}

	public void resetInterrupt() {				
		uShort resetAddrL = new uShort((char) 0xfffc);
		uShort resetAddrH = new uShort((char) 0xfffd);
		//uByte jumpLocL = new uByte(memory.read(resetAddrL));
		uByte jumpLocL = memory.read(resetAddrL);
		//uByte jumpLocH = new uByte(memory.read(resetAddrH));
		uByte jumpLocH = memory.read(resetAddrH);
		uShort address = new uShort(jumpLocH, jumpLocL);
		logger.info( "Reset, jumping to " + address);
		
		PC = address;
	}

	private void initStateRegisters() {
		P = new StatusBit();
		// Processor status
		P.clearCarry();
		P.clearZero();
		P.clearDecimal();
		P.clearInterruptDisable();
		P.clearDecimal();
		P.clearOverflow();
		P.clearNegative();

		// A,X,Y
		A = new uByte(0);
		X = new uByte(0);
		Y = new uByte(0);

		// Stack pointer
		S.set(new uByte(0xff));
	}

	private void incrementPC() {
		PC = PC.increment();
	}

	/**
	 * Runs until cpuIsRunning is false
	 */
	void emulate() {
		while (cpuIsRunning) {
			runStep();
		}
	}

	/**
	 * Holds the main loop of the emulator. Retrieves the next opcode from
	 * memory and passes it to processOp().
	 */
	public void emulateFor(long cyclesToRun) {
		if (!this.cpuIsRunning) {
			logger.info( "CPU has been started to run for " + cyclesToRun + " cycles");
			this.cpuIsRunning = true;
		}
		long cycleCount = 0;
		while (this.cpuIsRunning) {
			cycleCount += this.runStep();			
			if (cycleCount > cyclesToRun) {
				this.cpuIsRunning = false;
				logger.info( "CPU has been stopped after " + cycleCount + " cycles");
			}
		}
	}

	/**
	 * Runs the CPU for one operation regardless of how long it will take
	 * @return returns how many cycles the step took
	 */
	public int runStep() {
		// Read Opcode from PC
		Opcode op = this.getOpcode();
		uByte opcodeBytes = op.getOpcodeBytes();
		// Print instruction to logger
		logger.info("Got opcode {} with bytes {} at PC {}", new Object[]{op, opcodeBytes, PC});
		// Print CPU state to log
		// Process instructions for op
		int cyclesTaken = this.processOp( opcodeBytes);
		// Increment PC
		PC = PC.increment(op.getLength());
		
		// Return time taken
		return cyclesTaken;
	}

	/**
	 * Reads the instruction at that address. Creates a string that will readable
	 * and will look like "$FF00: ($D3 $F0)    ADD $F0".
	 * @param address Location where you want to read an instruction from
	 * @return A string formatted for debugger display.
	 */
	public String instructionToString(uShort address) {
		StringBuffer sb = new StringBuffer(address + ": ");
		int instructionLength = this.instructionLength(address);

		uByte[] bytes = new uByte[instructionLength];

		StringBuffer sbBytes = new StringBuffer();

		sbBytes.append("(");
		for (int j = 0; j < instructionLength; j++) {
			uByte b = this.memory.read(address);
			sbBytes.append(b);
			bytes[j] = b;
			if (j != instructionLength - 1)
				sbBytes.append(" ");
			address = address.increment();
		}
		sbBytes.append(")");
		// Pad string to maximum length of bytes possible for one instruction
		// That is 3 bytes which is ten characters: "(.. .. ..)"
		int maxLength = 13;
		while (sbBytes.length() < maxLength) {
			sbBytes.append(" ");
		}
		sb.append(sbBytes);
		String opcodeName = Opcode.getOpcodeByBytes(bytes[0]).getCodeName();
		sb.append(" " + opcodeName + " ");

		for (int j = 1; j < instructionLength; j++) {
			sb.append(bytes[j].toString());
			if (j != instructionLength - 1)
				sb.append(" ");
		}
		return sb.toString();
	}

	/**
	 * Returns the number of bytes that the current instruction occupies. This
	 * includes the actual instruction opcode itself. I.e. CLD returns 1 even 
	 * though it has no parameters
	 * @param address Address at which the instruction is at
	 * @return Number of bytes until next instruction
	 */
	public int instructionLength(uShort address) {
		return Opcode.getOpcodeByBytes(memory.read(address)).getLength();
	}

	/**
	 * Reads current PC, reads the opcode there, determines the addressing mode
	 * and returns an address by determining what bytes to read from the parameters
	 * of the instruction. The address returned is either the read/write address
	 * for the instruction
	 * @return Address of where to perform operation
	 */
	public uShort getAddress() {
		AddressingMode mode =
			Opcode.getOpcodeByBytes(memory.read(PC)).getAddressingMode();
		uShort addr = null;
		uShort tempPC = PC;
		switch (mode) {
			case IMPLICIT:
			case ACCUMULATOR:
			case IMMEDIATE:
				return null;
			case ZERO_PAGE:
				return new uShort(memory.read(tempPC.increment()));
			case ZERO_PAGE_X:
				uByte zpAddr = memory.read(tempPC.increment());
				return new uShort ( zpAddr.increment(X.get()) );
			case ZERO_PAGE_Y:	
				return new uShort ( 
					memory.read(tempPC.increment())
						.increment(Y.get()) 
				);
			case RELATIVE:
				uByte relOffset = memory.read(tempPC.increment());
				return tempPC.increment(2 + relOffset.get());
			case ABSOLUTE:
				/*
				uByte L = memory.read(tempPC.increment());
				uByte H = memory.read(tempPC.increment(2));
				System.err.println(
					tempPC + "," + tempPC.increment() + "," + tempPC.increment(2));
				System.err.println("Absolute " + L+H+" @" + PC);
				*/
				return new uShort(
					memory.read(tempPC.increment(2)),
					memory.read(tempPC.increment())
				);
			case ABSOLUTE_X:
				return new uShort(
					memory.read(tempPC).increment(2),
					memory.read(tempPC).increment()
				).increment(X.get());
			case ABSOLUTE_Y:
				return new uShort(
					memory.read(tempPC).increment(2),
					memory.read(tempPC).increment()
				).increment(Y.get());	
			case INDIRECT:
				addr = new uShort(
					memory.read(tempPC).increment(2),
					memory.read(tempPC).increment()
				);
				return new uShort(
					memory.read(addr.increment()),
					memory.read(addr)
					);
			case INDIRECT_X:
				addr = new uShort(
					memory.read(tempPC).increment(2),
					memory.read(tempPC).increment()
				).increment(X.get());
				return new uShort(
					memory.read(addr.increment()),
					memory.read(addr)
					);			
			case INDIRECT_Y:
				addr = new uShort(
					memory.read(tempPC).increment(2),
					memory.read(tempPC).increment()
				);				
				return new uShort(
					memory.read(addr.increment()),
					memory.read(addr)
					).increment(Y.get());							
			default:
				return null;
		}

	}

	/**
	 * Retrieves the next opcode from memory and returns it as a uByte
	 * @return opCode as a uByte
	 */
	private Opcode getOpcode() {
		uByte b = memory.read(PC);
		Opcode o = Opcode.getOpcodeByBytes(b);
		return o;
	}
	
	
	/**
	 * Main function to emulate operations of the processor to actions
	 * upon the CPU class and others.
	 * @param Opcode value as a uByte
	 * @return Number of cycles taken for the instruction
	 */
	private int processOp(uByte opCode) {
		switch ((int) opCode.get()) {
			// ADCi - Add with Carry immediate
			case 0x69:
				return i.ADCi();
			// BEQ - Branch if Equal
			case 0xF0:
				return i.BEQ();
			// BNE - Branch if Not Equal
			case 0xD0:
				return i.BNE();
			// BPL - Branch if Positive
			case 0x10:
				return i.BPL();
			// CLC - Clear Carry Flag
			case 0x18:
				return i.CLC();
			// CLD - Clear Decimal Mode
			case 0xD8:
				return i.CLD();
			// CMPay - Compare A register absolute Y
			case 0xD9:
				return i.CMPay();
			// CMPz - Compare A register zero page
			case 0xC5:
				return i.CMPz();
			// CPXz - Compare X Register
			case 0xE4:
				return i.CPXz();
			// CPYi - Compare Y Register immediate
			case 0xC0:
				return i.CPYi();
			// DEX - Decrement X Register
			case 0xCA:
				return i.DEX();
			// INCz - Increment CPUMemory zero page
			case 0xE6:
				return i.INCz();
			// INY - Increment Y Register
			case 0xC8:
				return i.INY();
			// JMPa - Jump
			case 0x4C:
				return i.JMPa();
			// JSR - Jump to Subroutine
			case 0x20:
				return i.JSR();
			// LDA - Load Accumulator
			case 0xA9:
			case 0xAD:
			case 0xB9:
			case 0xA5:
				return i.LDA();
			// LDXi - Load X Register
			case 0xA2:
				return i.LDXi();
			// LDYi - Load Y Register immediate
			case 0xA0:
				return i.LDYi();
			// ROLax - Rotate Left absolute X
			case 0x3E:
				return i.ROLax();
			// RTS - Return from Subroutine
			case 0x60:
				return i.RTS();
			// SEI - Set Interrupt Disable
			case 0x78:
				return i.SEI();
			// STA - Store Accumulator
			case 0x8D:
			case 0x99:
			case 0x91:
			case 0x85:
				return i.STA();
			// STYz - Store Y Register zero page
			case 0x84:
				return i.STYz();
			// TAX - Transfer Accumulator to X	
			case 0xAA:
				return i.TAX();
			// TAY - Transfer Accumulator to Y
			case 0xA8:
				return i.TAY();
			// TXS - Transfer X to Stack Pointer
			case 0x9A:
				return i.TXS();
			// TYA - Transfer Y to Accumulator
			case 0x98:
				return i.TYA();
			default:
				logger.info(
					"Opcode (" + opCode + ") not yet implemented");
				throw new UnsupportedOperationException(
					"Opcode (" + opCode + ") not yet implemented");
		}
	}

	private class Instruction {

		int ADCi() {
			incrementPC();
			uByte val = memory.read(PC);
			incrementPC();
			int temp = A.get() + val.get() + (P.isSetCarry() ? 1 : 0);
			P.setCarry(temp > 0xFF);
			// I don't actually understand this overflow thing myself.
			// Just copied it from 6502.txt
			if (((A.get() ^ val.get()) & 0x80) != 0)
				if (((A.get() ^ temp) & 0x80) != 0)
					P.clearOverflow();
				else
					P.setOverflow();
			A = new uByte(temp);
			P.setZero(A.get() == 0);
			P.setNegative(A.isNegative());
			return 2;
		}

		int BEQ() {
			uShort curAddr = PC;
			incrementPC();
			uByte relOffset = memory.read(PC);
			incrementPC();
			if (CPU.this.P.isSetZero()) {
				uShort newAddr = PC.increment(relOffset.toSigned());
				CPU.this.setPC(newAddr);
				if (this.pageJumped(curAddr, newAddr))
					return 4;
				return 3;
			}
			return 2;
		}

		int BNE() {
			short pageBeforeJump = PC.getUpper().get();
			incrementPC();
			uByte relOffset = memory.read(PC);
			incrementPC();
			uShort newPC = new uShort(PC.get() + relOffset.toSigned());
			logger.info("BNE " + relOffset + " @" + newPC);
			if (!P.isSetZero()) {
				CPU.this.setPC(PC.increment(relOffset.toSigned()));
				if (PC.getUpper().get() != pageBeforeJump)
					return 4;
				return 3;
			}
			return 2;
		}

		int BPL() {
			short pageBeforeJump = PC.getUpper().get();
			incrementPC();
			uByte relOffset = memory.read(PC);
			incrementPC();
			uShort newPC = new uShort(PC.get() + relOffset.toSigned());

			logger.info("BPL " + relOffset + " @" + newPC);

			if (!P.isSetNegative()) {
				CPU.this.setPC(PC.increment(relOffset.toSigned()));
				if (PC.getUpper().get() != pageBeforeJump)
					return 4;
				return 3;
			}
			return 2;
		}

		int CLC() {
			P.clearZero();
			incrementPC();
			return 2;
		}

		int CLD() {
			logger.info("CLD");
			P.clearDecimal();
			return 2;
		}

		int CMPz() {
			incrementPC();
			uByte value = memory.read(memory.read(PC));
			this.compare(A, value);
			incrementPC();
			return 3;
		}

		int CMPay() {
			incrementPC();
			uShort addr = readBytesAsAddress(PC);
			CPU.this.setPC(PC.increment(2));
			uShort newAddr = this.toAbsoluteYAddress(addr);
			this.compare(CPU.this.getA(), CPU.this.memory.read(newAddr));
			if (this.pageJumped(addr, newAddr))
				return 5;
			return 4;
		}

		int CPXz() {
			incrementPC();
			uByte tempByte = memory.read(PC); // get zero page offset
			logger.info( "CPXz " + tempByte);
			tempByte = memory.read(tempByte); // read from zero page
			compare(X, tempByte);
			incrementPC();
			return 3;
		}

		int CPYi() {
			incrementPC();
			// Check zero
			uByte tempByte = new uByte(memory.read(PC));
			logger.info("CPYi " + tempByte);
			compare(Y, tempByte);
			incrementPC();
			return 2;
		}

		int DEX() {
			incrementPC();
			X = X.decrement();
			P.setZero(X.get() == 0);
			P.setNegative(X.isNegative());
			return 2;
		}

		int INCz() {
			incrementPC();
			uByte zpAddress = new uByte(memory.read(PC));
			logger.info("INCz " + zpAddress);
			uByte zpValue = memory.read(zpAddress);
			zpValue = zpValue.increment();
			try {
				CPU.this.memory.write(zpAddress, zpValue);
			} catch (AddressException ex) {
				logger.warn(ex + "Error in INCz address:" + zpAddress + " value:" + zpValue);
			}
			P.setNegative(zpValue.isNegative());
			P.setZero(zpValue.get() == 0);
			incrementPC();
			return 5;
		}

		int INY() {
			incrementPC();
			CPU.this.setY(Y.increment());
			P.setNegative(Y.isNegative());
			P.setZero(Y.get() == 0);
			logger.info( "INY");
			return 2;
		}

		int JMPa() {
			incrementPC();
			uByte L = new uByte(memory.read(PC));
			incrementPC();
			uByte H = new uByte(memory.read(PC));
			PC = new uShort(H, L);
			logger.info( "JMPa " + H + L);
			return 3;
		}

		int JSR() {
			incrementPC();
			uShort subAddr = this.readBytesAsAddress(PC);
			incrementPC();
			S.push(CPU.this.PC.getUpper());
			S.push(CPU.this.PC.getLower());
			CPU.this.setPC(subAddr);
			return 6;
		}

		int LDA() {
			uShort addr = getAddress();
			A = memory.read(addr);
			P.setZero(A.get() == 0);
			P.setNegative(A.isNegative());
			//return opCodeData.getCycles( getOpcode() , false, false );
			return getOpcode().getCycles();
		}
		
		int LDAi() {
			incrementPC();
			A = memory.read(PC);
			logger.info("LDAi " + A);
			if (A.get() == 0)
				P.setZero();
			else
				P.clearZero();
			if ((A.get() >> 7) == 1)
				P.setNegative();
			else
				P.clearNegative();
			incrementPC();
			return 2;
		}

		int LDAa() {
			incrementPC();
			uByte L = new uByte(memory.read(PC));
			incrementPC();
			uByte H = new uByte(memory.read(PC));
			logger.info( "LDAa " + H + L);
			A = memory.read(H, L);
			P.setZero(A.get() == 0);
			P.setNegative(A.isNegative());
			incrementPC();
			return 4;
		}

		int LDAay() {
			incrementPC();
			uShort addr = readBytesAsAddress(PC);
			CPU.this.setPC(PC.increment(2));
			uShort newAddr = this.toAbsoluteYAddress(addr);
			A = memory.read(newAddr);
			P.setZero(A.get() == 0);
			P.setNegative(A.isNegative());
			//CPU.this.setPC(PC.increment());
			if (this.pageJumped(addr, newAddr))
				return 5;
			else
				return 4;
		}

		int LDAz() {
			incrementPC();
			A = memory.read(memory.read(PC));
			P.setZero(A.get() == 0);
			P.setNegative(A.isNegative());
			incrementPC();
			return 3;
		}

		int LDXi() {
			incrementPC();
			X = memory.read(PC);
			logger.info( "LDXi " + X);
			if (X.isNegative())
				P.setNegative();
			else
				P.clearNegative();
			if (X.get() == 0)
				P.setZero();
			else
				P.clearZero();
			incrementPC();
			return 2;
		}

		int LDYi() {
			incrementPC();
			Y = memory.read(PC);
			logger.info("LDYi " + Y);
			if (Y.isNegative())
				P.setNegative();
			else
				P.clearNegative();
			if (Y.get() == 0)
				P.setZero();
			else
				P.clearZero();
			incrementPC();
			return 2;
		}

		int ROLax() {
			incrementPC();
			uByte L = new uByte(memory.read(PC));
			incrementPC();
			uByte H = new uByte(memory.read(PC));
			uShort temp = new uShort(H, L);
			temp.increment(X.get());
			uByte rotate = memory.read(temp);
			// If bit 7, we need to carry that bit to status register
			if (rotate.isNegative())
				P.setCarry();
			else
				P.clearCarry();
			rotate.rotateLeft(P.isSetCarry());
			logger.info("ROLax " + H + L);
			return 7;
		}

		int RTS() {
			uByte L = S.pull();
			uByte H = S.pull();
			uShort addr = new uShort(H, L);
			CPU.this.setPC(addr);
			incrementPC();
			return 6;
		}

		int SEI() {
			logger.info( "SEI");
			P.setInterruptDisable();
			return 2;
		}

		int STA() {
			uShort addr = getAddress();
			try {
				memory.write( addr, A );
			} catch (AddressException ex) {
				logger.warn(
					ex + " addr" + addr + " PC " + PC );
				System.err.println( " addr" + addr + " PC " + PC );
			}
			return getOpcode().getCycles();
		}
		
		int STAa() {
			incrementPC();
			uByte L = new uByte(memory.read(PC));
			incrementPC();
			uByte H = new uByte(memory.read(PC));
			logger.info("STAa " + H + L);
			// A.set(memory.read(H,L));
			try {
				memory.write(H, L, A);
			} catch (AddressException e) {
				System.out.println("HL addr" + H + L + " PC " + PC);
			}
			incrementPC();
			return 4;
		}

		int STAay() {
			incrementPC();
			uByte L = new uByte(memory.read(PC));
			incrementPC();
			uByte H = new uByte(memory.read(PC));
			uShort temp = new uShort(H, L);
			temp.increment(Y.get());
			try {
				memory.write(temp, A);
			} catch (AddressException ex) {
				logger.warn(ex.getMessage());
			}
			incrementPC();
			logger.info("STAay " + H + L);
			return 5;
		}

		int STAiy() {
			incrementPC();
			uByte offset = new uByte(memory.read(PC));
			uByte L = new uByte(memory.read(offset));
			offset.increment();
			uByte H = new uByte(memory.read(offset));
			uShort temp = new uShort(H, L);
			temp.increment(Y.get());
			try {
				memory.write(temp, A);
			} catch (AddressException ex) {
				logger.warn(
					ex + "PC: " + PC);
			}
			logger.info("STAiy " + offset);
			incrementPC();
			return 6;
		}

		int STAz() {
			incrementPC();
			try {
				memory.write(memory.read(memory.read(PC)), CPU.this.getA());
			} catch (AddressException ex) {
				logger.warn(ex + "PC: " + PC);
			}
			incrementPC();
			return 3;
		}

		int STYz() {
			incrementPC();
			uByte zpAddr = CPU.this.memory.read(CPU.this.getPC());
			incrementPC();
			try {
				CPU.this.memory.write(zpAddr, CPU.this.getY());
			} catch (AddressException ex) {
				logger.warn(ex + " STYz at ZP addr:" + zpAddr + "Y:" + CPU.this.getY());
			}
			return 3;
		}

		int TAX() {
			incrementPC();
			X = A;
			P.setNegative(X.isNegative());
			P.setZero(X.get() == 0);
			return 2;
		}

		int TAY() {
			incrementPC();
			Y = A;
			P.setNegative(Y.isNegative());
			P.setZero(Y.get() == 0);
			logger.info("TAY");
			return 2;
		}

		int TXS() {
			incrementPC();
			CPU.this.S.set(X);
			logger.info("TXS");
			return 2;
		}

		int TYA() {
			incrementPC();
			A = Y;
			P.setNegative(Y.isNegative());
			P.setZero(Y.get() == 0);
			return 2;
		}
// ------------------------
// Helper functions
// ------------------------
		/**
		 * Accepts two addresses and returns true if those two addresses are
		 * on different pages. I.e. return false if their upper bytes are not 
		 * equal.
		 * @param startAddress Starting address, typically the PC of the instruction
		 * @param endAddress End address, typically the location that is to be
		 * written to. 
		 * @return Returns true if the two addresses lie on different pages,
		 * false otherwise.
		 */
		boolean pageJumped(uShort startAddress, uShort endAddress) {
			return !startAddress.getUpper().equals(endAddress.getUpper());
		}

		/**
		 * accepts two bytes and returns an address that is calculated based
		 * on absolute Y addressing. Combines upper and lower address bytes
		 * and reads that location in memory and ten increments that address
		 * by the value of the Y register.
		 * @param lowerAddress
		 * @param upperAddress
		 * @return Address to read or write from
		 */
		uShort toAbsoluteYAddress(uByte upperAddress, uByte lowerAddress) {
			uShort temp = new uShort(upperAddress, lowerAddress);
			return toAbsoluteYAddress(temp);
		}

		uShort toAbsoluteYAddress(uShort address) {
			return address.increment(CPU.this.Y.get());
		}

		uShort toAbsoluteXAddress(uByte upperAddress, uByte lowerAddress) {
			uShort temp = new uShort(upperAddress, lowerAddress);
			temp = temp.increment(CPU.this.X.get());
			return temp;
		}

		/**
		 * Reads next two bytes in memory and combines them as if they were an
		 * address and returns those bytes interpreted as an address. I.e. LDA
		 * $00 $FF would return the address uShort $FF00.
		 * @param address Where to read the bytes from, will typically be the PC
		 * @return Next two bytes in memory as an address
		 */
		uShort readBytesAsAddress(uShort address) {
			uByte L = memory.read(address);
			uByte H = memory.read(address.increment());
			return new uShort(H, L);
		// or as 1-liner: return new uShort(memory.read(address),memory.read(address.increment()));
		}

		/**
		 * Compares two bytes, used by all comparison operations.
		 * Simulates A - B and changes status flags based on results
		 * @param A minuend (usually a register)
		 * @param B subtrahend (usually from memory)
		 */
		void compare(uByte A, uByte B) {
			byte s = (byte) (A.toSigned() - B.toSigned());
			if (A.get() < B.get()) {
				P.setNegative(s < 0);
				P.clearZero();
				P.clearCarry();
			} else if (A.get() == B.get()) {
				P.clearNegative();
				P.setZero();
				P.setCarry();
			} else {
				P.setNegative(s < 0);
				P.clearZero();
				P.setCarry();
			}
		}
	}

	private class Stack {

		/**
		 * Holds the current offset into the 1-page (stack) for the next 
		 * available
		 * empty spot for pushing to the stack
		 */
		private uByte stackPointer = new uByte(0xFF);
		private final uShort stackOffset = new uShort(0x100);

		uByte get() {
			return stackPointer;
		}

		void set(uByte sp) {
			stackPointer = sp;
		}

		uByte pull() {
			stackPointer = new uByte(stackPointer.increment());
			uShort addr = new uShort(stackPointer.get() + stackOffset.get());
			uByte val = CPU.this.memory.read(addr);
			//System.out.println("Pulling " + val + " from " + addr);
			return val;
		}

		void push(uByte val) {
			uShort addr = new uShort(stackPointer.get() + stackOffset.get());
			try {
				CPU.this.memory.write(addr, val);
			//System.out.println("Pushing " + val + " to " + addr);
			} catch (AddressException ex) {
				logger.warn(ex + "Error pushing " + val + " to " + addr);
			}
			stackPointer = new uByte(stackPointer.decrement());
		}
	}

	public CPUMemory getMemory() {
		return this.memory;
	}
}

