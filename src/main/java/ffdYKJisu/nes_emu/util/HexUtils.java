package ffdYKJisu.nes_emu.util;

public final class HexUtils {

	private HexUtils() {}
		
	public static String toHex(short address_) {
		return String.format("0x%04X", address_);
	}
	
	public static String toHex(byte address_) {
		return String.format("0x%02X", address_);
	}
	
}
