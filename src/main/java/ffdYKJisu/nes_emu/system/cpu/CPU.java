package ffdYKJisu.nes_emu.system.cpu;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Shorts;
import ffdYKJisu.nes_emu.domain.AddressingMode;
import ffdYKJisu.nes_emu.domain.Opcode;
import ffdYKJisu.nes_emu.domain.StatusBit;
import ffdYKJisu.nes_emu.exceptions.AddressingModeException;
import ffdYKJisu.nes_emu.exceptions.OpcodeExecutionException;
import ffdYKJisu.nes_emu.system.cartridge.Cartridge;
import ffdYKJisu.nes_emu.system.memory.Addressable;
import ffdYKJisu.nes_emu.system.memory.ArrayCpuMemory;
import ffdYKJisu.nes_emu.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * Controls all functions of the main CPU of the NES. Handles all opcode processing and registers of
 * the cpu. The cart is loaded and restarted and emulation begins in this class.
 *
 * @author fe01106
 */
public class CPU implements ICPU {

  private static Logger logger = LoggerFactory.getLogger(CPU.class);

  /** Program counter, holds memory location of current position */
  private short PC;
  /** Accumulator */
  private byte A;
  /** Index register X */
  private byte X;
  /** Index register X */
  private byte Y;
  /** Holds the bits of the status byte for the processor */
  private StatusBit P;

  private final Addressable memory;
  private int _cyclesRun;
  private int _cyclesRunSinceReset;
  private byte _stackPointer;

  private static short NMI_VECTOR_LOW = (short) 0xFFFA;
  private static short NMI_VECTOR_HIGH = (short) 0xFFFB;

  private static short RESET_VECTOR_LOW = (short) 0xFFFC;
  private static short RESET_VECTOR_HIGH = (short) 0xFFFD;

  private static short INTERRUPT_VECTOR_LOW = (short) 0xFFFE;
  private static short INTERRUPT_VECTOR_HIGH = (short) 0xFFFF;

  private static final short STACK_OFFSET = 0x100;

  private boolean _nonMaskableInterruptFlag;

  public CPU(Cartridge cartridge) {
    memory = new ArrayCpuMemory(cartridge);
    _cyclesRun = 0;
    _cyclesRunSinceReset = 0;
    // Set up State registers
    P = new StatusBit();
    reset();
  }

  /** Runs the CPU for one operation regardless of how long it will take */
  public void runStep() {
    // Read Opcode from PC
    Opcode op = getOpcode();

    short initialPC = PC;

    // Print instruction to logger
    logger.info(
        "CPU - Cycle: {}, Got instruction {} opcode {} at PC {}",
        new Object[] {_cyclesRunSinceReset, instructionToString(PC), op, HexUtils.toHex(PC)});

    // Increment PC
    PC += op.getLength();

    Byte result = doOperation(op, initialPC);

    persistResult(op, result, initialPC);

    short finalPC = PC;

    incrementCycles(calculateCyclesTaken(op, initialPC, finalPC));

    processInterrupts();
  }

  private void processInterrupts() {
    if (_nonMaskableInterruptFlag) {
      logger.info("Processing NMI");
      NMI();
      incrementCycles(7);
    }
  }

  void incrementCycles(int cyclesTaken_) {
    _cyclesRun += cyclesTaken_;
    _cyclesRunSinceReset += cyclesTaken_;
  }

  /**
   * Two mutually exclusive options, one, for indexed reads, add one cycle if indexing across page
   * boundary. Two, for branches add one cycle if branch is taken, Add one additional if branching
   * operation crosses page boundary
   *
   * @param op_
   * @return cycles taken
   */
  private int calculateCyclesTaken(Opcode op_, short initialPC_, short finalPC_) {
    int cyclesTaken = op_.getCycles();

    // Branches
    if (AddressingMode.RELATIVE == op_.getAddressingMode()) {
      if (initialPC_ != finalPC_ && op_.extraCycleOnBranch()) {
        cyclesTaken++;
        byte initialPage = Shorts.toByteArray(initialPC_)[0];
        byte finalPage = Shorts.toByteArray(finalPC_)[0];
        if (initialPage != finalPage && op_.extraCycleOnPageJump()) {
          cyclesTaken++;
        }
      }
      return cyclesTaken;
    }

    // Indexed addressing
    else if (ImmutableSet.of(
                AddressingMode.ABSOLUTE_X, AddressingMode.ABSOLUTE_Y, AddressingMode.INDIRECT_Y)
            .contains(op_.getAddressingMode())
        && op_.extraCycleOnPageJump()) {
      if (isIndexedPageJump(op_, initialPC_)) {
        return op_.getCycles() + 1;
      }
    }

    return cyclesTaken;
  }

  private Byte doOperation(Opcode op_, short PC_) {
    if (op_.readsMemory()) {
      short address = getAddress(op_.getAddressingMode(), PC_);
      // JMP/JSR take in the address directly
      if (ImmutableSet.of("JMP", "JSR").contains(op_.getCodeName())) {
        return invokeMethod(op_.getCodeName(), address);
      } else {
        byte operand = getOperand(op_.getAddressingMode(), address);
        return invokeMethod(op_.getCodeName(), operand);
      }
    } else {
      return invokeMethod(op_.getCodeName());
    }
  }

  private Byte invokeMethod(String methodName_) {
    try {
      return (Byte) getClass().getMethod(methodName_).invoke(this);
    } catch (IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException e) {
      throw new OpcodeExecutionException(e, "Unable to execute %s", methodName_);
    }
  }

  private Byte invokeMethod(String methodName_, byte operand_) {
    try {
      return (Byte) getClass().getMethod(methodName_, byte.class).invoke(this, operand_);
    } catch (IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException e) {
      throw new OpcodeExecutionException(
          e, "Unable to execute %s with operand %s", methodName_, operand_);
    }
  }

  private Byte invokeMethod(String methodName_, short address_) {
    try {
      return (Byte) getClass().getMethod(methodName_, short.class).invoke(this, address_);
    } catch (IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException
        | NoSuchMethodException
        | SecurityException e) {
      throw new OpcodeExecutionException(
          e, "Unable to execute %s with address %s", methodName_, address_);
    }
  }

  /* *******************
   * Addressing
   ******************* */

  private byte getOperand(AddressingMode mode_, short address_) {
    logger.debug("Finding operand for mode {} at address {}", mode_, HexUtils.toHex(address_));

    switch (mode_) {
      case IMPLICIT:
        throw new UnsupportedOperationException();
      case ACCUMULATOR:
        logger.debug("Got operand A = {}", A);
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
        byte val = memory.read(address_);
        logger.debug(
            "Got operand of value {} at address {}", HexUtils.toHex(val), HexUtils.toHex(address_));
        return val;
      default:
        logger.error("No matching addressing mode for {}", mode_);
        throw new UnsupportedOperationException();
    }
  }

  /**
   * Reads current PC, reads the opcode there, determines the addressing mode and returns an address
   * by determining what bytes to read from the parameters of the instruction. The address returned
   * is either the read/write address for the instruction
   *
   * <p>http://wiki.nesdev.com/w/index.php/CPU_addressing_modes
   *
   * @return Address of where to perform operation
   */
  private short getAddress(AddressingMode mode_, short PC_) {
    short addr = 0;

    switch (mode_) {
      case IMPLICIT:
        break;
      case ACCUMULATOR:
        break;
      case RELATIVE:
      case IMMEDIATE:
        addr = (short) (PC_ + 1);
        break;
      case ZERO_PAGE:
        byte zpAddr = memory.read((short) (PC_ + 1));
        addr = (short) (zpAddr & 0xFF);
        break;
      case ZERO_PAGE_X:
        byte zpAddrX = (byte) (memory.read((short) (PC_ + 1)) + X);
        addr = (short) (zpAddrX & 0xFF);
        break;
      case ZERO_PAGE_Y:
        byte zpAddrY = (byte) (memory.read((short) (PC_ + 1)) + Y);
        addr = (short) (zpAddrY & 0xFF);
        break;
      case ABSOLUTE:
        addr = readShort((short) (PC_ + 1));
        break;
      case ABSOLUTE_X:
        // val = PEEK(arg + X)
        addr = (short) (readShort((short) (PC_ + 1)) + (X & 0xFF));
        break;
      case ABSOLUTE_Y:
        // val = PEEK(arg + Y)
        addr = (short) (readShort((short) (PC_ + 1)) + (Y & 0xFF));
        break;
      case INDIRECT:
        addr = readShort((short) (PC_ + 1));

        byte lowAddr = Shorts.toByteArray(addr)[1];
        byte highAddr = Shorts.toByteArray(addr)[0];
        addr =
            Shorts.fromBytes(
                memory.read(Shorts.fromBytes(highAddr, (byte) (lowAddr + 1))),
                memory.read((addr)));
        break;
      case INDIRECT_X:
        /* val = PEEK(
         * 			PEEK((arg + X) % 256) +
         * 			PEEK((arg + X + 1) % 256) * 256)
         */
        // TODO: Unify indirect addressing modes
        byte argX = memory.read((short) (PC_ + 1));
        short zeroPageAddress = (short) ((Byte.toUnsignedInt(argX) + Byte.toUnsignedInt(X)) % 256);
        byte lowerAddr = memory.read(zeroPageAddress);
        byte upperAddr = memory.read((short) ((zeroPageAddress + 1) % 256));
        addr = Shorts.fromBytes(upperAddr, lowerAddr);
        logger.info(
            "For mode {} got arg {} @ PC {}, got final addr {} from upper {} and lower {}",
            new Object[] {
              mode_,
              HexUtils.toHex(argX),
              HexUtils.toHex(PC),
              HexUtils.toHex(addr),
              HexUtils.toHex(upperAddr),
              HexUtils.toHex(lowerAddr),
            });
        break;
      case INDIRECT_Y:

        /* val = PEEK(PEEK(arg) + PEEK((arg + 1) % 256) + y) */
        byte argY = memory.read((short) (PC_ + 1));
        byte lowerAddrY = memory.read((short) Byte.toUnsignedInt(argY));
        byte upperAddrY = memory.read((short) ((Byte.toUnsignedInt(argY) + 1) & 0xFF));
        addr = (short) (Shorts.fromBytes(upperAddrY, lowerAddrY) + Byte.toUnsignedInt(Y));
        break;
      default:
        logger.error("No matching addressing mode for {}", mode_);
        throw new AddressingModeException(mode_.toString());
    }

    logger.debug(
        "Got address {} from mode {} at PC {}",
        new Object[] {HexUtils.toHex(addr), mode_, HexUtils.toHex(PC_)});
    return addr;
  }

  /** Reads an address for two consecutive bytes and forms that into an address */
  private short readShort(short address) {
    return Shorts.fromBytes(memory.read((short) (address + 1)), memory.read((address)));
  }

  /**
   * Checks to see if the addressing mode will result in an additional cycle due to a page jum
   *
   * @param op_
   * @param initialPC_
   * @return true if the indexed operation crosses a page boundary
   */
  private boolean isIndexedPageJump(Opcode op_, short initialPC_) {
    switch (op_.getAddressingMode()) {
      case ABSOLUTE_X:
        return isIndexAbsolutePageJump((short) (initialPC_ + 1), X);
      case ABSOLUTE_Y:
        return isIndexAbsolutePageJump((short) (initialPC_ + 1), Y);
      case INDIRECT_Y:
        byte argY = memory.read((short) (initialPC_ + 1));
        byte lowerAddrY = memory.read(argY);
        return Byte.toUnsignedInt(lowerAddrY) + Byte.toUnsignedInt(Y) > 0xFF;
      default:
        return false;
    }
  }

  private boolean isIndexAbsolutePageJump(short address_, byte val_) {
    byte lowerAddress = memory.read(address_);
    int result = Byte.toUnsignedInt(lowerAddress) + Byte.toUnsignedInt(val_);
    return result > 0XFF;
  }

  private void persistResult(Opcode op_, Byte result_, short PC_) {
    // void functions return null. Don't have to do anything.
    if (result_ == null) {
      return;
    }

    switch (op_.getAddressingMode()) {
      case ABSOLUTE:
      case ABSOLUTE_X:
      case ABSOLUTE_Y:
      case INDIRECT:
      case INDIRECT_X:
      case INDIRECT_Y:
      case ZERO_PAGE:
      case ZERO_PAGE_X:
      case ZERO_PAGE_Y:
        short address = getAddress(op_.getAddressingMode(), PC_);
        logger.info(
            "Persisting result {} from operation {} to {}",
            new Object[] {HexUtils.toHex(result_), op_, HexUtils.toHex(address)});
        memory.write(address, result_);
        break;
      case ACCUMULATOR:
        logger.info("Persisting result {} from operation {} to A", HexUtils.toHex(result_), op_);
        A = result_;
        break;
      case IMMEDIATE: // Some ops read immediate results but these shouldn't return results
      case IMPLICIT:
      case RELATIVE: // all branching functions don't have results
      default:
        throw new UnsupportedOperationException();
    }
  }

  /**
   * Returns the number of bytes that the current instruction occupies. This includes the actual
   * instruction opcode itself. I.e. CLD returns 1 even though it has no parameters
   *
   * @param address Address at which the instruction is at
   * @return Number of bytes until next instruction
   */
  private int instructionLength(short address) {
    return Opcode.getOpcodeByBytes(memory.read(address)).getLength();
  }

  /**
   * Retrieves the next opcode from memory and returns it
   *
   * @return opCode
   */
  protected Opcode getOpcode() {
    byte b = memory.read(PC);
    Opcode o = Opcode.getOpcodeByBytes(b);
    logger.debug(
        "Reading opcode at PC addr {}. Got byte {} and opcode {}",
        new Object[] {HexUtils.toHex(PC), HexUtils.toHex(b), o});
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
    P.setOverflow((val_ & 1 << 6) != 0); // Set overflow to value of bit 6
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
    val_ = (byte) ((val_ & 0xFF) >>> 1);
    setZero(val_);
    setNegative(val_);
    return val_;
  }

  public byte ROL(byte val_) {
    boolean carry = (val_ & 0x80) != 0;
    byte result = shift(val_, 1, P.isSetCarry());
    P.setCarry(carry);
    return result;
  }

  public byte ROR(byte val_) {
    boolean carry = (val_ & 0x01) != 0;
    byte result = shift(val_, -1, P.isSetCarry());
    P.setCarry(carry);
    return result;
  }

  /** positive shiftAmount <<, negative shiftAmount >> */
  private byte shift(byte val_, int direction_, boolean carry_) {
    if (direction_ > 0) {
      val_ <<= 1; // do shift
      val_ = (byte) (carry_ ? val_ | 1 : val_ & ~1); // account for carry
    } else {
      val_ = (byte) ((val_ & 0xFF) >>> 1);
      logger.info("Val after shift {}", HexUtils.toHex(val_));
      val_ = (byte) (carry_ ? val_ | (1 << 7) : val_ & ~(1 << 7));
      logger.info("Val after carry {}, carry = {}", HexUtils.toHex(val_), carry_);
    }
    setZero(val_);
    setNegative(val_);
    return val_;
  }

  /* *******************
   * Arithmetic
   ******************* */

  public void ADC(byte val_) {
    int temp = Byte.toUnsignedInt(val_) + Byte.toUnsignedInt(A) + (P.isSetCarry() ? 1 : 0);
    P.setCarry(temp > 0xFF);
    // ADC: SET_OVERFLOW(!((AC ^ src) & 0x80) && ((AC ^ temp) & 0x80));
    P.setOverflow(((A ^ val_) & 0x80) == 0 && ((A ^ temp) & 0x80) != 0);
    A = (byte) temp;
    setZero(A);
    setNegative(A);
  }

  /** http://forums.nesdev.com/viewtopic.php?p=19080#19080 */
  public void SBC(byte val_) {
    ADC((byte) (val_ ^ 0xFF));
  }

  public void INX() {
    X = increment(X, 1);
  }

  public void DEX() {
    X = increment(X, -1);
  }

  public void INY() {
    Y = increment(Y, 1);
  }

  public void DEY() {
    Y = increment(Y, -1);
  }

  public byte INC(byte val_) {
    return increment(val_, 1);
  }

  public byte DEC(byte val_) {
    return increment(val_, -1);
  }

  private byte increment(byte val_, int increment_) {
    byte result = (byte) (val_ + increment_);
    setNegative(result);
    setZero(result);
    return result;
  }

  /** http://forums.nesdev.com/viewtopic.php?t=6331 */
  private void setOverflow(boolean isAdding_, byte initialA_, byte val_, byte sum_) {
    // ADC: SET_OVERFLOW(!((AC ^ src) & 0x80) && ((AC ^ temp) & 0x80));
    if (isAdding_) {
      P.setOverflow(((initialA_ ^ val_) & 0x80) == 0 && ((initialA_ ^ sum_) & 0x80) != 0);
    } else {
      // SBC: SET_OVERFLOW(((AC ^ temp) & 0x80) && ((AC ^ src) & 0x80));
      P.setOverflow(((initialA_ ^ sum_) & 0x80) != 0 && ((initialA_ ^ val_) & 0x80) != 0);
    }
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

  public void BCS(byte val_) {
    branch(P.isSetCarry(), val_);
  }

  public void BCC(byte val_) {
    branch(!P.isSetCarry(), val_);
  }

  public void BEQ(byte val_) {
    branch(P.isSetZero(), val_);
  }

  public void BNE(byte val_) {
    branch(!P.isSetZero(), val_);
  }

  public void BMI(byte val_) {
    branch(P.isSetNegative(), val_);
  }

  public void BPL(byte val_) {
    branch(!P.isSetNegative(), val_);
  }

  public void BVS(byte val_) {
    branch(P.isSetOverflow(), val_);
  }

  public void BVC(byte val_) {
    branch(!P.isSetOverflow(), val_);
  }

  private void branch(boolean status_, byte offset_) {
    if (status_) {
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

  public void RTS() {
    byte lowAddr = pop();
    PC = (short) (Shorts.fromBytes(pop(), lowAddr) + 1);
  }

  /** http://wiki.nesdev.com/w/index.php/CPU_status_flag_behavior */
  public void BRK() {
    PC++;
    pushPC();
    byte status = P.asByte();
    status |= 1 << 4; // "B" flag only exists on the stack, never set in status register
    push(status);
    P.setInterruptDisable();
    setPCFromVector(INTERRUPT_VECTOR_LOW, INTERRUPT_VECTOR_HIGH);
  }

  /** Not a true opcode but similar to BRK */
  public void NMI() {
    pushPC();
    push(P.asByte());
    P.setInterruptDisable();
    setPCFromVector(NMI_VECTOR_LOW, NMI_VECTOR_HIGH);
  }

  public void RTI() {
    P.fromByte(pop());
    byte lowAddr = pop();
    PC = Shorts.fromBytes(pop(), lowAddr);
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

  public void CLC() {
    P.clearCarry();
  }

  public void CLD() {
    P.clearDecimal();
  }

  public void CLI() {
    P.clearInterruptDisable();
  }

  public void CLV() {
    P.clearOverflow();
  }

  /* *******************
   * Compares
   ******************* */

  public void CMP(byte val_) {
    compare(A, val_);
  }

  public void CPX(byte val_) {
    compare(X, val_);
  }

  public void CPY(byte val_) {
    compare(Y, val_);
  }

  /**
   * Compares two bytes, used by all comparison operations. Simulates A - B and changes status flags
   * based on results
   *
   * @param a_ minuend (usually a register)
   * @param b_ subtrahend (usually from memory)
   */
  private void compare(byte a_, byte b_) {
    int a = Byte.toUnsignedInt(a_);
    int b = Byte.toUnsignedInt(b_);
    int result = a - b;
    logger.info(
        "byte a {} - byte b {} = {}",
        new Object[] {HexUtils.toHex(a_), HexUtils.toHex(b_), result});
    byte resultByte = (byte) (result & 0xFF);
    P.setCarry(result >= 0);
    P.setZero(resultByte == 0);
    P.setNegative((resultByte & 0x80) != 0);
  }

  /* *******************
   * Stack
   ******************* */

  public void PHA() {
    push(A);
  }

  public void PHP() {
    byte status = P.asByte();
    status |=
        1
            << 4; // "B" flag only exists on the stack, never set in status register, see BRK
                  // instruction
    push(status);
  }

  public void PLA() {
    A = pop();
    setZero(A);
    setNegative(A);
  }

  public void PLP() {
    P.fromByte(pop());
  }

  private void push(byte val_) {
    memory.write((short) (Byte.toUnsignedInt(_stackPointer) + STACK_OFFSET), val_);
    _stackPointer--;
  }

  private byte pop() {
    _stackPointer++;
    return memory.read((short) (Byte.toUnsignedInt(_stackPointer) + STACK_OFFSET));
  }

  private void pushPC() {
    byte[] bytesPC = Shorts.toByteArray(PC);
    push(bytesPC[0]); // Upper
    push(bytesPC[1]); // Lower
  }

  /* *******************
   * Other
   ******************* */

  public void NOP() {}

  /* *******************
   * Sets
   ******************* */

  public void SEC() {
    P.setCarry();
  }

  public void SED() {
    P.setDecimal();
  }

  public void SEI() {
    P.setInterruptDisable();
  }

  /* *******************
   * Stores
   ******************* */

  public byte STA() {
    return A;
  }

  public byte STX() {
    return X;
  }

  public byte STY() {
    return Y;
  }

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

  public void TXA() {
    A = X;
    setNegative(A);
    setZero(A);
  }

  public void TXS() {
    _stackPointer = X;
  }

  public void TYA() {
    A = Y;
    setNegative(A);
    setZero(A);
  }

  /* *******************
   * Helper functions
   ******************* */

  /**
   * Accepts two addresses and returns true if those two addresses are on different pages. I.e.
   * return false if their upper bytes are not equal.
   *
   * @param startAddress Starting address, typically the PC of the instruction
   * @param endAddress End address, typically the location that is to be written to.
   * @return Returns true if the two addresses lie on different pages, false otherwise.
   */
  boolean pageJumped(short startAddress, short endAddress) {
    // return !startAddress.getUpper().equals(endAddress.getUpper());
    return false;
  }

  /**
   * http://forums.nesdev.com/viewtopic.php?f=3&t=9252
   * http://stackoverflow.com/questions/16913423/why-is-the-initial-state-of-the-interrupt-flag-of-the-6502-a-1
   */
  public void reset() {
    A = 0;
    X = 0;
    Y = 0;
    P.fromByte((byte) 0);
    P.setInterruptDisable();
    _stackPointer = (byte) 0xFD;
    _cyclesRunSinceReset = 0;
    setPCFromVector(RESET_VECTOR_LOW, RESET_VECTOR_HIGH);
  }

  private void setPCFromVector(short vectorLow_, short vectorHigh_) {
    short address = Shorts.fromBytes(memory.read(vectorHigh_), memory.read(vectorLow_));
    logger.info(
        "Jumping to {} from vector {} {}",
        new Object[] {
          HexUtils.toHex(address), HexUtils.toHex(vectorHigh_), HexUtils.toHex(vectorLow_),
        });
    PC = address;
  }

  public short getPC() {
    return PC;
  }

  public void setPC(short address_) {
    PC = address_;
  }
  /** Stack pointer */
  public byte getSP() {
    return _stackPointer;
  }
  /** Status register */
  public byte getSR() {
    return P.asByte();
  }

  public byte getA() {
    return A;
  }

  public byte getX() {
    return X;
  }

  public byte getY() {
    return Y;
  }

  public boolean getCarryFlag() {
    return P.isSetCarry();
  }

  public boolean getZeroFlag() {
    return P.isSetZero();
  }

  public boolean getInterruptDisable() {
    return P.isSetInterruptDisable();
  }

  public boolean getDecimalMode() {
    return P.isSetDecimal();
  }

  public boolean getOverflowFlag() {
    return P.isSetOverflow();
  }

  public boolean getNegativeFlag() {
    return P.isSetNegative();
  }

  public Addressable getMemory() {
    return memory;
  }

  public int getCyclesSinceReset() {
    return _cyclesRunSinceReset;
  }

  public int getCycles() {
    return _cyclesRun;
  }

  /**
   * Reads the instruction at that address. Creates a string that will readable and will look like
   * "$FF00: ($D3 $F0) ADD $F0".
   *
   * @param address Location where you want to read an instruction from
   * @return A string formatted for debugger display.
   */
  public String instructionToString(short address) {
    StringBuilder sb = new StringBuilder(HexUtils.toHex(address) + ": ");
    int instructionLength = this.instructionLength(address);

    byte[] bytes = new byte[instructionLength];

    StringBuilder sbBytes = new StringBuilder();

    sbBytes.append("(");
    for (int j = 0; j < instructionLength; j++) {
      byte b = memory.read(address);
      sbBytes.append(HexUtils.toHex(b));
      bytes[j] = b;
      if (j != instructionLength - 1) sbBytes.append(" ");
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
      sb.append(HexUtils.toHex(bytes[j]));
      if (j != instructionLength - 1) sb.append(" ");
    }
    return sb.toString();
  }

  public void nonMaskableInterrupt() {
    _nonMaskableInterruptFlag = true;
  }
}
