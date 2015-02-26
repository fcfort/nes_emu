package ffdYKJisu.nes_emu.system;

import org.junit.Before;
import org.junit.Test;

import ffdYKJisu.nes_emu.exceptions.BankNotFoundException;
import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;

public class CartridgeTest {

	Cartridge _c;
	
	@Before
	public void initialize() throws UnableToLoadRomException {
		_c = new Cartridge(ClassLoader.getSystemResourceAsStream("Pac-Man (U) [!].nes")); 
	}
	
	@Test
	public void testLoad() throws BankNotFoundException {
		_c.get16PRGBank(0);	
	}

}
