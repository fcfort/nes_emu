/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ffdYKJisu.nes_emu.main;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ffdYKJisu.nes_emu.debugger.ConsoleDebugger;
import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.NES;

/**
 * 
 * @author fe01106
 */
public class Main {

	static final Logger logger = LoggerFactory.getLogger(Main.class);

	/**
	 * 
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {

		String romName = "Pac-Man (U) [!].nes";

		try {
			Cartridge cart = getCartridge(romName);
			NES nes = new NES();
			nes.setCart(cart);
			nes.reset();
			ConsoleDebugger d = new ConsoleDebugger(nes);
			d.startConsole();
		} catch (UnableToLoadRomException e) {
			logger.error("Failed to load rom {}", romName);
		} catch (IOException e) {
			logger.error("Failed to create console debugger");
		}

	}

	public static Cartridge getCartridge(String resourcePath)
			throws UnableToLoadRomException {
		InputStream romIs = Main.class.getClassLoader().getResourceAsStream(
				resourcePath);

		if (romIs == null) {
			logger.error("Failed to load cartridge");
			throw new UnableToLoadRomException();
		}

		Cartridge cart = new Cartridge(romIs);

		return cart;
	}
}
