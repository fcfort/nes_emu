package ffdYKJisu.nes_emu.system.cpu;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ffdYKJisu.nes_emu.domain.AddressingMode;
import ffdYKJisu.nes_emu.domain.Opcode;
import ffdYKJisu.nes_emu.domain.StatusBit;
import ffdYKJisu.nes_emu.domain.uByte;
import ffdYKJisu.nes_emu.domain.uShort;
import ffdYKJisu.nes_emu.exceptions.InvalidAddressException;
import ffdYKJisu.nes_emu.exceptions.AddressingModeException;
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
	private int cyclesRun;
	/** Holds Stack object for stack instructions to use */
	Stack S;	
	private final NES nes;
	
	public CPU(NES nes) {		
		logger.info("CPU has been initiated");	
		this.nes = nes;
		cyclesRun = 0;
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
/*
	public uByte getSP() {
		return this.S.get();
	}

	public void setSP(uByte SP) {
		this.S.set(SP);
	}
	*/
	public CPUMemory getMemory() {
		return memory;
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

	private void incrementPC(int increment) {
		for ( int i=0; i < increment; i++ ) {
			incrementPC();
		}
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
		Opcode op = getOpcode();
		
		uByte opcodeBytes = op.getOpcodeBytes();
		// Print instruction to logger
		logger.info("Got opcode {} with bytes {} at PC {}", new Object[]{op, opcodeBytes, PC});
		// Print CPU state to log
		// Process instructions for op
		int cyclesBefore = this.cyclesRun; 
		this.processOp(op);
		// Increment PC
		incrementPC(op.getLength());
		
		// Return time taken
		return this.cyclesRun - cyclesBefore;
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
		Opcode o = Opcode.getOpcodeByBytes(memory.read(PC));		
		AddressingMode mode = o.getAddressingMode();
		uShort addr = null;
		uShort tempPC = PC;
				
		switch (mode) {
			case IMPLICIT:
				break;
			case ACCUMULATOR:
				break;
			case IMMEDIATE:
				break;
			case ZERO_PAGE:
				addr = new uShort(memory.read(tempPC.increment()));
				break;
			case ZERO_PAGE_X:
				uByte zpAddr = memory.read(tempPC.increment());
				addr = new uShort ( zpAddr.increment(X.get()) );
				break;
			case ZERO_PAGE_Y:	
				addr = new uShort ( 
					memory.read(tempPC.increment())
						.increment(Y.get()) 
				);
				break;
			case RELATIVE:
				uByte relOffset = memory.read(tempPC.increment());
				addr = tempPC.increment(2 + relOffset.get());
				break;
			case ABSOLUTE:
				/*
				uByte L = memory.read(tempPC.increment());
				uByte H = memory.read(tempPC.increment(2));
				System.err.println(
					tempPC + "," + tempPC.increment() + "," + tempPC.increment(2));
				System.err.println("Absolute " + L+H+" @" + PC);
				*/
				addr = new uShort(
					memory.read(tempPC.increment(2)),
					memory.read(tempPC.increment())
				);
				break;
			case ABSOLUTE_X:
				addr = new uShort(
					memory.read(tempPC).increment(2),
					memory.read(tempPC).increment()
				).increment(X.get());
				break;
			case ABSOLUTE_Y:
				addr = new uShort(
					memory.read(tempPC).increment(2),
					memory.read(tempPC).increment()
				).increment(Y.get());
				break;
			case INDIRECT:
				addr = new uShort(
					memory.read(tempPC).increment(2),
					memory.read(tempPC).increment()
				);
				addr = new uShort(
					memory.read(addr.increment()),
					memory.read(addr)
					);
				break;
			case INDIRECT_X:
				addr = new uShort(
					memory.read(tempPC).increment(2),
					memory.read(tempPC).increment()
				).increment(X.get());
				addr = new uShort(
					memory.read(addr.increment()),
					memory.read(addr)
					);
				break;
			case INDIRECT_Y:
				addr = new uShort(
					memory.read(tempPC).increment(2),
					memory.read(tempPC).increment()
				);				
				addr = new uShort(
					memory.read(addr.increment()),
					memory.read(addr)
					).increment(Y.get());
				break;
			default:
				logger.error("No matching addressing mode for {}", mode);
				throw new AddressingModeException(mode.toString());
		}
		
		logger.info("Reading opcode {} at PC {} with mode {}. Got final address {}", new Object[]{o, PC, mode, addr});
		return addr;
	}

	/**
	 * Retrieves the next opcode from memory and returns it as a uByte
	 * @return opCode as a uByte
	 */
	private Opcode getOpcode() {
		uByte b = memory.read(PC);		
		Opcode o = Opcode.getOpcodeByBytes(b);
		logger.info("Reading opcode at PC addr {}. Got byte {} and opcode {}", new Object[] {PC, b, o});
		return o;
	}
	
	/**
	 * Get a reference to a byte of memory by addressing mode
	 * 
	 **/
	 private uByte read(uShort address, AddressingMode mode) {
		 uByte addressRead;
		 
		 
		switch (mode) {
			case IMPLICIT:
				break;
			case ACCUMULATOR:
				break;
			case IMMEDIATE:
				break;
			case ZERO_PAGE:
				addressRead = memory.read(address);
				break;
			case ZERO_PAGE_X:
				uByte zeroPageIndex = memory.read(address);
				uShort zeroPageXOffset = new uShort(zeroPageIndex.get()+X.get());
				addressRead = memory.read(zeroPageXOffset);
				break;
			case ZERO_PAGE_Y:	
				uByte zeroPageIndex = memory.read(address);
				uShort zeroPageXOffset = new uShort(zeroPageIndex.get()+X.get());
				addressRead = memory.read(zeroPageXOffset);
				break;
			case RELATIVE:
				uByte relOffset = memory.read(tempPC.increment());
				addressRead = tempPC.increment(2 + relOffset.get());
				break;
			case ABSOLUTE:
				/*
				uByte L = memory.read(tempPC.increment());
				uByte H = memory.read(tempPC.increment(2));
				System.err.println(
					tempPC + "," + tempPC.increment() + "," + tempPC.increment(2));
				System.err.println("Absolute " + L+H+" @" + PC);
				*/
				addressRead = new uShort(
					memory.read(tempPC.increment(2)),
					memory.read(tempPC.increment())
				);
				break;
			case ABSOLUTE_X:
				addressRead = new uShort(
					memory.read(tempPC).increment(2),
					memory.read(tempPC).increment()
				).increment(X.get());
				break;
			case ABSOLUTE_Y:
				addressRead = new uShort(
					memory.read(tempPC).increment(2),
					memory.read(tempPC).increment()
				).increment(Y.get());
				break;
			case INDIRECT:
				addressRead = new uShort(
					memory.read(tempPC).increment(2),
					memory.read(tempPC).increment()
				);
				addressRead = new uShort(
					memory.read(addressRead.increment()),
					memory.read(addressRead)
					);
				break;
			case INDIRECT_X:
				addressRead = new uShort(
					memory.read(tempPC).increment(2),
					memory.read(tempPC).increment()
				).increment(X.get());
				addressRead = new uShort(
					memory.read(addressRead.increment()),
					memory.read(addressRead)
					);
				break;
			case INDIRECT_Y:
				addressRead = new uShort(
					memory.read(tempPC).increment(2),
					memory.read(tempPC).increment()
				);				
				addressRead = new uShort(
					memory.read(addressRead.increment()),
					memory.read(addressRead)
					).increment(Y.get());
				break;
			default:
				logger.error("No matching addressing mode for {}", mode);
				throw new AddressingModeException(mode.toString());
		}
		
		logger.info("Reading opcode {} at PC {} with mode {}. Got final address {}", new Object[]{o, PC, mode, addressRead});
		return addressRead;
	 }
	 
	
	
	/**
	 * Main function to emulate operations of the processor to actions
	 * upon the CPU class and others.
	 * @param Opcode value as a uByte
	 * @return Number of cycles taken for the instruction
	 */
	private void processOp(Opcode op) {
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

		private void ADCi() {
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

		private void CLC() {
			P.clearZero();
			this.cyclesRun += Opcode.CLC.getCycles();
		}

		private void CLD() {
			logger.info("CLD");
			P.clearDecimal();
			this.cyclesRun += Opcode.CLD.getCycles();
		}

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
			uShort subAddr = this.readBytesAsAddress(PC);
			incrementPC();
			S.push(CPU.this.PC.getUpper());
			S.push(CPU.this.PC.getLower());
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
			uByte L = S.pop();
			uByte H = S.pop();
			uShort addr = new uShort(H, L);
			CPU.this.setPC(addr);
			incrementPC();
			this.cyclesRun += Opcode.RTS.getCycles();
		}

		private void SEI() {
			logger.info( "SEI");
			P.setInterruptDisable();
			this.cyclesRun += Opcode.SEI.getCycles();
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
			S.set(X);
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
	}

