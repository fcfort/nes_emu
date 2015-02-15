package ffdYKJisu.nes_emu.system.cpu;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Shorts;

import ffdYKJisu.nes_emu.domain.AddressingMode;
import ffdYKJisu.nes_emu.domain.Opcode;
import ffdYKJisu.nes_emu.domain.StatusBit;
import ffdYKJisu.nes_emu.exceptions.AddressingModeException;
import ffdYKJisu.nes_emu.system.HexUtils;
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
	private byte _stackPointer;

	private static short RESET_VECTOR_LOW = (short) 0xFFFC;
	private static short RESET_VECTOR_HIGH = (short) 0xFFFD;
	
	private static short INTERRUPT_VECTOR_LOW = (short) 0xFFFE;
	private static short INTERRUPT_VECTOR_HIGH = (short) 0xFFFF;
	
	public CPU(CPUMemory memory_) {		
		logger.info("CPU has been initiated");	
		memory = memory_;
		cyclesRun = 0;
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
		/* 
		Result
			ResultLocation
				ResultLocationType: MEMORY, REGISTER_X, REGISTER_Y
				ResultLocationValue: short (MEMORY)
			ResultValue
		
		createResult(AddressingMode m_, short addr_)
		createResult(AddressingMode m_, byte val_)  
		
		Result r = OPCODE(byte val_) or OPCODE (short addr_)
		
		*/
		byte opcodeBytes = op.getOpcodeBytes();
		short address = getAddress(op.getAddressingMode());
		
		// Print instruction to logger
		logger.info("Got opcode {} with bytes {} at PC {}", new Object[]{op, opcodeBytes, PC});
		
		if(hasOperand(op.getAddressingMode())) {
			byte operand = getOperand(op.getAddressingMode(), address);
			doOperation(op, operand);
		} else {
			doOperation(op);
		}
		
		// Print CPU state to log
		// Process instructions for op
		int cyclesBefore = this.cyclesRun;
		// this.processOp(op, operand);
		// Increment PC
		incrementPC(op.getLength());
		
		// Return time taken
	}

	private void doOperation(Opcode op_) {		
		try {
			Method opCodeImplementation = getClass().getDeclaredMethod(op_.getCodeName());
			opCodeImplementation.invoke(this);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void doOperation(Opcode op_, byte operand_) {		
		try {	
			logger.info("Looking for method with name {} for operation {} with operands {}", 
					new Object[] {op_.getCodeName(), op_, operand_});
			Method opCodeImplementation = getClass().getDeclaredMethod(op_.getCodeName(), byte.class);
			logger.info("Found method {} for op {}, calling with {} operands of length {}", 
					new Object[] {opCodeImplementation, op_, operand_, 1});							
			opCodeImplementation.invoke(this, operand_);
		} catch (NoSuchMethodException e) {
			logger.error("{}", e);
			throw new UnsupportedOperationException();
		} catch (SecurityException e) {
			throw new UnsupportedOperationException();
		} catch (IllegalAccessException e) {
			throw new UnsupportedOperationException();
		} catch (IllegalArgumentException e) {
			logger.error("{}", e);
			throw new UnsupportedOperationException();
		} catch (InvocationTargetException e) {
			throw new UnsupportedOperationException();
		}
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
	
	

	private boolean hasOperand(AddressingMode mode_) {
		return mode_ != AddressingMode.IMPLICIT;
	}
	
	private byte getOperand(AddressingMode mode_, short address_) {
		switch (mode_) {
		case IMPLICIT:
			throw new UnsupportedOperationException();
		case ACCUMULATOR:
			return A;
		case IMMEDIATE:
		case ZERO_PAGE:			
		case ZERO_PAGE_X:
		case ZERO_PAGE_Y:
		case RELATIVE:
		case ABSOLUTE:
		case ABSOLUTE_X:
		case ABSOLUTE_Y:
		case INDIRECT:
		case INDIRECT_X:
		case INDIRECT_Y:
			return memory.read(address_);
		default:
			logger.error("No matching addressing mode for {}", mode_);
			throw new UnsupportedOperationException();
		}
	}
	
	/**
	 * Reads current PC, reads the opcode there, determines the addressing mode
	 * and returns an address by determining what bytes to read from the parameters
	 * of the instruction. The address returned is either the read/write address
	 * for the instruction
	 * @return Address of where to perform operation
	 */	
	private short getAddress(AddressingMode mode_) {
		short addr = 0;
				
		switch (mode_) {
			case IMPLICIT:
				break;
			case ACCUMULATOR:
				break;
			case IMMEDIATE:
				addr = (short) (PC + 1);
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
				logger.error("No matching addressing mode for {}", mode_);
				throw new AddressingModeException(mode_.toString());
		}
		
		logger.info("At PC {} with mode {}. Got final address {}", new Object[]{PC, mode_, addr});
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
	 * Retrieves the next opcode from memory and returns it 
	 * @return opCode
	 */
	private Opcode getOpcode() {
		byte b = memory.read(PC);		
		Opcode o = Opcode.getOpcodeByBytes(b);
		logger.info("Reading opcode at PC addr {}. Got byte {} and opcode {}", new Object[] {PC, b, o});
		return o;
	}
	
	/* ******************* 
	 * Logic
	 ******************* */
	
	public void AND(byte val_) {
		A = (byte) (A & val_);
		setZero(A);
		setNegative(A);		
	}
	
	public byte ASL(byte val_) {
		P.setCarry((val_ & 0x80) != 0);
		return shift(val_, 1, false);		
	}
	
	public void BIT(byte val_) {
		setZero((byte) (A & val_));
		setNegative(val_); // set if value is negative
		P.setOverflow((val_& 1 << 6) != 0); // Set overflow to value of bit 6
	}
	
	public void EOR(byte val_) {
		A ^= val_;
		setZero(A);
		setNegative(A);
	}
	
	public void ORA(byte val_) {
		A |= val_;
		setZero(A);
		setNegative(A);
	}
	
	public byte LSR(byte val_) {
		P.setCarry((val_ & 0x01) != 0);
		return shift(val_, -1, false);
	}
	
	public byte ROL(byte val_) {
		boolean carry = (val_ & 0x80) != 0;
		P.setCarry(carry);
		return shift(val_, 1, carry);
	}
	
	public byte ROR(byte val_) {
		boolean carry = (val_ & 0x01) != 0;
		P.setCarry(carry);
		return shift(val_, -1, carry);
	}
	
	/** positive shiftAmount <<, 
	 *  negative shiftAmount >>
	 */ 	
	private byte shift(byte val_, int direction_, boolean carry_) {
		if(direction_ > 0) {
			val_ <<= 1;
			val_ = (byte) (carry_ ? val_ | 1 : val_ & ~1);
		} else {
			val_ >>= 1;
			val_ = (byte) (carry_ ? val_ | (1 << 8): val_ & ~(1 << 8));
		}
		setZero(val_);
		setNegative(val_);
		return val_;
	}
	
	/* ******************* 
	 * Arithmetic
	 ******************* */
	
	public void ADC(byte val_) {
		byte initialA = A;
		int temp = Byte.toUnsignedInt(A) + Byte.toUnsignedInt(val_) + (P.isSetCarry() ? 1 : 0); 
		P.setCarry(temp > 0xFF);
		A = (byte) temp;
		setZero(A);
		setNegative(A);
		setOverflow(initialA, A);
		logger.info("Added {} to {} and got {} with status {}", new Object[] {
				HexUtils.toHex(val_),
				HexUtils.toHex(initialA),
				HexUtils.toHex(A),
				P
		});
	}
	
	public void DEX() {
		X--;
	}
	
	public void DEY() {
		Y--;
	}
	
	public void INX() {
		X++;
	}
	
	public void INY() {
		Y++;
	}

	private void setOverflow(byte initial_, byte final_) {
		P.setOverflow((final_ & (byte)0x80) != (initial_ & 0x80));
	}
	
	private void setZero(byte val_) {
		P.setZero((val_ & 0xFF) == 0);
	}
	
	private void setNegative(byte val_) {
		P.setNegative(val_ < 0);	
	}	
	
	/* ******************* 
	 * Branches
	 ******************* */
	
	public void BCS(byte val_) { branch(P.isSetCarry(), val_); }
	
	public void BCC(byte val_) { branch(!P.isSetCarry(), val_); }	
		
	public void BEQ(byte val_) { branch(P.isSetZero(), val_); }
	
	public void BNE(byte val_) { branch(!P.isSetZero(), val_); }
	
	public void BMI(byte val_) { branch(P.isSetNegative(), val_); }
	
	public void BPL(byte val_) { branch(!P.isSetNegative(), val_); }
	
	public void BVS(byte val_) { branch(P.isSetOverflow(), val_); }
	
	public void BVC(byte val_) { branch(!P.isSetOverflow(), val_); }
	
	private void branch(boolean status_, byte offset_) {
		if(status_) {
			PC += offset_;
		}
	}
	
	public void JMP(short address_) {
		PC = address_;
	}
	
	public void JSR(short address_) {
		PC--;
		pushPC();
		PC = address_;
	}
	
	/* ******************* 
	 * Loads
	 ******************* */
	
	public void LDA(byte val_) {
		A = val_;
		setNegative(A);
		setZero(A);
	}
		
	public void LDX(byte val_) {
		X = val_;
		setNegative(X);
		setZero(X);
	}
	
	public void LDY(byte val_) {
		Y = val_;
		setNegative(Y);
		setZero(Y);
	}

	/* ******************* 
	 * Clears 
	 ******************* */
	
	public void CLC() { P.clearCarry(); }

	public void CLD() { P.clearDecimal(); }
	
	public void CLI() { P.clearInterruptDisable(); }

	public void CLV() { P.clearOverflow(); }
	
	/* ******************* 
	 * Compares 
	 ******************* */
	
	public void CMP(byte val_) { compare(A, val_); }
	
	public void CPX(byte val_) { compare(X, val_); }
	
	public void CPY(byte val_) { compare(Y, val_); }
	
	/**
	 * Compares two bytes, used by all comparison operations.
	 * Simulates A - B and changes status flags based on results
	 * @param A minuend (usually a register)
	 * @param B subtrahend (usually from memory)
	 */		
	private void compare(byte a_, byte b_) {
		int result = Byte.toUnsignedInt(a_) - Byte.toUnsignedInt(b_);
		P.setCarry(result >= 0);
		P.setZero(result == 0);
		P.setNegative(result < 0);
	}
	
	/* ******************* 
	 * Stack
	 ******************* */
	
	public void PHA() {
		push(A);
	}
	
	public void PHP() {
		push(P.asByte());
	}
	
	public void PLA() {
		A = pop();
	}
	
	public void PLP() {		
		P.fromByte(pop());
	}
		
	private void push(byte val_) {
		memory.push(_stackPointer, val_);
		_stackPointer--;	
	}
	
	private byte pop() {
		_stackPointer++;
		return memory.pop(_stackPointer);		
	}
	
	private void pushPC() {
		byte[] bytesPC = Shorts.toByteArray(PC);
		push(bytesPC[0]); // Upper
		push(bytesPC[1]); // Lower
	}
	
	
	/* ******************* 
	 * Other
	 ******************* */
	
	public void BRK() {		
		pushPC();
		P.setBreak();
		push(P.asByte());
		P.setInterruptDisable();
		setPCFromVector(INTERRUPT_VECTOR_LOW, INTERRUPT_VECTOR_HIGH);		
	}

	public void NOP() {}
	
	
	/*

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
	
	private void RTS() {			
		uByte L = new uByte(_stack[_stackPointer++]);
		uByte H = new uByte(_stack[_stackPointer++]);
		uShort addr = new uShort(H, L);
		CPU.this.setPC(addr);
		incrementPC();
		this.cyclesRun += Opcode.RTS.getCycles();
	}
	
	*/
	
	/* ******************* 
	 * Sets
	 ******************* */		

	public void SEC() { P.setCarry(); }
	
	public void SED() { P.setDecimal(); }
	
	public void SEI() { P.setInterruptDisable(); }	
	
	/* ******************* 
	 * Stores
	 ******************* */
	
	public byte STA() { return A; }
	
	public byte STX() { return X; }
	
	public byte STY() { return Y; }
	
	/* ******************* 
	 * Transfers
	 ******************* */

	public void TAX() {
		X = A;
		setNegative(X);
		setZero(X);
	}

	public void TAY() {
		Y = A;
		setNegative(Y);
		setZero(Y);
	}

	public void TSX() {
		X = _stackPointer;
		setNegative(X);
		setZero(X);
	}
	
	public void TXS() {
		_stackPointer = X;
	}

	public void TYA() {
		A = Y;
		setNegative(A);
		setZero(A);
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
		boolean pageJumped(short startAddress, short endAddress) {
			// return !startAddress.getUpper().equals(endAddress.getUpper());
			return false;
		}

		/**
		 * Reads next two bytes in memory and combines them as if they were an
		 * address and returns those bytes interpreted as an address. I.e. LDA
		 * $00 $FF would return the address uShort $FF00.
		 * @param address Where to read the bytes from, will typically be the PC
		 * @return Next two bytes in memory as an address
		 */
		/*
		uShort readBytesAsAddress(uShort address) {
			uByte L = memory.read(address);
			uByte H = memory.read(address.increment());
			return new uShort(H, L);
		// or as 1-liner: return new uShort(memory.read(address),memory.read(address.increment()));
		}
		*/

		public void reset() {
			setPCFromVector(RESET_VECTOR_LOW, RESET_VECTOR_HIGH);
		}
		
		private void setPCFromVector(short vectorLow_, short vectorHigh_) {
			short address = Shorts.fromBytes(memory.read(vectorHigh_), memory.read(vectorLow_));
			logger.info( "Jumping to {} from vector {} {}", new Object[] {
				HexUtils.toHex(address),
				HexUtils.toHex(vectorHigh_),
				HexUtils.toHex(vectorLow_),
			});		
			PC = address;
		}
		
		public short getPC() { return PC; }		
		public byte getSP() { return _stackPointer; }
		public byte getSR() { return P.asByte(); }
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


