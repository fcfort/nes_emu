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
import ffdYKJisu.nes_emu.util.HexUtils;

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
    
    public PPUMemory getMemory() { return _memory; }
    
    public void runStep() {    	
    	// Idle cycle at the start of every scanline    	
    	
    	if(isRenderingEnabled()) {
    		byte nameTableData = _memory.read(fetchNameTableByte());
    		byte attributeData = _memory.read(fetchAttributeTableByte());
    		
    	}
    	
    	if(_verticalScroll >= 0 && _verticalScroll < 240 ) {
    		if(_horizontalScroll != 0 && _horizontalScroll % 8 == 0) {
    			
    		}
    	}       
    	
    	if(_verticalScroll == MAX_SCANLINE) {

    	} else if(_verticalScroll >= 0 && _verticalScroll <= 239) {
    		
    	}
    	
    	if(isRenderingEnabled() && _horizontalScroll == 256) {
    		incrementY();
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
    
	private void incrementY() {
		if ((_v & 0x7000) != 0x7000) {  // if fine Y < 7
			_v += 0x1000;               // increment fine Y
		} else {
			_v &= ~0x7000;              // fine Y = 0
			int y = (_v & 0x03E0) >> 5; // let y = coarse Y
			if (y == 29) {
				y = 0;                  // coarse Y = 0
				_v ^= 0x0800;           // switch vertical nametable
			} else if (y == 31) {
				y = 0;                  // coarse Y = 0, nametable not switched
			} else {
				y += 1;                 // increment coarse Y
			}
			_v = (short) ((_v & ~0x03E0) | (y << 5)); // put coarse Y back into v
		}
	}
	
	private void incrementCoarseX() {
		if ((_v & 0x001F) == 31) { // if coarse X == 31
			_v &= ~0x001F;         // coarse X = 0
			_v ^= 0x0400;          // switch horizontal nametable
		} else {
			_v += 1;               // increment coarse X
		}
	}
    
    private boolean isRenderingEnabled() {
    	return isBackgroundRenderingEnabled() || isSpriteRenderingEnabled();
    }
    
    private boolean isBackgroundRenderingEnabled() {
    	return _maskRegister.getValue(3);
    }
    
    private boolean isSpriteRenderingEnabled() {
    	return _maskRegister.getValue(2);
    }
    
    private byte fetchNameTableByte() {
    	return 0;
    }
    
    private byte fetchAttributeTableByte() {
    	return _memory.read(calculateAttributeAddress());
    }
    
    private short calculateTileAddress() {
    	return (short) (0x2000 | ( _v & 0x0FFF));
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
				throw new UnsupportedOperationException("Tried to read PPU address " + HexUtils.toHex(address_));
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
