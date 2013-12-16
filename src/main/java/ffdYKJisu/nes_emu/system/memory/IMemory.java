package ffdYKJisu.nes_emu.system.memory;

import ffdYKJisu.nes_emu.domain.uByte;
import ffdYKJisu.nes_emu.domain.uShort;

/**
 * 
 * @author fcf
 */
public interface IMemory {
	uByte read(uShort address);

	void write(uShort address, uByte val);
}
