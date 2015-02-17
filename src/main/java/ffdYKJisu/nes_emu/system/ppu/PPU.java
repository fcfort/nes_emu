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
	
    private final Register _controlRegister;
    private final Register _maskRegister;
    private final Register _statusRegister;
    private final Register _scrollRegister;
    private final Register _addressRegister;
    private final Register _dataRegister;
    
    private int _cyclesRun;
    private int _cyclesRunSinceReset;
    
    public PPU(NES nes_) {
    	_nes = nes_;
    	_memory = new PPUMemory(this);
    	
    	_controlRegister = new Register();
    	_maskRegister = new Register();      
        _statusRegister = new Register();
        _scrollRegister = new Register();
        _addressRegister = new Register();
        _dataRegister = new Register();
        
        _cyclesRun = 0;
        _cyclesRunSinceReset = 0;
    }

	public PPUMemory getPPUMemory() { return _memory; }

	// http://wiki.nesdev.com/w/index.php/PPU_power_up_state
	public void reset() {
    	_controlRegister.setByte((byte) 0);
    	_maskRegister.setByte((byte) 0);      
        _statusRegister.setByte((byte) 0);
        _scrollRegister.setByte((byte) 0);
        _addressRegister.setByte((byte) 0);
        _dataRegister.setByte((byte) 0);	
        
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
    
	
	public void write(short address_, byte val_) {
		switch(address_) {
		case 0x2000:
			_controlRegister.setByte(val_);
			break;
		case 0x2001:
			_maskRegister.setByte(val_);
		default:
			throw new UnsupportedOperationException();
		}
	}
    
}
