package ffdYKJisu.nes_emu.system.cpu;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.cartridge.Cartridge;
import ffdYKJisu.nes_emu.system.cartridge.CartridgeFactory;
import ffdYKJisu.nes_emu.system.memory.Addressable;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestCPURunning {

	NES _n;
	CPU _c;
	Addressable _mem;
	
	@Before
	public void initialize() throws UnableToLoadRomException {
		Cartridge c =
						new CartridgeFactory()
										.fromInputStream(ClassLoader.getSystemResourceAsStream("Pac-Man (U) [!].nes"));
		NES _nes = new NES(c);
		_c = _nes.getCPU();		
		_mem = _c.getMemory();
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
