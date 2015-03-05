package ffdYKJisu.nes_emu.domain;

public class Register {
	
	private byte _register;
	
	public Register() {
		_register = 0;
	}
	
	public boolean getBit(int position_) {
		return (_register & 1 << position_) != 0; 
	}	
	
	public void setBit(int position_, boolean value_) {	
		_register = (byte) (
				value_
		          ? _register | (1 << position_)
		          : _register & ~(1 << position_));
	}
	
	public void setBit(int position_) { setBit(position_, true); }
	
	public void clearBit(int position_) { setBit(position_, false); }
	
	public void setByte(byte value_) { _register = value_; }
	
	public byte asByte() { return _register; }
}
