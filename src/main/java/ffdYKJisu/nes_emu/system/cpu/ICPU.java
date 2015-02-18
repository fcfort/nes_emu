package ffdYKJisu.nes_emu.system.cpu;


/** 
 * The CPU holds the internal state of the NES. It does not know about carts or
 * anything like that. It simply reads op codes at the program counter, 
 * and manipulates status, registers and memory
 */
public interface ICPU {

	void reset();
	
	/** Runs a single operation from the current program counter */
	void runStep();
	
	/* Registers */
	
	/** Returns the current value of the program counter */
	short getPC();
	
	byte getSP();
	
	byte getA();
	
	byte getX();
	
	byte getY();
	
	/* Status */
	
	boolean getCarryFlag();
	
	boolean getZeroFlag();
	
	boolean getInterruptDisable();
	
	boolean getDecimalMode();
	
	boolean getBreakCommand();
	
	boolean getOverflowFlag();
	
	boolean getNegativeFlag();
}
