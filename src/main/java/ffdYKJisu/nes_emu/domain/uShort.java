/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ffdYKJisu.nes_emu.domain;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 *
 * @author Administrator
 */
public final class uShort {

	private final char value;

	public uShort() {
		this.value = 0;
	}

	public uShort(char initVal) {
		this.value = initVal;
	}

	public uShort(uShort copy) {
		this.value = copy.get();
	}

	public uShort(uByte lower) {
		this.value = (char) lower.get();
	}

	public uShort(uByte H, uByte L) {
		this.value = (char) ((char) (H.get() << 8) + L.get());
	}

	public uShort(int initVal) {
		this.value = (char) (0xffff & initVal);
	}

	public char get() {
		return this.value;
	}

	/**
	 *
	 * @return 16-bit value like "$F4"
	 */
	@Override
	public String toString() {
		//return "" + ((int) this.uShort);
		return String.format("$%04X", (int) this.value);
	}

	static public String addressToString(uShort ushort) {
		return String.format("$%0X", (int) ushort.get());
	}

	static public String addressToString(char ushort) {
		return String.format("$%0X", (int) ushort);
	}

	static public uShort toAddress(int addr) {
		return new uShort(addr);
	}

	static public uShort toAddress(uByte addr) {
		return new uShort(addr.get());
	}

	/**
	 * Returns the lower portion of a 16-bit value as an 8-bit value
	 * @return uByte of the lower portion of the uShort
	 */
	public uByte getLower() {
		return new uByte(this.value);
	}

	/**
	 * Returns the upper portion of a 16-bit value as an 8-bit value
	 * @return uByte of the upper portion of the uShort
	 */
	public uByte getUpper() {
		return new uByte((this.value >> 8) & 0x00FF);
	}

	public uShort increment() {
		return new uShort(this.value + 1);
	}

	public uShort increment(short incrementAmount) {
		return new uShort(this.value + incrementAmount);
	}

	public uShort increment(int i) {
		return new uShort(this.value + i);
	}

	@Override public boolean equals(Object obj) {
		   if (obj == null) { return false; }
		   if (obj == this) { return true; }
		   if (obj.getClass() != getClass()) {
		     return false;
		   }
		   uByte rhs = (uByte) obj;
		   return new EqualsBuilder()
		                 .append(this.get(),rhs.get()).isEquals();
	}   
	
	@Override public int hashCode() {
		return new HashCodeBuilder().append(value).toHashCode();
	}
}