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
		byte b = 0;		
		b = setBit(b, 7, N);
		b = setBit(b, 6, V);
		b = setBit(b, 5, true);
		b = setBit(b, 4, false);
		b = setBit(b, 3, D);
		b = setBit(b, 2, I);
		b = setBit(b, 1, Z);
		b = setBit(b, 0, C);
		return b;	
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
	
	private static byte setBit(byte bitField_, int index_, boolean value_) {		
		if(value_) {
			bitField_ |= (1 << index_);
		} else {
			bitField_ &= ~(1 << index_);
		}
		 
		return bitField_;
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
