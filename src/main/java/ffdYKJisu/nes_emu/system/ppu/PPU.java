/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ffdYKJisu.nes_emu.system.ppu;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ffdYKJisu.nes_emu.domain.Register;
import ffdYKJisu.nes_emu.screen.Image;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.memory.PPUMemory;
import ffdYKJisu.nes_emu.util.HexUtils;
import ffdYKJisu.nes_emu.util.UnsignedShorts;

/**
 * Controls all PPU actions and holds object PPUMemory. Largely a passive
 * class. State information is passed to the class when the CPU is done working.
 * 
 * Based on http://wiki.nesdev.com/w/index.php/The_skinny_on_NES_scrolling
 */
public class PPU {
	
	private static Logger logger = LoggerFactory.getLogger(PPU.class);
	
    private static final int REGISTER_SIZE = 8;
    private static final int TILE_SIZE = 8;
    private static final int TILES_PER_SCANLINE = 32;
    private static final int LAST_VISIBLE_SCANLINE = 239;
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
    
    private byte lastNametableValue;
    private byte lastAttributeTableValue;
    private byte lastLowBackgroundTile;
    private byte lastHighBackgroundTile;
    
    
    private byte[] _objectAttributeMemory;
    
    private short _v;
    private short _t;
    private byte _fineXScroll;
    private boolean _isFirstWrite;
    
    private boolean _nmiOccurred;
    private boolean _nmiOutput;
    
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
        
    public int muxPixel(byte fineXScroll_, int attributeBits_, byte lowBackground_, byte highBackground_) {    	
    	//byte bgBit = lowBackground_ & 1 << fineXScroll_;
    	//attributeBits << 3
    	int bgValue = (highBackground_ << 1) | (lowBackground_);
    	
    	if(bgValue == 0) {
    		return 0x00_00_00;
    	} else {
    		return 0xFF_FF_FF;
    	}
    }

    /** 
     * At dot 257:
     * hori(v) = hori(t) 
     * v: ....F.. ...EDCBA = t: ....F.. ...EDCBA
     */
    public void copyHorizontalTtoV() {
    	short mask = 1 << 10 | 0b1_1111;
    	_v = (short) ((_v & ~mask) | (_t & mask));
    }
    
    /**
     * During dots 280 to 304 of the pre-render scanline (end of vblank):
	 * If rendering is enabled, at the end of vblank, shortly after the horizontal bits 
	 * are copied from t to v at dot 257, the PPU will repeatedly copy the vertical bits 
	 * from t to v from dots 280 to 304, completing the full initialization of v from t:
	 * v: IHGF.ED CBA..... = t: IHGF.ED CBA.....
     */
    public void copyVerticalTtoV() {
    	// TODO: move mask copy to its own function?
    	short mask = 0b111_1011_1110_0000;
    	_v = (short) ((_v & ~mask) | (_t & mask)); 
    }  
    
    private void sleep(int seconds) {
    	try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void runStep() {
    	if(isRenderingEnabled()) {    		    	
    		    		
    		if(_verticalScroll == 0) {
    			
    		}
    		
    		if(_horizontalScroll == 0) {
    			_fineXScroll = 0;
    		}
    		
    		if(_horizontalScroll == 256) {
    			incrementY();
	    	}
    		
    		if(_horizontalScroll == 257) {
    			copyHorizontalTtoV();
    		}
    		
    		if(_horizontalScroll >= 328 || _horizontalScroll <= 256) {    			
    			if(_horizontalScroll % 8 == 0 && _horizontalScroll != 0) {
    				lastNametableValue = fetchNametableByte();
					lastAttributeTableValue = fetchAttributeTableByte();    			
					lastLowBackgroundTile = fetchLowBackgroundByte();
					lastHighBackgroundTile = fetchHighBackgroundByte();
					logger.info("For coarse x {}, fine X {}, read nametable {} attr {} low bg {} high bg {}", new Object[] {
							getCoarseX(),
							getFineXScroll(),
							HexUtils.toHex(lastNametableValue),
							HexUtils.toHex(lastAttributeTableValue),
							HexUtils.toHex(lastLowBackgroundTile),
							HexUtils.toHex(lastHighBackgroundTile)
					});
					

    			}    			
    		}
    		
    		if(_verticalScroll <= LAST_VISIBLE_SCANLINE || _verticalScroll == MAX_SCANLINE) {
				int attributeByteOffset = ((getCoarseY() % 2) << 1)| (getCoarseX() % 2) << 1;
				
				int attributeBits = ((lastAttributeTableValue & (0b11 << attributeByteOffset)) >> attributeByteOffset) & 0b11;
				
				
				logger.info("Got low bg {}, high bg {}, name table {}", new Object[] {lastLowBackgroundTile, lastHighBackgroundTile, lastNametableValue});
				int pixel = muxPixel(getFineXScroll(), lastAttributeTableValue, lastLowBackgroundTile, lastHighBackgroundTile);
				logger.info("Got loopy v {}, loopy t {}, frame {}", new Object[] {HexUtils.toHex(_v), HexUtils.toHex(_t), _frame});  
				
				if(_verticalScroll != MAX_SCANLINE) {
					_image.setPixel(_horizontalScroll, _verticalScroll, pixel);
				}
    		}
    		
    		if(_verticalScroll == MAX_SCANLINE) {
    			if(_horizontalScroll >= 280 && _horizontalScroll <= 304) {
    				copyVerticalTtoV();
    			}
    		}
    		
    		if(_horizontalScroll >= 328 || _horizontalScroll <= 256) {    			
    			if(_horizontalScroll % 8 == 0 && _horizontalScroll != 0) {
    				incrementCoarseX();
    				_fineXScroll = 0;
    			}
    		}
    		
			incrementFineX();
    	}
    		
    	// Increment counters
    	_horizontalScroll++;
    	
    	if(_horizontalScroll > CYCLES_PER_SCANLINE) {    		
    		_verticalScroll++;
    		logger.info("Now on vertical scroll {}", _verticalScroll);
    		_horizontalScroll = 0;
    	}
    	
    	// set vblank
    	if(_verticalScroll == LAST_VISIBLE_SCANLINE + 1 && _horizontalScroll == 1) {
    		setNMIOccurred(true);

    	}
    	
    	// clear vblank
    	if(_verticalScroll == MAX_SCANLINE && _horizontalScroll == 1) {
    		setNMIOccurred(false);

    	}
    	
    	if(_verticalScroll > MAX_SCANLINE) {
    		_verticalScroll = 0;
    		_frame++;
    		_image.render();
    	}
    	
    	_cyclesRun++;
    	_cyclesRunSinceReset++;
    }   

	private void setNMIOccurred(boolean val_) {
		logger.info("NMI occurred status set to {}", val_);
		_statusRegister.setBit(7, val_);
		
		if(val_ && isNMIEnabled()) {
			logger.info("Generating NMI from PPU");
			nonMaskableInterrupt();
		}
	}
	
	private boolean isNMIEnabled() {
		return _controlRegister.getBit(7);
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
	
	private void incrementFineX() {		
		_fineXScroll++;
		
		if(_fineXScroll == 8) {
			_fineXScroll = 0;
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
    
	private void nonMaskableInterrupt() {
		_nes.getCPU().nonMaskableInterrupt();
	}
	
	public boolean isRenderingEnabled() {
    	return isBackgroundRenderingEnabled() || isSpriteRenderingEnabled();
    }
    
	public boolean isBackgroundRenderingEnabled() {
    	return _maskRegister.getBit(3);
    }
    
	public boolean isSpriteRenderingEnabled() {
    	return _maskRegister.getBit(2);
    }
    
    private byte fetchNametableByte() {
    	return _memory.read(calculateTileAddress());
    }
    
    private byte fetchAttributeTableByte() {
    	return _memory.read(calculateAttributeAddress());
    }
    
    private byte fetchLowBackgroundByte() {
    	return _memory.read(calculatePatternTableAddress(lastNametableValue, 0));
    }
    
    private byte fetchHighBackgroundByte() {
    	return _memory.read(calculatePatternTableAddress(lastNametableValue, 1));
    }    
    
    private short calculatePatternTableAddress(byte nametableByte_, int offset_) {
    	int backgroundHandBit = _controlRegister.getBit(4) ? 1 : 0;
    	
    	short patternTableAddress = (short) ( 
    			(backgroundHandBit << 12) |
    			((nametableByte_ << 3) & 0xFF) |
    			getFineXScroll()    			
    	);
    	
    	return (short) (patternTableAddress + offset_);
    }
    
    private short calculateTileAddress() {
    	return (short) (0x2000 | ( _v & 0x0FFF));
    }
    
    private short calculateAttributeAddress() {
    	 return (short) (0x23C0 | (_v & 0x0C00) | ((_v >> 4) & 0x38) | ((_v >> 2) & 0x07));
    }
    
    public short getTemporaryVRAMAddress() { return _t; }
	public short getCurrentVRAMAddress() { return _v; }  
	public void setTemporaryVRAMAddress(short t_) { _t = t_; }
	public void setCurrentVRAMAddress(short v_) { _v = v_; }
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
			case PPUSTATUS_ADDRESS: // $2002
				/*
				 * http://wiki.nesdev.com/w/index.php/NMI
				 * Read $2002: Return old status of NMI_occurred in bit 7, then set NMI_occurred to false.
				 */
				byte oldStatus = _statusRegister.asByte();
				setNMIOccurred(false);
				_isFirstWrite = true;
				return oldStatus;
			case PPUDATA_ADDRESS: // $2007
				dataRegisterAccessIncrement();
				return _dataRegister.asByte();
			default:
				throw new UnsupportedOperationException("Tried to read PPU address " + HexUtils.toHex(address_));
		}
	}    
	
	public void write(short address_, byte val_) {
		switch(address_) {
			case PPUCTRL_ADDRESS: // $2000
				_controlRegister.setByte(val_);
				setNMIOccurred((val_ & (1 << 7)) != 0);
				/* t: ...BA.. ........ = d: ......BA */
				short destBitMask = ~(0b11 << 10);
				byte srcBitMask = 0b11;
				_t = (short) ((_t & destBitMask) | ((val_ & srcBitMask) << 10)); 				
				break;
			case PPUMASK_ADDRESS: // $2001
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
			case PPUADDR_ADDRESS: // $2006
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
			case PPUDATA_ADDRESS: // $2007
				_dataRegister.setByte(val_);
				dataRegisterAccessIncrement();
				break;
			default:
				throw new UnsupportedOperationException();
		}
	}

	private void dataRegisterAccessIncrement() {
		if(isRendering()) {		
			_v += _controlRegister.getBit(2) ? 32 : 1;
		} else {
			// TODO: both x and y increments are supposed to happen simultaneously
			incrementCoarseX();
			incrementY();
		}
	}
	
	private boolean isRendering() { 
		return isRenderingEnabled() && 
				(_verticalScroll <= LAST_VISIBLE_SCANLINE || _verticalScroll == MAX_SCANLINE); 
	}

	public int getCyclesSinceReset() { return _cyclesRunSinceReset; }
	public int getCycles() { return _cyclesRun; }
	public int getHorizontalScroll() { return _horizontalScroll; }
	public int getVerticalScroll() { return _verticalScroll; }
	public byte getControlRegister() { return _controlRegister.asByte(); }
	public byte getMaskRegister() { return _maskRegister.asByte(); }
	public byte getStatusRegister() { return _statusRegister.asByte(); }
	public PPUMemory getMemory() { return _memory; }    
    public int getCoarseX() { return _v & 0b1_1111; }   
    public int getCoarseY() { return (_v >> 5) & 0b1_1111; }      
    

}
