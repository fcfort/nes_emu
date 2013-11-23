/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ffdYKJisu.nes_emu.main;


import java.io.File;

/**
 *
 * @author fe01106
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
		String romName = "Pac-Man (U) [!].nes";
		NES nes = new NES();
		nes.loadRom(new File(romName));
		nes.initialize();
		nes.emulateFor(20);
		/*
		d.loadRom(romName);
		d.startConsole();
		 */
		/*
        
        File cart = new File(romName);
        NES nes = new NES();
        nes.loadRom(cart);
        nes.initialize();
        nes.emulateFor(100);
		 */
		
        /*
        CPU cpu = new CPU();
        cpu.loadRom(cart);
        cpu.init();
           */
        // cpu.emulateFor(100);
    // uByte test harness
        /*
        uByte temp;
        for ( int i = 0; i < 0x100; i++) {
            temp = new uByte(i);
            // System.out.print(temp + " ");
            System.out.print(temp + "->" + temp.negativeValue() + " neg? "
                    + temp.isNegative() + " as signed byte " + temp.toSigned()
                    + "\n");
        }
        */
        /*System.out.println("uByte test harness");
        uByte a = new uByte(2323);
        for(int i =0; i > -1000; i--) {
        a.set(i);
        System.out.print(a.toString() + " ");
        }
        */
    }
}
