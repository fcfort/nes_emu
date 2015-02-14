package ffdYKJisu.nes_emu.system.cpu;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.memory.CPUMemory;

public class TestCPURunning {

	NES _n;
	CPU _c;
	CPUMemory _mem; 
	
	@Before
	public void initialize() throws UnableToLoadRomException {
		Cartridge c = new Cartridge(ClassLoader.getSystemResourceAsStream("Pac-Man (U) [!].nes"));
		_mem = new CPUMemory();
		_c = new CPU(_mem);
		_mem.writeCartToMemory(c);
		_c.reset();
	}
	
	@Test
	public void testOneStepSEI() {
		_c.runStep();
		assertTrue(_c.getInterruptDisable());
	}
	
	@Test
	public void testTwoStepSEI_CLD() {
		_c.runStep();
		_c.runStep();
		assertTrue(!_c.getDecimalMode());
	}

	@Test
	public void testThreeStepSEI_CLD_LDA() {
		runSteps(3);
		assertTrue(!_c.getDecimalMode());
		assertEquals(0, _c.getA());
	}
	
	private void runSteps(int steps_) {
		if(steps_ == 0) { return; }
		_c.runStep();
		runSteps(steps_ - 1);
	}

}
