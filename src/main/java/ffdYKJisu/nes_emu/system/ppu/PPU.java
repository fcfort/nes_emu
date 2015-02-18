/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ffdYKJisu.nes_emu.system.ppu;

import java.util.BitSet;

import org.apache.commons.lang.BitField;

import ffdYKJisu.nes_emu.domain.Register;
import ffdYKJisu.nes_emu.screen.Image;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.memory.PPUMemory;

/**
 *  Controls all PPU actions and holds object PPUMemory. Largely a passive
 * class. State information is passed to the class when the CPU is done working.
 * @author fe01106
 */
public class PPU {
    private static final int REGISTER_SIZE = 8;
    private static final int TILE_SIZE = 8;
    private static final int TILES_PER_SCANLINE = 32;
    private static final int MAX_SCANLINE = 261;
    private static final int CYCLES_PER_SCANLINE = 341;
    
    private final NES _nes;
	private final PPUMemory _memory;
	private final Image _image;
	
    private final Register _controlRegister;
    private final Register _maskRegister;
    private final Register _statusRegister;
    private final Register _scrollRegister;
    private final Register _addressRegister;
    private final Register _dataRegister;
    
    private int _frame;
    private int _horizontalScroll;
    private int _verticalScroll;
    
    private int _cyclesRun;
    private int _cyclesRunSinceReset;  
    
    
    public PPU(NES nes_) {
    	_nes = nes_;
    	_image = _nes.getImage();
    	_memory = new PPUMemory(this);
    	
    	_controlRegister = new Register(); // 0x2000
    	_maskRegister = new Register(); // 0x2001    
        _statusRegister = new Register(); // 0x2002
        _scrollRegister = new Register(); // 0x2005
        _addressRegister = new Register(); // 0x2006
        _dataRegister = new Register(); // 0x2007
        
        _frame = 0;
        _cyclesRun = 0;
        _cyclesRunSinceReset = 0;
        _horizontalScroll = 0;
        _verticalScroll = 0;
    }
    
    public void runStep() {
    	
    	// Idle cycle at the start of every scanline
    	if(_horizontalScroll == 0) {
    		return;
    	}       
    	
    	if(_verticalScroll == MAX_SCANLINE) {

    	} else if(_verticalScroll >= 0 && _verticalScroll <= 239) {
    		
    	}
    	
    	// Increment counters
    	_horizontalScroll++;
    	
    	if(_horizontalScroll > CYCLES_PER_SCANLINE) {
    		_verticalScroll++;
    		_horizontalScroll = 0;
    	}
    	
    	if(_verticalScroll > MAX_SCANLINE) {
    		_verticalScroll = 0;
    		_frame++;
    	}
    	_cyclesRun++;
    	_cyclesRunSinceReset++;
    }
    
    private byte fetchNameTableByte() {
    	return 0;
    }
    
    private byte fetchAttributeTableByte() {
    	return 0;
    }
    
    private byte fetchPatternTableBitmap() {
    	return 0;
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
			case 0x2002:
				return _statusRegister.getByte();
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
