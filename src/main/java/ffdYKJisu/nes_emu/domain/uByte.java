/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ffdYKJisu.nes_emu.domain;

/**
 *
 * @author Administrator
 */
public final class uByte {

    final private short value;

    public uByte(short initVal) {
        this.value = (short) Math.abs(initVal & 0xff);
    }

    public uByte(uByte copy) {
        this.value = copy.get();
    }

    public uByte(int initVal) {
        this.value = (short) Math.abs(initVal & 0xff);
    }

    public short get() {
        return this.value;
    }
    
    @Override public String toString() {
        return "" + String.format("$%02X", (int) this.value);
    }

    /**
     * Returns sign of underlying byte if it were interpreted as a
     * two's complement number
     * @return true if negative, false if positive
     */
    public boolean isNegative() {
        return (((this.value >> 7) & 0x1) == 1);
    }
    
    /**
     * Returns the absolute value of the underlying byte as if it
     * were interpreted as a two's complement number.
     */
    public uByte negativeValue() {
        if (this.value > 0x7f) {
            return new uByte(
                ((~this.value) + 1) // flip the bits, add 1
                & 0xff // keep within one byte
                );    
        } else {
            return new uByte(this.value);
        }
        
    }
    
	/**
	 * This performs a rotate left. You need to pass in the old carry bit
	 * from the processor status.
	 * @param carry new bit zero after rotate
	 */
	public uByte rotateLeft (boolean carrySet) {
		if (carrySet ) {
			return new uByte((this.value<<1)|0x1);
		} else {
			return new uByte((this.value<<1));
		}
	}
	
    /**
     * Converts the uByte to a regular signed byte. Specifically, it maps
     * 0x00-0x7f to [0,127] and 0xff-0x80 to [-1,-128]
     * @return uByte interpreted as a signed number
     */
    public byte toSigned() {
        // If positive
        if(this.value < 0x80) {
            return (byte)this.value;
        } 
        // If negative
        else {
            return (byte)(-1 * this.negativeValue().get());
        }            
    }
    
    public uByte increment() {
		return this.increment(1);
    }
	
	public uByte decrement() {
		return this.increment(-1);
	}
	
	public uByte decrement(int amount) {
		return this.increment(amount * (-1));
	}
    
	public uByte increment(int amount) {
		 return new uByte(this.value + amount);
	}
	
	/*
	@Override public boolean equals(Object byteValue) {
		if ( byteValue instanceOf uByte) {
			
		}
		return (byteValue instanceOf uByte &&
			this.value == ((uByte) byteValue).get());
	}
     */
}
