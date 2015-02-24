/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ffdYKJisu.nes_emu.system.ppu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ffdYKJisu.nes_emu.domain.Register;
import ffdYKJisu.nes_emu.screen.Image;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.memory.PPUMemory;
import ffdYKJisu.nes_emu.util.UnsignedShorts;

/**
 *  Controls all PPU actions and holds object PPUMemory. Largely a passive
 * class. State information is passed to the class when the CPU is done working.
 * 
 * Based on http://wiki.nesdev.com/w/index.php/The_skinny_on_NES_scrolling
 */
public class PPU {
	
	private static Logger logger = LoggerFactory.getLogger(PPU.class);
	
    private static final int REGISTER_SIZE = 8;
    private static final int TILE_SIZE = 8;
    private static final int TILES_PER_SCANLINE = 32;
    private static final int MAX_SCANLINE = 261;
    private static final int CYCLES_PER_SCANLINE = 341;
    private static final int OAM_SIZE = 256;
    
    /* http://wiki.nesdev.com/w/index.php/PPU_registers */
    private static final short PPUCTRL_ADDRESS = 0x2000;
    private static final short PPUMASK_ADDRESS = 0x2001;
    private static final short PPUSTATUS_ADDRESS = 0x2002;
    private static final short OAMADDR_ADDRESS = 0x2003;
    private static final short OAMDATA_ADDRESS = 0x2004;
    private static final short PPUSCROLL_ADDRESS = 0x2005;
    private static final short PPUADDR_ADDRESS = 0x2006;
    private static final short PPUDATA_ADDRESS = 0x2007;
    private static final short OAMDMA_ADDRESS = 4014;
    
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
    
    private int _patternTableIndex;
    private int _nameTableIndex;
    
    private byte[] _objectAttributeMemory;
    
    private short _v;
    private short _t;
    private byte _fineXScroll;
    private boolean _isFirstWrite;
    
    public PPU(NES nes_) {
    	logger.info("Initializing PPU");
    	_nes = nes_;
    	_image = _nes.getImage();
    	_memory = new PPUMemory(this);
    	
    	_controlRegister = new Register(); // 0x2000
    	_maskRegister = new Register(); // 0x2001    
        _statusRegister = new Register(); // 0x2002
        _scrollRegister = new Register(); // 0x2005
        _addressRegister = new Register(); // 0x2006
        _dataRegister = new Register(); // 0x2007
        
        _objectAttributeMemory = new byte[OAM_SIZE];
        
        _frame = 0;
        _cyclesRun = 0;
        _cyclesRunSinceReset = 0;
        _horizontalScroll = 0;
        _verticalScroll = 0;
        
        _isFirstWrite = true;
        
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
    	return _memory.read(calculateAttributeAddress());
    }
    
    private short calculateAttributeAddress() {
    	 return (short) (0x23C0 | (_v & 0x0C00) | ((_v >> 4) & 0x38) | ((_v >> 2) & 0x07));
    }
    
    private byte fetchPatternTableBitmap() {
    	return 0;
    }
    
    public short getTemporaryVRAMAddress() { return _t; }
	public short getCurrentVRAMAddress() { return _v; }    
    public boolean isFirstWrite() { return _isFirstWrite; }    
    public byte getFineXScroll() { return _fineXScroll; }
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
			case PPUSTATUS_ADDRESS:
				_isFirstWrite = true;
				return _statusRegister.getByte();				
			default:
				throw new UnsupportedOperationException();
		}
	}    
	
	public void write(short address_, byte val_) {
		switch(address_) {
			case PPUCTRL_ADDRESS:
				_controlRegister.setByte(val_);
				/* t: ...BA.. ........ = d: ......BA */
				short destBitMask = ~(0b11 << 10);
				byte srcBitMask = 0b11;
				_t = (short) ((_t & destBitMask) | ((val_ & srcBitMask) << 10)); 				
				break;
			case PPUMASK_ADDRESS:
				_maskRegister.setByte(val_);
				break;
			case PPUSCROLL_ADDRESS:
				_scrollRegister.setByte(val_);
				if(_isFirstWrite) {
					/* t: ....... ...HGFED = d: HGFED... */
					_t = (short) ((_t & ~0b1_1111) | ((val_ & 0b1111_1000) >>> 3));
					 /* x:              CBA = d: .....CBA */
					_fineXScroll = (byte) (val_ & 0b111);
				} else {
					/* t: CBA..HG FED..... = d: HGFEDCBA */
					// upper write to t
					_t = (short) (_t & ~(0b111 << 12) | ((val_ & 0b111) << 12)); 
					// lower write to t					
					_t = (short) ((_t & ~(0b1_1111 << 5)) | ((val_ & 0b1111_1000) << 2));
				}
				_isFirstWrite ^= true; // toggle
				break;
			case PPUADDR_ADDRESS:
				_addressRegister.setByte(val_);
				if(_isFirstWrite) {
					/* t: .FEDCBA ........ = d: ..FEDCBA */
					_t = (short) ((_t & ~(0b11_1111 << 8)) | ((val_ & 0b11_1111) << 8));
					/* t: X...... ........ = 0 */
					_t &= ~(1 << 14);
				} else {
					/* t: ....... HGFEDCBA = d: HGFEDCBA */
					_t = (short) ((_t & ~0b1111_1111) | val_ & 0b1111_1111);
				    /* v                   = t */
					_v = _t;
				}
				_isFirstWrite ^= true; // toggle
				break;
			default:
				throw new UnsupportedOperationException();
		}
	}

}
