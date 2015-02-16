/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ffdYKJisu.nes_emu.system.ppu;

import java.util.BitSet;

import ffdYKJisu.nes_emu.domain.Register;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.memory.PPUMemory;

/**
 *  Controls all PPU actions and holds object PPUMemory. Largely a passive
 * class. State information is passed to the class when the CPU is done working.
 * @author fe01106
 */
public class PPU {
    private static final int REGISTER_SIZE = 8;
    
    private final NES _nes;
	private final PPUMemory _memory;
	
    private final BitSet _controlRegister;
    private final boolean[] _control;
    private final Register _controlRegister2;
    private final BitSet _maskRegister;
    private final BitSet _statusRegister;
    private final BitSet _scrollRegister;
    private final BitSet _addressRegister;
    private final BitSet _dataRegister;
    
    private int _cyclesRun;
    private int _cyclesRunSinceReset;
    
    public PPU(NES nes_) {
    	_nes = nes_;
    	_memory = new PPUMemory(this);
    	
    	_controlRegister = new BitSet(REGISTER_SIZE);
    	_control = new boolean[REGISTER_SIZE];
    	_controlRegister2 = new Register();
    	_maskRegister = new BitSet(REGISTER_SIZE);      
        _statusRegister = new BitSet(REGISTER_SIZE);
        _scrollRegister = new BitSet(REGISTER_SIZE);
        _addressRegister = new BitSet(REGISTER_SIZE);
        _dataRegister = new BitSet(REGISTER_SIZE);
        
        _cyclesRun = 0;
        _cyclesRunSinceReset = 0;
    }

	public PPUMemory getPPUMemory() { return _memory; }

	// http://wiki.nesdev.com/w/index.php/PPU_power_up_state
	public void reset() {
    	_controlRegister.clear();
    	_maskRegister.clear();      
        _statusRegister.clear();
        _scrollRegister.clear();
        _addressRegister.clear();
        _dataRegister.clear();	
        
        _cyclesRunSinceReset = 0;
	}
	
	public byte read(short address_) {
		switch(address_) {
		case 0x2000:
			break;
		default:
				throw new UnsupportedOperationException();
		}
	}
    
	
	public byte write(short address_, byte val_) {
		switch(address_) {
		case 0x2000:
			_controlRegister2.setByte(val_);
		}
	}
    
}
