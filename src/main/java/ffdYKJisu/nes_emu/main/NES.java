/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ffdYKJisu.nes_emu.main;

import java.io.File;

/**
 * This will hold both the CPU and PPU objects and the Catridge. 
 * This is to facilitate passing
 * state information between the two architectures. Also this is needed to allow
 * both the cpu and the ppu to simulate simultaneous operation. 
 * @author fe01106
 */
public class NES {

    private Cartridge cart;
    private CPU cpu;
    private PPU ppu;
    /** How many cycles the ppu runs for every cpu cycles */
    private final double PpuCpuRatio = 3;
    private Timing timing;

    void initialize() {
        cpu = new CPU();
        ppu = new PPU();
        this.setCart(cart);
        cpu.initialize();
        ppu.initialize();
    }

    private enum Timing {

        PAL,
        NTSC
    }

    NES() {
        timing = Timing.NTSC;
    }

    /** 
     * 
     * @param numCycles Cycles to run NES (in CPU cycles)
     */
    void emulateFor(long numCycles) {
        //int testRunLength = 200;
        //for(int i=0; i < testRunLength; i++) {
        int ppuCycles = (int) (numCycles / 3);
        // Pass data the cpu needs to the ppu and run the cpu 
        // cpu.emulateFor(numCycles, ppu.getCpuData();
        cpu.emulateFor(numCycles);

    // Pass data the ppu needs to the ppu and run the ppu 
    // ppu.emulateFor(ppuCycles, cpu.getPpuData());
    //}
    }

    void loadRom(File cart) {
        this.cart = new Cartridge(cart);
    }

    void setCart(Cartridge cart) {
        cpu.setCart(cart);
        ppu.setCart(cart);
    }

    CPU getCpu() {
        return this.cpu;
    }
}
