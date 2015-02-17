package ffdYKJisu.nes_emu.domain;

import java.util.Map;

import com.google.common.collect.Maps;

public class Register {
	
	private Map<String, Integer> _namedPositions;
	private byte _register;
	
	public Register() {
		_register = 0;
		_namedPositions = Maps.newHashMap();
	}
	
	public Register(Map<String, Integer> namedPositions_) {
		_register = 0;
		_namedPositions = namedPositions_;
	}
	
	public boolean getValue(String name_) {
		return getValue(_namedPositions.get(name_));
	}
	
	public boolean getValue(int position_) {
		return (_register & 1 << position_) != 0; 
	}
	
	public void setValue(String name_, boolean value_) {
		setValue(_namedPositions.get(name_), value_);
	}
	
	public void setValue(int position_, boolean value_) {	
		_register = (byte) (
				value_
		          ? _register | (1 << position_)
		          : _register & ~(1 << position_));
	}
	
	public void setByte(byte value_) {
		_register = value_;
	}
	
	public byte getByte() {
		return _register;
	}
}
