package ffdYKJisu.nes_emu.main;


import java.io.IOException;

import ffdYKJisu.nes_emu.system.memory.CpuMemory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asg.cliche.Command;
import asg.cliche.ShellFactory;
import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.cpu.CPU;
import ffdYKJisu.nes_emu.system.memory.ArrayCpuMemory;
import ffdYKJisu.nes_emu.system.memory.PPUMemory;
import ffdYKJisu.nes_emu.system.ppu.PPU;
import ffdYKJisu.nes_emu.util.HexUtils;

/**
 * Controls interaction between cpu/nes and command line input.
 * This will be used for debugging the cpu core.
 * @author Administrator
 */
public class ConsoleDebugger {
	
	private static final Logger logger = LoggerFactory.getLogger(ConsoleDebugger.class);
	
	private final CpuMemory _cpuMemory;
	private final PPUMemory _ppuMemory;
	private final CPU _cpu;
	private final NES _nes;
	private final PPU _ppu;
	
	public void usage() {
		System.out.println("Welcome to the NES debugger." + 
				" Enter ? for help.");
		System.out.println("s step");
	}
	
	public ConsoleDebugger() throws UnableToLoadRomException {
		Cartridge c = new Cartridge(ClassLoader.getSystemResourceAsStream("Pac-Man (U) [!].nes"));
		_nes = new NES();
		_nes.setCart(c);
		_cpu = _nes.getCPU();
		_cpuMemory = _cpu.getMemory();
		_ppu = _nes.getPPU();
		_ppuMemory = _ppu.getMemory(); 
		logger.info(c.toString());
	}
	
	@Command
	public String status() {
		StringBuilder sb = new StringBuilder();
		
		String cpuStatus = String.format("CPU - PC: %s SP: %s A: %s X: %s Y: %s Status: S %s V %s D %s I %s Z %s C %s",
				HexUtils.toHex(_cpu.getPC()),
				HexUtils.toHex(_cpu.getSP()),
				HexUtils.toHex(_cpu.getA()),
				HexUtils.toHex(_cpu.getX()),
				HexUtils.toHex(_cpu.getY()),
				_cpu.getNegativeFlag() ? 1 : 0,
				_cpu.getOverflowFlag() ? 1 : 0,
				_cpu.getDecimalMode() ? 1 : 0,
			    _cpu.getInterruptDisable() ? 1 : 0,
				_cpu.getZeroFlag() ? 1 : 0,
				_cpu.getCarryFlag() ? 1 : 0
		);
		
		String ppuStatus = String.format("PPU - Cycle: %d Coarse X: %d Fine X: %d Y: %d Hori: %d Vert: %d T: %s V: %s CTRL: %s MASK: %s STATUS: %s",
				_ppu.getCyclesSinceReset(),
				_ppu.getCoarseX(),
				_ppu.getFineXScroll(),
				_ppu.getCoarseY(),
				_ppu.getHorizontalScroll(),
				_ppu.getVerticalScroll(),
				HexUtils.toHex(_ppu.getTemporaryVRAMAddress()),
				HexUtils.toHex(_ppu.getCurrentVRAMAddress()),
				HexUtils.toHex(_ppu.getControlRegister()),
				HexUtils.toHex(_ppu.getMaskRegister()),
				HexUtils.toHex(_ppu.getStatusRegister())				
		);
		
		return cpuStatus + System.lineSeparator() + ppuStatus;
		
	}
	
	@Command
	public void reset() {
		_nes.reset();
	}	
	
	@Command
	public void step() {
		_nes.runStep();
	}
	
	@Command
	public void step(int steps_) {
		for(int i = 0; i < steps_; i++) {
			_nes.runStep();
		}
	}
	
	@Command
	public void quit() {
		System.exit(0);
	}
	
	@Command
	public void run() {
		while(true) {
			_nes.runStep();
		}
	}	
	
	@Command
	public String patterns() {
		byte[][][] patternTableMap = new byte[2][128][128];
		/* 
		 * For a given address in the pattern table (high order bits first):
		 * CBA9876543210
		 * HRRRRCCCCPTTT
		 * 
		 * H - hand (0 = left, 1 = right)
		 * R - row number		
		 * C - column
		 * P - plane (1 = upper, 0 = lower)
		 * T - Fine Y offset of tile
		 */		
		for(short addr = PPUMemory.PATTERN_TABLE_0_LOC; addr < PPUMemory.PATTERN_TABLE_1_LOC + PPUMemory.PATTERN_TABLE_SIZE; addr++) {			
			byte val = _ppuMemory.read(addr);
			int handIndex = (addr & (1 << 12)) == 0 ? 0 : 1; // bit  12
			int rowNumber = (addr >>> 8) & 0xF;              // bits 8-11 
			int colNumber = (addr >>> 4) & 0xF;              // bits 4-7
			boolean isLowerPlane = (addr & (1 << 3)) == 0;	 // bit  3		
			int tileOffset = addr & 0b111;                   // bits 0-2
			
			// Get x and y into pixel array from these:
			int x = colNumber << 3;
			int y = (rowNumber << 3) + tileOffset;						
			
			logger.debug("Got rowNumber {}, colNumber {}, handIndex {}, x {}, y {}, tile offset {} with value {} and address {}", new Object[] {
					rowNumber,
					colNumber,
					handIndex,
					x,
					y,
					tileOffset,
					HexUtils.toHex(val),
					HexUtils.toHex(addr)					
			});
			
			for(int i = 0; i < 8; i++) {
				byte bit = (byte) ( (val & (1 << 8 - i)) == 0 ? 0 : 1 );
				
				if(isLowerPlane) {
					bit <<= 1;
				}
				
				logger.debug("Copying bit value {} to handIndex {}, x {}, y {}", new Object[] {bit, handIndex, x + i, y});
				
				patternTableMap[handIndex][x + i][y] += bit; 
			}					
		}
		
		StringBuilder sb = new StringBuilder();
		for(int handIndex = 0; handIndex < 2; handIndex++) {
			for(int y = 0; y < 128; y++) {
				for(int x = 0; x < 128; x++) {
					int val = patternTableMap[handIndex][x][y];
					sb.append(val == 0 ? "." : val);
				}
				sb.append("\n");
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}

    public static void main(String[] args) throws IOException, UnableToLoadRomException {
        ShellFactory.createConsoleShell(">", "", new ConsoleDebugger()).commandLoop(); // and three.
    }

}
