/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ffdYKJisu.nes_emu.system;

/**
 *  Controls all PPU actions and holds object PPUMemory. Largely a passive
 * class. State information is passed to the class when the CPU is done working.
 * @author fe01106
 */
class PPU {
    Cartridge cart;
    PPUMemory memory;
    
    void setCart(Cartridge c) {
        this.cart = c;
    }
	
	void initialize() { 
		//load
	}
}