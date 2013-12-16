package ffdYKJisu.nes_emu.system.memory;

import ffdYKJisu.nes_emu.domain.uByte;
import ffdYKJisu.nes_emu.domain.uShort;
import ffdYKJisu.nes_emu.exceptions.InvalidAddressException;

/**
 * 
 * @author fcf
 */
public interface IMemory {
	uByte read(uShort address);

	void write(uShort address, uByte val) throws InvalidAddressException;
}
