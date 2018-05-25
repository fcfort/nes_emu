package ffdYKJisu.nes_emu.domain;

import java.util.BitSet;


/**
 * Holds the status bits for the processor status
 */
public class StatusBit {

	/** Carry		*/
	private boolean C;
	/** Zero		*/
	private boolean Z;
	/** Interrupt	*/
	private boolean I;
	/** Decimal		*/
	private boolean D;
	/** Overflow	*/
	private boolean V;
	/** Negative	*/
	private boolean N;

	public byte asByte() {
		return (byte) (
		   (N ? 1 : 0) << 7 |
		   (V ? 1 : 0) << 6 |
		   1 << 5 |
		   (D ? 1 : 0) << 3 |
		   (I ? 1 : 0) << 2 |
		   (Z ? 1 : 0) << 1 |
		   (C ? 1 : 0)
		  );
	}

	public void fromByte(byte pop_) {
		BitSet b = BitSet.valueOf(new byte[] {pop_});
		N = b.get(7);
		V = b.get(6);
		D = b.get(3);
		I = b.get(2);
		Z = b.get(1);
		C = b.get(0);
	}

	public void setCarry() {
		this.C = true;
	// cpuLogger.log(Level.FINER,"Carry flag set");
	}

	public void clearCarry() {
		this.C = false;
	// cpuLogger.log(Level.FINER,"Carry flag cleared");
	}

	public void setZero() {
		this.Z = true;
	// cpuLogger.log(Level.FINER,"Zero flag set");
	}

	public void clearZero() {
		this.Z = false;
	// cpuLogger.log(Level.FINER,"Zero flag cleared");
	}

	public void setInterruptDisable() {
		this.I = true;
	// cpuLogger.log(Level.FINER,"Interrupt flag set");
	}

	public void clearInterruptDisable() {
		this.I = false;
	// cpuLogger.log(Level.FINER,"Interrupt flag cleared");
	}

	public void setDecimal() {
		this.D = true;
	// cpuLogger.log(Level.FINER,"Decimal flag set");
	}

	public void clearDecimal() {
		this.D = false;
	// cpuLogger.log(Level.FINER,"Decimal flag cleared");
	}

	public void setOverflow() {
		this.V = true;
	// cpuLogger.log(Level.FINER,"Overflow flag set");
	}

	public void clearOverflow() {
		this.V = false;
	// cpuLogger.log(Level.FINER,"Overflow flag cleared");
	}

	public void setNegative() {
		this.N = true;
	// cpuLogger.log(Level.FINER,"Negative flag set");
	}

	public void clearNegative() {
		this.N = false;
	// cpuLogger.log(Level.FINER,"Negative flag cleared");
	}
	// ***************
	// Setters that accept a value
	// ***************
	public void setCarry(boolean b) {
		this.C = b;
	}

	public void setZero(boolean b) {
		this.Z = b;
	}

	public void setInterruptDisable(boolean b) {
		this.I = b;
	}

	public void setDecimal(boolean b) {
		this.D = b;
	}

	public void setOverflow(boolean b) {
		this.V = b;
	}

	public void setNegative(boolean b) {
		this.N = b;
	}
	// ***************
	// Getters for class variables
	// ***************
	public boolean isSetCarry() {
		return C;
	}

	public boolean isSetZero() {
		return Z;
	}

	public boolean isSetInterruptDisable() {
		return I;
	}

	public boolean isSetDecimal() {
		return D;
	}

	public boolean isSetOverflow() {
		return V;
	}

	public boolean isSetNegative() {
		return N;
	}

  @Override
  public String toString() {
    return String.format(
        "N: %d V: %d - - D: %d I: %s Z: %d C: %d",
        this.isSetNegative() ? 1 : 0,
        this.isSetOverflow() ? 1 : 0,
        this.isSetDecimal() ? 1 : 0,
        this.isSetInterruptDisable() ? 1 : 0,
        this.isSetZero() ? 1 : 0,
        this.isSetCarry() ? 1 : 0);
  }
}
