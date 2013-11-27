/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ffdYKJisu.nes_emu.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ffdYKJisu.nes_emu.debugger.Debugger;
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
        String path = "src/main/resources/";
        String romName = "Pac-Man (U) [!].nes";

        ClassLoader l = Main.class.getClassLoader();
        InputStream pacmanIs = l.getResourceAsStream("pacman.nes");       
        
        Cartridge pacmanCart = null;
		try {
			pacmanCart = new Cartridge(pacmanIs);
		} catch (UnableToLoadRomException e) {
			logger.error("Failed to load cartridge");
			System.exit(1);
		}
        
        NES nes = new NES(pacmanCart);
        nes.loadRom(new File(romName));
        Debugger d = new Debugger(nes);
        d.startConsole();
        }
}
