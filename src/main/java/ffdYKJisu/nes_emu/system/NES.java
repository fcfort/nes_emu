/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ffdYKJisu.nes_emu.system;

import java.io.File;
import java.io.InputStream;

import org.apache.log4j.Logger;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.cpu.CPU;

/**
 * This will hold both the CPU and PPU objects and the Cartridge. 
 * This is to facilitate passing
 * state information between the two architectures. Also this is needed to allow
 * both the cpu and the ppu to simulate simultaneous operation. 
 * @author fe01106
 */
public class NES {

    private static final Logger logger = Logger.getLogger(NES.class); 
    
    private Cartridge cart;
    private CPU cpu;
    private PPU ppu;
    /** How many cycles the ppu runs for every cpu cycles */
    private static final double PPU_CPU_CYCLE_RATIO = 3;
    private Timing timing;

    public void initialize() {
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

    public NES() {
        timing = Timing.NTSC;
    }

    /** 
     * 
     * @param numCycles Cycles to run NES (in CPU cycles)
     */
    public void emulateFor(long numCycles) {
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

    public void loadRom(InputStream cart) {
        try {
            this.cart = new Cartridge(cart);
        } catch (UnableToLoadRomException e) {
            logger.warn("Unable to load cart");
        }
    }
    
    public void loadRom(File cart) {
        try {
            this.cart = new Cartridge(cart);
        } catch (UnableToLoadRomException e) {
            logger.warn("Unable to load cart " + cart);
        } 
    }

    public void setCart(Cartridge cart) {
        cpu.setCart(cart);
        ppu.setCart(cart);
    }

    public CPU getCpu() {
        return this.cpu;
    }
}
