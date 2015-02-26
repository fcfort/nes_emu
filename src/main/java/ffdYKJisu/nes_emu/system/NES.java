package ffdYKJisu.nes_emu.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ffdYKJisu.nes_emu.exceptions.BankNotFoundException;
import ffdYKJisu.nes_emu.screen.Image;
import ffdYKJisu.nes_emu.system.cpu.CPU;
import ffdYKJisu.nes_emu.system.memory.CPUMemory;
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
	private final Image _image;
	/** How many cycles the ppu runs for every cpu cycles */
	
	private static final double PPU_CPU_CYCLE_RATIO = 3;
	
	private static final int IMAGE_WIDTH = 256;
	private static final int IMAGE_HEIGHT = 240;
	
	private Timing timing;

	private enum Timing {
		PAL, NTSC
	}

	public NES() {		
		timing = Timing.NTSC;
		_image = new Image(IMAGE_WIDTH, IMAGE_HEIGHT);
		_cpu = new CPU(this);
		_ppu = new PPU(this);
		_image.render();
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
		// TODO: fix exception handling
		try {
			_cpu.getMemory().writeCartToMemory(cart);
			_ppu.getMemory().writeCartToMemory(cart);
		} catch (BankNotFoundException e) {
			throw new RuntimeException(e);
		}		
	}

	public CPU getCPU() { return _cpu; }
	public PPU getPPU() { return _ppu; }
	public Image getImage() { return _image; }
	
	public CPUMemory getCPUMemory() {
		return _cpu.getMemory();
	}

	public void reset() {
		_cpu.reset();
	}

	public void step() {
		_cpu.runStep();
	}

	
}
