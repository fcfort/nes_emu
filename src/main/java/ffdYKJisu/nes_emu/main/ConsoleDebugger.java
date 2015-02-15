package ffdYKJisu.nes_emu.main;


import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asg.cliche.Command;
import asg.cliche.ShellFactory;
import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.HexUtils;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.cpu.CPU;
import ffdYKJisu.nes_emu.system.cpu.ICPU;
import ffdYKJisu.nes_emu.system.memory.CPUMemory;

/**
 * Controls interaction between cpu/nes and command line input.
 * This will be used for debugging the cpu core.
 * @author Administrator
 */
public class ConsoleDebugger {
	
	private static final Logger logger = LoggerFactory.getLogger(ConsoleDebugger.class);
	
	private final CPUMemory _memory;
	private final ICPU _cpu;
	private final NES _nes;
	
	public void usage() {
		System.out.println("Welcome to the NES debugger." + 
				" Enter ? for help.");
		System.out.println("s step");
	}
	
	public ConsoleDebugger() throws IOException, UnableToLoadRomException {
		Cartridge c = new Cartridge(ClassLoader.getSystemResourceAsStream("Pac-Man (U) [!].nes"));
		_memory = new CPUMemory();
		_memory.writeCartToMemory(c);
		_cpu = new CPU(_memory);		
		_nes = new NES(_cpu, _memory);		
	}
	
	@Command
	public String status() {
		return String.format("PC: %s SP: %s A: %s X: %s Y: %s Status: S %s V %s B %s D %s I %s Z %s C %s",
				HexUtils.toHex(_cpu.getPC()),
				HexUtils.toHex(_cpu.getSP()),
				HexUtils.toHex(_cpu.getA()),
				HexUtils.toHex(_cpu.getX()),
				HexUtils.toHex(_cpu.getY()),
				_cpu.getNegativeFlag() ? 1 : 0,
				_cpu.getOverflowFlag() ? 1 : 0,
				_cpu.getBreakCommand() ? 1 : 0,
				_cpu.getDecimalMode() ? 1 : 0,
			    _cpu.getInterruptDisable() ? 1 : 0,
				_cpu.getZeroFlag() ? 1 : 0,
				_cpu.getCarryFlag() ? 1 : 0
		);
		
	}
	
	@Command
	public void reset() {
		_cpu.reset();
	}	
	
	@Command
	public void step() {
		_cpu.runStep();
	}	

    public static void main(String[] args) throws IOException, UnableToLoadRomException {
        ShellFactory.createConsoleShell(">", "", new ConsoleDebugger()).commandLoop(); // and three.
    }

}
