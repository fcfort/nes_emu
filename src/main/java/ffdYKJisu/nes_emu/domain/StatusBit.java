package ffdYKJisu.nes_emu.domain;

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
	/** Break		*/
	private boolean B;
	/** Overflow	*/
	private boolean V;
	/** Negative	*/
	private boolean N;

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

	public void setBreak() {
		this.B = true;
	// cpuLogger.log(Level.FINER,"Break flag set");
	}

	public void clearBreak() {
		this.B = false;
	// cpuLogger.log(Level.FINER,"Break flag cleared");
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

	public void setBreak(boolean b) {
		this.B = b;
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

	public boolean isSetBreak() {
		return B;
	}

	public boolean isSetOverflow() {
		return V;
	}

	public boolean isSetNegative() {
		return N;
	}

	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("N:");
		if (this.isSetNegative()) {
			s.append("1");
		} else {
			s.append("0");
		}
		s.append(" ");

		s.append("V:");
		if (this.isSetOverflow()) {
			s.append("1");
		} else {
			s.append("0");
		}
		s.append(" ");

		s.append("- - ");

		s.append("D:");
		if (this.isSetDecimal()) {
			s.append("1");
		} else {
			s.append("0");
		}
		s.append(" ");

		s.append("I:");
		if (this.isSetInterruptDisable()) {
			s.append("1");
		} else {
			s.append("0");
		}
		s.append(" ");

		s.append("Z:");
		if (this.isSetZero()) {
			s.append("1");
		} else {
			s.append("0");
		}
		s.append(" ");

		s.append("C:");
		if (this.isSetCarry()) {
			s.append("1");
		} else {
			s.append("0");
		}

		return s.toString();
	}
}
