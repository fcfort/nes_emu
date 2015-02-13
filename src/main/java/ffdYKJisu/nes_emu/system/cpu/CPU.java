package ffdYKJisu.nes_emu.system.cpu;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Shorts;
import com.google.common.primitives.UnsignedBytes;
import com.google.common.primitives.UnsignedInteger;

import ffdYKJisu.nes_emu.domain.AddressingMode;
import ffdYKJisu.nes_emu.domain.Opcode;
import ffdYKJisu.nes_emu.domain.StatusBit;
import ffdYKJisu.nes_emu.exceptions.AddressingModeException;
import ffdYKJisu.nes_emu.exceptions.InvalidAddressException;
import ffdYKJisu.nes_emu.system.HexUtils;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.memory.CPUMemory;

/**
 * Controls all functions of the main CPU of the NES.
 * Handles all opcode processing and registers of the cpu.
 * The cart is loaded and restarted and emulation begins in this class.
 * @author fe01106
 */
public class CPU implements ICPU {

	private static Logger logger = LoggerFactory.getLogger(CPU.class);

	/**
	 * Program counter, holds memory location of current position
	 */
	private short PC;
	/** Accumulator */
	private byte A;
	/** Index register X */
	private byte X;
	/** Index register X */
	private byte Y;
	/** Holds the bits of the status byte for the processor */
	private StatusBit P;
	private final CPUMemory memory;
	private int cyclesRun;
	/** Holds Stack object for stack instructions to use */
	private byte[] _stack;
	private byte _stackPointer;

	public CPU(CPUMemory memory_) {		
		logger.info("CPU has been initiated");	
		memory = memory_;
		cyclesRun = 0;
		// Initialize stack	
		_stack = new byte[256];
		// Set up State registers
		initStateRegisters();
		// Load cart into memory
		// Loads cartridge banks to cpu memory banks
		//memory.writeCartToMemory(cart);
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
		A = 0;
		X = 0;
		Y = 0;

		// Stack pointer
		_stackPointer = (byte) 0xFF;
	}

	private void incrementPC(int increment) {
		for ( int i=0; i < increment; i++ ) {
			incrementPC();
		}
	}
	
	private void incrementPC() {
		PC++;
	}
	
	/**
	 * Runs the CPU for one operation regardless of how long it will take
	 * @return returns how many cycles the step took
	 */
	public void runStep() {
		// Read Opcode from PC
		Opcode op = getOpcode();
		
		byte opcodeBytes = op.getOpcodeBytes();
		// Print instruction to logger
		logger.info("Got opcode {} with bytes {} at PC {}", new Object[]{op, opcodeBytes, PC});
		// Print CPU state to log
		// Process instructions for op
		int cyclesBefore = this.cyclesRun; 
		this.processOp(op);
		// Increment PC
		incrementPC(op.getLength());
		
		// Return time taken
	}

	/**
	 * Reads the instruction at that address. Creates a string that will readable
	 * and will look like "$FF00: ($D3 $F0)    ADD $F0".
	 * @param address Location where you want to read an instruction from
	 * @return A string formatted for debugger display.
	 */
	public String instructionToString(short address) {
		StringBuffer sb = new StringBuffer(address + ": ");
		int instructionLength = this.instructionLength(address);

		byte[] bytes = new byte[instructionLength];

		StringBuffer sbBytes = new StringBuffer();

		sbBytes.append("(");
		for (int j = 0; j < instructionLength; j++) {
			byte b = this.memory.read(address);
			sbBytes.append(b);
			bytes[j] = b;
			if (j != instructionLength - 1)
				sbBytes.append(" ");
			address++;
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
			sb.append(bytes[j]);
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
	public int instructionLength(short address) {
		return Opcode.getOpcodeByBytes(memory.read(address)).getLength();
	}

	/**
	 * Reads current PC, reads the opcode there, determines the addressing mode
	 * and returns an address by determining what bytes to read from the parameters
	 * of the instruction. The address returned is either the read/write address
	 * for the instruction
	 * @return Address of where to perform operation
	 */
	private short getAddress() {
		Opcode o = Opcode.getOpcodeByBytes(memory.read(PC));		
		AddressingMode mode = o.getAddressingMode();
		short addr = 0;
		short tempPC = PC;
				
		switch (mode) {
			case IMPLICIT:
				break;
			case ACCUMULATOR:
				break;
			case IMMEDIATE:
				break;
			case ZERO_PAGE:
				addr = memory.read((short)(PC + 1));
				break;
			case ZERO_PAGE_X:
				byte zpAddrX = memory.read((short)(PC + 1));
				addr = (short)(zpAddrX + X);
				break;
			case ZERO_PAGE_Y:
				byte zpAddrY = memory.read((short)(PC + 1));
				addr = (short)(zpAddrY + Y);
				break;
			case RELATIVE:
				byte relOffset = memory.read((short)(PC + 1));
				addr = (short) relOffset;
				break;
			case ABSOLUTE:
				addr = (short) (readShort((short) (PC + 1)));
				break;
			case ABSOLUTE_X:
				addr = (short) (readShortIndirect((short) (PC + 1), X));
				break;
			case ABSOLUTE_Y:
				addr = (short) (readShortIndirect((short) (PC + 1), Y));
				break;
			case INDIRECT:
				addr = readShort((short) (PC + 1));
				addr = readShort(addr);
				break;
			case INDIRECT_X:
				addr = (short) (readShortIndirect((short) (PC + 1), X));
				addr = readShort(addr);
				break;
			case INDIRECT_Y:
				addr = (short) (readShort((short) (PC + 1)));
				addr = readShortIndirect(addr, Y);
				break;
			default:
				logger.error("No matching addressing mode for {}", mode);
				throw new AddressingModeException(mode.toString());
		}
		
		logger.info("Reading opcode {} at PC {} with mode {}. Got final address {}", new Object[]{o, PC, mode, addr});
		return addr;
	}
	
	/** Reads an address for two consecutive bytes and forms
	 * that into an address */
	private short readShort(short address) {
		return Shorts.fromBytes(
			memory.read((short)(address + 1)),
			memory.read((address)
		));
	}
	
	private short readShortIndirect(short address, byte offset) {
		return (short) (readShort(address) + offset);
	}
	
	

	/**
	 * Retrieves the next opcode from memory and returns it as a uByte
	 * @return opCode as a uByte
	 */
	private Opcode getOpcode() {
		byte b = memory.read(PC);		
		Opcode o = Opcode.getOpcodeByBytes(b);
		logger.info("Reading opcode at PC addr {}. Got byte {} and opcode {}", new Object[] {PC, b, o});
		return o;
	}

	
	/**
	 * Main function to emulate operations of the processor to actions
	 * upon the CPU class and others.
	 * @param Opcode value as a uByte
	 * @return Number of cycles taken for the instruction
	 */
	private void processOp(Opcode op) {
		this.getClass().getMethod(op.getCodeName(), Byte.class).invoke(obj, args)
		
		switch (op) {
			// ADCi - Add with Carry immediate
			case ADCi: ADCi(); break;
			// BEQ - Branch if Equal
			case BEQ: BEQ(); break;
			// BNE - Branch if Not Equal
			case BNE: BNE(); break;
			// BPL - Branch if Positive
			case BPL: BPL(); break;
			// CLC - Clear Carry Flag
			case CLC: CLC(); break;
			// CLD - Clear Decimal Mode
			case CLD: CLD(); break;
			// CMPay - Compare A register absolute Y
			case CMPay: CMPay(); break;
			// CMPz - Compare A register zero page
			case CMPz: CMPz(); break;
			// CPXz - Compare X Register
			case CPXz: CPXz(); break;
			// CPYi - Compare Y Register immediate
			case CPYi: CPYi(); break;
			// DEX - Decrement X Register
			case DEX: DEX(); break;
			// INCz - Increment CPUMemory zero page
			case INCz: INCz(); break;
			// INY - Increment Y Register
			case INY: INY(); break;
			// JMPa - Jump
			case JMPa: JMPa(); break;
			// JSR - Jump to Subroutine
			case JSR: JSR(); break;
			// LDA - Load Accumulator
			case LDAa: LDAa(); break;			
			case LDAay: LDAay(); break;
			case LDAi: LDAi(); break;
			case LDAz: LDAz(); break;
			// LDXi - Load X Register
			case LDXi: LDXi(); break;
			// LDYi - Load Y Register immediate
			case LDYi: LDYi(); break;
			// ROLax - Rotate Left absolute X
			case ROLax: ROLax(); break;
			// RTS - Return from Subroutine
			case RTS: RTS(); break;
			// SEI - Set Interrupt Disable
			case SEI: SEI(); break;
			// STA - Store Accumulator
			case STAa: STAa(); break;
			case STAay: STAay(); break;
			case STAiy: STAiy(); break;
			case STAz: STAz(); break;
			// STYz - Store Y Register zero page
			case STYz: STYz(); break;
			// TAX - Transfer Accumulator to X	
			case TAX: TAX(); break;
			// TAY - Transfer Accumulator to Y
			case TAY: TAY(); break;
			// TXS - Transfer X to Stack Pointer
			case TXS: TXS(); break;
			// TYA - Transfer Y to Accumulator
			case TYA: TYA(); break;
			default:
				logger.info("Opcode {} not yet implemented", op);
				throw new UnsupportedOperationException("Opcode (" + op + ") not yet implemented");
				//PC.increment(op.getLength());
		}
	}
	
	public void ADC(byte val_) {
		byte initialA = A;
		int temp = Byte.toUnsignedInt(A) + Byte.toUnsignedInt(val_) + (P.isSetCarry() ? 1 : 0); 
		P.setCarry(temp > 0xFF);
		A = (byte) temp;
		setZero();
		setNegative();
		setOverflow(initialA, A);
		logger.info("Added {} to {} and got {} with status {}", new Object[] {
				HexUtils.toHex(val_),
				HexUtils.toHex(initialA),
				HexUtils.toHex(A),
				P
		});
	}
	
	public void AND(byte val_) {
		A = (byte) (A & val_);
		setZero();
		setNegative();		
	}
	
	public void ASL() {
		P.setCarry((A & 0x80) != 0);			
		A = (byte) (A << 1);		
		setZero();
		setNegative();
	}
	
	public void BIT(byte val_) {
		setZero((byte) (A & val_));
		setNegative(val_); // set if value is negative
		P.setOverflow((val_& 1 << 6) != 0); // Set overflow to value of bit 6
	}
	
	private void setOverflow(byte initial_, byte final_) {
		P.setOverflow((final_ & (byte)0x80) != (initial_ & 0x80));
	}
	
	private void setZero(byte val_) {
		P.setZero((val_ & 0xFF) == 0);
	}
	
	private void setZero() {
		setZero(A);
	}
	
	private void setNegative(byte val_) {
		P.setNegative(val_ < 0);	
	}
	
	private void setNegative() {
		setNegative(A);
	}
	
	
	

		private void BEQ() {
			uShort curAddr = PC;
			incrementPC();
			uByte relOffset = memory.read(PC);
			incrementPC();
			if (CPU.this.P.isSetZero()) {
				uShort newAddr = PC.increment(relOffset.toSigned());
				CPU.this.setPC(newAddr);
				if (this.pageJumped(curAddr, newAddr)){}
					//return 4;
				//return 3;
			}
			//return 2;
		}

		private void BNE() {
			short pageBeforeJump = PC.getUpper().get();
			incrementPC();
			uByte relOffset = memory.read(PC);
			incrementPC();
			uShort newPC = new uShort(PC.get() + relOffset.toSigned());
			logger.info("BNE " + relOffset + " @" + newPC);
			if (!P.isSetZero()) {
				CPU.this.setPC(PC.increment(relOffset.toSigned()));
				if (PC.getUpper().get() != pageBeforeJump) {
					this.cyclesRun += Opcode.BNE.getCycles(true, true);
				} else {
					this.cyclesRun += Opcode.BNE.getCycles(true, false);
				}			
			} else {
				this.cyclesRun += Opcode.BNE.getCycles(false, false);
			}
		}

		private void BPL() {
			short pageBeforeJump = PC.getUpper().get();
			incrementPC();
			uByte relOffset = memory.read(PC);
			incrementPC();
			uShort newPC = new uShort(PC.get() + relOffset.toSigned());

			logger.info("BPL " + relOffset + " @" + newPC);

			if (!P.isSetNegative()) {
				CPU.this.setPC(PC.increment(relOffset.toSigned()));
				if (PC.getUpper().get() != pageBeforeJump){
					this.cyclesRun += Opcode.BPL.getCycles(true, true);
				} else {
					this.cyclesRun += Opcode.BPL.getCycles(true, false);
				}
			} else {
				this.cyclesRun += Opcode.BPL.getCycles(false, false);
			}
		}

		/* ******************* 
		 * Loads
		 ******************* */		

		// TODO: Write tests for LDX
		public void LDX(byte val_) {
			X = val_;
			setZero(X);
			setNegative(X);
		}
		
		/* ******************* 
		 * Sets
		 ******************* */		

		public void SEC() {
			P.setCarry();
			this.cyclesRun += Opcode.SEC.getCycles();
		}
		
		public void SED() {
			P.setDecimal();
			this.cyclesRun += Opcode.SED.getCycles();
		}
		
		private void SEI() {
			P.setInterruptDisable();
			this.cyclesRun += Opcode.SEI.getCycles();
		}
		
		/* ******************* 
		 * Clears 
		 ******************* */
		
		public void CLC() {
			P.clearCarry();
			this.cyclesRun += Opcode.CLC.getCycles();
		}

		public void CLD() {
			P.clearDecimal();
			this.cyclesRun += Opcode.CLD.getCycles();
		}
		
		public void CLI() {
			P.clearInterruptDisable();
			this.cyclesRun += Opcode.CLI.getCycles();
		}

		public void CLV() {
			P.clearOverflow();
			this.cyclesRun += Opcode.CLV.getCycles();
		}
		
		/* ******************* 
		 * Compares 
		 ******************* */

		private void CMPz() {
			incrementPC();
			uByte value = memory.read(memory.read(PC));
			this.compare(A, value);
			incrementPC();
			this.cyclesRun += Opcode.CMPz.getCycles();
		}

		private void CMPay() {
			incrementPC();
			uShort addr = readBytesAsAddress(PC);
			CPU.this.setPC(PC.increment(2));
			uShort newAddr = this.toAbsoluteYAddress(addr);
			this.compare(CPU.this.getA(), CPU.this.memory.read(newAddr));
			if (this.pageJumped(addr, newAddr)) {
				this.cyclesRun += Opcode.CMPay.getCycles(false, true);				
			} else {
				this.cyclesRun += Opcode.CMPay.getCycles();
			}
		}

		private void CPXz() {
			incrementPC();
			uByte tempByte = memory.read(PC); // get zero page offset
			logger.info( "CPXz " + tempByte);
			tempByte = memory.read(tempByte); // read from zero page
			compare(X, tempByte);
			incrementPC();
			this.cyclesRun += Opcode.CPXz.getCycles();
		}

		private void CPYi() {
			incrementPC();
			// Check zero
			uByte tempByte = new uByte(memory.read(PC));
			logger.info("CPYi " + tempByte);
			compare(Y, tempByte);
			incrementPC();
			this.cyclesRun += Opcode.CPYi.getCycles();
		}

		private void DEX() {
			incrementPC();
			X = X.decrement();
			P.setZero(X.get() == 0);
			P.setNegative(X.isNegative());
			this.cyclesRun += Opcode.DEX.getCycles();
		}

		private void INCz() {
			incrementPC();
			uByte zpAddress = new uByte(memory.read(PC));
			logger.info("INCz " + zpAddress);
			uByte zpValue = memory.read(zpAddress);
			zpValue = zpValue.increment();
			try {
				CPU.this.memory.write(zpAddress, zpValue);
			} catch (InvalidAddressException ex) {
				logger.warn(ex + "Error in INCz address:" + zpAddress + " value:" + zpValue);
			}
			P.setNegative(zpValue.isNegative());
			P.setZero(zpValue.get() == 0);
			incrementPC();
			this.cyclesRun += Opcode.INCz.getCycles();
		}

		private void INY() {
			incrementPC();
			CPU.this.setY(Y.increment());
			P.setNegative(Y.isNegative());
			P.setZero(Y.get() == 0);
			logger.info( "INY");
			this.cyclesRun += Opcode.INY.getCycles();
		}

		private void JMPa() {
			incrementPC();
			uByte L = new uByte(memory.read(PC));
			incrementPC();
			uByte H = new uByte(memory.read(PC));
			PC = new uShort(H, L);
			logger.info( "JMPa " + H + L);
			this.cyclesRun += Opcode.JMPa.getCycles();
		}

		private void JSR() {
			incrementPC();
			byte upperByte = (byte) PC.getUpper().get();
			_stack[_stackPointer--] = upperByte;
			byte lowerByte = (byte) PC.getLower().get();
			_stack[_stackPointer--] = lowerByte;	
			uShort subAddr = this.readBytesAsAddress(PC);
			CPU.this.setPC(subAddr);
			this.cyclesRun += Opcode.JSR.getCycles();
		}
		
		private void LDAi() {
			logger.info("Beginning operation LDAi");
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
			this.cyclesRun += Opcode.LDAi.getCycles();
		}

		private void LDAa() {
			incrementPC();
			uByte L = new uByte(memory.read(PC));
			incrementPC();
			uByte H = new uByte(memory.read(PC));
			logger.info( "LDAa " + H + L);
			A = memory.read(H, L);
			P.setZero(A.get() == 0);
			P.setNegative(A.isNegative());
			incrementPC();
			this.cyclesRun += Opcode.LDAa.getCycles();
		}

		private void LDAay() {
			incrementPC();
			uShort addr = readBytesAsAddress(PC);
			CPU.this.setPC(PC.increment(2));
			uShort newAddr = this.toAbsoluteYAddress(addr);
			A = memory.read(newAddr);
			P.setZero(A.get() == 0);
			P.setNegative(A.isNegative());
			//CPU.this.setPC(PC.increment());
			if (this.pageJumped(addr, newAddr)) {
				this.cyclesRun += Opcode.LDAay.getCycles(false, true);
			} else { 
				this.cyclesRun += Opcode.LDAay.getCycles();
			}
				
		}

		private void LDAz() {
			incrementPC();
			A = memory.read(memory.read(PC));
			P.setZero(A.get() == 0);
			P.setNegative(A.isNegative());
			incrementPC();
			this.cyclesRun += Opcode.LDAz.getCycles();
		}

		private void LDXi() {
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
			this.cyclesRun += Opcode.LDXi.getCycles();
		}

		private void LDYi() {
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
			this.cyclesRun += Opcode.LDYi.getCycles();
		}

		private void ROLax() {
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
			this.cyclesRun += Opcode.ROLax.getCycles();
		}

		private void RTS() {			
			uByte L = new uByte(_stack[_stackPointer++]);
			uByte H = new uByte(_stack[_stackPointer++]);
			uShort addr = new uShort(H, L);
			CPU.this.setPC(addr);
			incrementPC();
			this.cyclesRun += Opcode.RTS.getCycles();
		}
		
		private void STAa() {
			incrementPC();
			uByte L = new uByte(memory.read(PC));
			incrementPC();
			uByte H = new uByte(memory.read(PC));
			logger.info("STAa " + H + L);
			// A.set(memory.read(H,L));
			try {
				memory.write(H, L, A);
			} catch (InvalidAddressException e) {
				System.out.println("HL addr" + H + L + " PC " + PC);
			}
			incrementPC();
			this.cyclesRun += Opcode.STAa.getCycles();
		}

		private void STAay() {
			incrementPC();
			uByte L = new uByte(memory.read(PC));
			incrementPC();
			uByte H = new uByte(memory.read(PC));
			uShort temp = new uShort(H, L);
			temp.increment(Y.get());
			try {
				memory.write(temp, A);
			} catch (InvalidAddressException ex) {
				logger.warn(ex.getMessage());
			}
			incrementPC();
			logger.info("STAay " + H + L);
			this.cyclesRun += Opcode.STAay.getCycles();
		}

		private void STAiy() {
			incrementPC();
			uByte offset = new uByte(memory.read(PC));
			uByte L = new uByte(memory.read(offset));
			offset.increment();
			uByte H = new uByte(memory.read(offset));
			uShort temp = new uShort(H, L);
			temp.increment(Y.get());
			try {
				memory.write(temp, A);
			} catch (InvalidAddressException ex) {
				logger.warn(
					ex + "PC: " + PC);
			}
			logger.info("STAiy " + offset);
			incrementPC();
			this.cyclesRun += Opcode.STAiy.getCycles();
		}

		private void STAz() {
			incrementPC();
			try {
				memory.write(memory.read(memory.read(PC)), CPU.this.getA());
			} catch (InvalidAddressException ex) {
				logger.warn(ex + "PC: " + PC);
			}
			incrementPC();
			this.cyclesRun += Opcode.STAz.getCycles();
		}

		private void STYz() {
			incrementPC();
			uByte zpAddr = CPU.this.memory.read(CPU.this.getPC());
			incrementPC();
			try {
				CPU.this.memory.write(zpAddr, CPU.this.getY());
			} catch (InvalidAddressException ex) {
				logger.warn(ex + " STYz at ZP addr:" + zpAddr + "Y:" + CPU.this.getY());
			}
			this.cyclesRun += Opcode.STYz.getCycles();
		}

		private void TAX() {
			incrementPC();
			X = A;
			P.setNegative(X.isNegative());
			P.setZero(X.get() == 0);
			this.cyclesRun += Opcode.TAX.getCycles();
		}

		private void TAY() {
			incrementPC();
			Y = A;
			P.setNegative(Y.isNegative());
			P.setZero(Y.get() == 0);
			logger.info("TAY");
			this.cyclesRun += Opcode.TAY.getCycles();
		}

		private void TXS() {
			incrementPC();
			_stackPointer = (byte) X.get();
			logger.info("TXS");
			this.cyclesRun += Opcode.TXS.getCycles();
		}

		private void TYA() {
			incrementPC();
			A = Y;
			P.setNegative(Y.isNegative());
			P.setZero(Y.get() == 0);
			this.cyclesRun += Opcode.TYA.getCycles();
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
		
		void compare(byte A, byte B) {
			if (A < B) {
				P.setNegative(A - B < 0);
				P.clearZero();
				P.clearCarry();
			} else if (A == B) {
				P.clearNegative();
				P.setZero();
				P.setCarry();
			} else {
				P.setNegative(A - B < 0);
				P.clearZero();
				P.setCarry();
			}
		}

		public void reset() { 
			short resetAddrL = (short)0xfffc;
			short resetAddrH = (short)0xfffd;
			//uByte jumpLocL = new uByte(memory.read(resetAddrL));
			byte jumpLocL = memory.read(resetAddrL);
			//uByte jumpLocH = new uByte(memory.read(resetAddrH));
			byte jumpLocH = memory.read(resetAddrH);
					
			short address = Shorts.fromBytes(jumpLocH, jumpLocL);
			logger.info( "Reset, jumping to {}", HexUtils.toHex(address));		
			PC = address;
		}
		
		public short getPC() { return PC; }
		public byte getSP() { return _stackPointer; }
		public byte getA() { return A; }
		public byte getX() { return X; }
		public byte getY() { return Y; }
		public boolean getCarryFlag() { return P.isSetCarry(); }
		public boolean getZeroFlag() { return P.isSetZero(); }
		public boolean getInterruptDisable() { return P.isSetInterruptDisable(); }
		public boolean getDecimalMode() { return P.isSetDecimal(); }
		public boolean getBreakCommand() { return P.isSetBreak(); }
		public boolean getOverflowFlag() { return P.isSetOverflow(); }
		public boolean getNegativeFlag() { return P.isSetNegative(); }
	}

