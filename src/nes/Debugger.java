/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nes;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controls interaction between cpu/nes and command line input.
 * This will be used for debugging the cpu core.
 * @author Administrator
 */
public class Debugger {
	Console c;
	NES nes;
	BufferedReader in;
	
	Debugger( ) {
		c = System.console();
		if ( c == null ) {
			System.err.println("No Console");
			// System.exit(1);
		} else {
			InputStreamReader reader = new InputStreamReader(System.in);
			in = new BufferedReader(reader);
		}
		nes = new NES();		
	}

	void loadRom(String romName) {
		File cart = new File(romName);
		this.nes.loadRom(cart);
	}

	void startConsole() {
        nes.initialize();
		boolean debuggerRunning = true;
		System.out.println("Welcome to the NES debugger." + 
			" Enter ? for help.");
		while(debuggerRunning) {
			String s = this.readString();
			System.out.println("I saw: " + s);
			if ( s.equals("q") )  {
				debuggerRunning = false;
			}
		}
	}
	
	private String readString() {
		try {
			return in.readLine();
		} catch (IOException ex) {
			Logger.getLogger(Debugger.class.getName()).log(
				Level.SEVERE, null, ex);
			return "caw";
		} 
	}
}
