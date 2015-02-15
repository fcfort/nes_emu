package ffdYKJisu.nes_emu.system.memory;

/** @author fcf */
public interface IMemory {
	byte read(short address);

	void write(short address, byte val);
}
