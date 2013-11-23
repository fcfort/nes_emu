/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ffdYKJisu.nes_emu.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import ffdYKJisu.nes_emu.system.NES;

/**
 * 
 * @author fe01106
 */
public class Main {

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        String romName = "Pac-Man (U) [!].nes";

        InputStream romIs = Main.class.getClass().getClassLoader()
                .getResourceAsStream(romName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(romIs));

        NES nes = new NES();
        nes.loadRom(romIs);
        nes.initialize();
        nes.emulateFor(20);
        /*
         * d.loadRom(romName); d.startConsole();
         */
        /*
         * 
         * File cart = new File(romName); NES nes = new NES();
         * nes.loadRom(cart); nes.initialize(); nes.emulateFor(100);
         */

        /*
         * CPU cpu = new CPU(); cpu.loadRom(cart); cpu.init();
         */
        // cpu.emulateFor(100);
        // uByte test harness
        /*
         * uByte temp; for ( int i = 0; i < 0x100; i++) { temp = new uByte(i);
         * // System.out.print(temp + " "); System.out.print(temp + "->" +
         * temp.negativeValue() + " neg? " + temp.isNegative() +
         * " as signed byte " + temp.toSigned() + "\n"); }
         */
        /*
         * System.out.println("uByte test harness"); uByte a = new uByte(2323);
         * for(int i =0; i > -1000; i--) { a.set(i);
         * System.out.print(a.toString() + " "); }
         */
    }
}
