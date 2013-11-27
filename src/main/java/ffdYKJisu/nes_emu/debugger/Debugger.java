/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ffdYKJisu.nes_emu.debugger;


import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ffdYKJisu.nes_emu.system.NES;

/**
 * Controls interaction between cpu/nes and command line input.
 * This will be used for debugging the cpu core.
 * @author Administrator
 */
public class Debugger {
	
	private static final Logger logger = LoggerFactory.getLogger(Debugger.class);
	
	Console c;
	NES nes;
	BufferedReader in;
	
	public Debugger(NES nes) {
		this.nes = nes;
		
		c = System.console();
		if ( c == null ) {
			logger.error("Unable to create console");
		} else {
			InputStreamReader reader = new InputStreamReader(System.in);
			in = new BufferedReader(reader);
		}		
	}
	
	public void startConsole() {
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
			logger.error("Failed to read input from inputstream");
			return "";
		} 
	}
}
