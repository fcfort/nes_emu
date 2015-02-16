/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ffdYKJisu.nes_emu.system.ppu;

import java.util.BitSet;

import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.memory.PPUMemory;

/**
 *  Controls all PPU actions and holds object PPUMemory. Largely a passive
 * class. State information is passed to the class when the CPU is done working.
 * @author fe01106
 */
public class PPU {
    private static final int REGISTER_SIZE = 8;
    
	private final PPUMemory _memory;    
    private final BitSet _controlRegister1; 
    private final BitSet _controlRegister2;
    private final BitSet _statusRegister;
    private final NES _nes;
    
    public PPU(NES nes_) {
    	_nes = nes_;
    	_memory = new PPUMemory(this);
    	_controlRegister1 = new BitSet(REGISTER_SIZE);
        _controlRegister2 = new BitSet(REGISTER_SIZE);      
        _statusRegister = new BitSet(REGISTER_SIZE);
    }

	public PPUMemory getPPUMemory() { return _memory; }

	public byte read(short address) {
		// TODO Auto-generated method stub
		return 0;
	}
    
}
