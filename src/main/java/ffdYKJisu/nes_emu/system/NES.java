package ffdYKJisu.nes_emu.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ffdYKJisu.nes_emu.system.cpu.CPU;
import ffdYKJisu.nes_emu.system.cpu.ICPU;
import ffdYKJisu.nes_emu.system.memory.CPUMemory;
import ffdYKJisu.nes_emu.system.memory.PPUMemory;
import ffdYKJisu.nes_emu.system.ppu.PPU;

/**
 * This will hold both the CPU and PPU objects and the Cartridge. This is to
 * facilitate passing state information between the two architectures. Also this
 * is needed to allow both the cpu and the ppu to simulate simultaneous
 * operation.
 * 
 * @author fe01106
 */
public class NES {

	private final Logger logger = LoggerFactory.getLogger(NES.class);

	private Cartridge cart;
	
	private final CPU _cpu;
	private final PPU _ppu;
	/** How many cycles the ppu runs for every cpu cycles */
	
	private static final double PPU_CPU_CYCLE_RATIO = 3;
	
	private Timing timing;

	private enum Timing {
		PAL, NTSC
	}

	public NES() {		
		timing = Timing.NTSC;
		_cpu = new CPU(this);
		_ppu = new PPU(this);
	}

	/**
	 * 
	 * @param numCycles
	 *            Cycles to run NES (in CPU cycles)
	 */
	public void emulateFor(long numCycles) {
		// int testRunLength = 200;
		// for(int i=0; i < testRunLength; i++) {
		int ppuCycles = (int) (numCycles / 3);
		// Pass data the cpu needs to the ppu and run the cpu
		// cpu.emulateFor(numCycles, ppu.getCpuData();
		for(long i = 0; i < numCycles; i++) {
			_cpu.runStep();
		}

		// Pass data the ppu needs to the ppu and run the ppu
		// ppu.emulateFor(ppuCycles, cpu.getPpuData());
		// }
	}
	
	public Cartridge getCart() { 
		return cart;
	}
	
	public void setCart(Cartridge cart) {
		this.cart = cart;
		_cpu.getCPUMemory().writeCartToMemory(cart);
	}

	public CPU getCPU() {
		return _cpu;
	}
	
	public CPUMemory getCPUMemory() {
		return _cpu.getCPUMemory();
	}

	public void reset() {
		_cpu.reset();
	}

	public void step() {
		_cpu.runStep();
	}

	public PPU getPPU() {
		return _ppu;
	}
}
