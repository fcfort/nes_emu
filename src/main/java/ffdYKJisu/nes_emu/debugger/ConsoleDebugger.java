/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ffdYKJisu.nes_emu.debugger;


import java.io.IOException;
import java.io.PrintWriter;

import jline.console.ConsoleReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ffdYKJisu.nes_emu.system.NES;

/**
 * Controls interaction between cpu/nes and command line input.
 * This will be used for debugging the cpu core.
 * @author Administrator
 */
public class ConsoleDebugger {
	
	private static final Logger logger = LoggerFactory.getLogger(ConsoleDebugger.class);
	
	ConsoleReader c;
	NES nes;
	
	public void usage() {
		System.out.println("Welcome to the NES debugger." + 
				" Enter ? for help.");
		System.out.println("s step");
	}
	
	public ConsoleDebugger(NES nes) throws IOException {
		logger.info("Starting console debugger");
		this.nes = nes;
		c = new ConsoleReader();
        c.setPrompt("prompt> ");	
	}
	
	public void startConsole() throws IOException {
			String line;
            PrintWriter out = new PrintWriter(c.getOutput());

            while ((line = c.readLine()) != null) {
                out.println("======>\"" + line + "\"");                
                out.flush();

                // If we input the special word then we will mask
                // the next line.
                
                if(line.equalsIgnoreCase("s")) {
                	nes.step();
                }
                else if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("exit")) {
                    break;
                }
		}
	}

}
