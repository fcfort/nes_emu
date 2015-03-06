package ffdYKJisu.nes_emu.system.ppu;

/** Taken from doc/nes_emu.txt */


public class PPU_Example {

	PPUData ppu;
	Region region;
	int k;
	CPU cpu;
	
	short pat_addr;
	short render_addr;
	short attribute_addr;
	byte tile;
	int attrib_shift;
	int attribute_shift;
	byte attribute;
	byte[] palette_index_buf = new byte[272];
	byte[] pattern_table;
	byte[] spr_pixel_index_buf = new byte[64];
	short addr;
	short tmp;
	
    static byte[] attr_shift_table = new byte[0x400];
    static byte[] attr_shift = attr_shift_table;
    static byte[] attr_loc = new byte[0x400];
    
    static {
	    for (int i = 0; i != 0x400; ++i)
	    {
	         attr_shift_table[i] = (byte) (((i >> 4) & 0x04) 
	                               | (i & 0x02));
	         attr_loc[i] = (byte) (((i >> 4) & 0x38) | ((i >> 2)) 
	                       & 7);
	    }
    }
		
	private static class CPU {
		int nmi;
	}
	
	private static class PPUData {
		int cycle;
		int scanline;
		int end_cycle;
		boolean short_scanline;
		boolean rendering;
		byte[] reg;
		byte treg6;
		boolean on_screen;
		int end_scanline;
		short bg_addr;
		short render_addr;
		byte[][] p_nt;
		byte[] palette_index_buf = new byte[272];
	}
	
	enum Region { NTSC, PAL }

	private void ppu_sprite_run() {
		throw new UnsupportedOperationException();
	}
	
	public void ppu_run(int ppu_cycles)
	{
	    for (int i = 0; i != ppu_cycles; ++i)
	    {
	        ++ppu.cycle;
	        if (ppu.cycle == 256)
	        {
	            if (ppu.scanline > -1 )
	                ppu.end_cycle = 341;
	            else if (ppu.short_scanline && ppu.rendering)
	                ppu.end_cycle = 340;
	            //while blargg's document said it was around 328,
	            //we just need to keep the sum of the cycles the same.
	        }
	        else if (ppu.cycle == 304)
	        {
	            //frame start, $2006 gets reloaded with the tmp addr
	            //this happens in the dummy scanline, and the PPU
	            //is rendering. The reason for the reload because
	            //reg[6] is changed as the PPU is rendering.
	            //reg[6] is the "program counter" for the PPU.
	            
	            if ((ppu.scanline < 0) && ppu.rendering)
	                ppu.reg[6] = ppu.treg6;
	        }
	        else if (ppu.cycle == ppu.end_cycle)
	        {
	            ++ppu.scanline;
	            ppu.cycle = 0;
	            if (ppu.scanline == 0)
	                ppu.on_screen = true;
	            else if (ppu.scanline == 240) //the idle scanline, 
	                                          //no rendering or anything
	                ppu.on_screen = ppu.rendering = false;
	            else if (ppu.scanline == 241) 
	            {
	                /* we are done with filling the frame buffer,
	                   render now */
	                // display->render_frame();
	                ppu.reg[2] |= 0x80; //vblank
	                /* It is known that the sprite register address 0x2003
	                   gets changed during rendering, and somehow goes back 
	                   to 0 when it is done with a frame, but so far, the
	                   cycle behavior isn't exactly known for how the reg 
	                   0x2003 gets changed during rendering, some games rely 
	                   on this behavior ppu.reg[3] getting reset to 
	                   0 at the end of frame.
	                 */
	                ppu.reg[3] = 0; //reset spr addr
	                cpu.nmi = ppu.reg[0] & 0x80; //set NMI 
	                //with this, we have 20 scanline of vblank for NTSC,
	                //70 for PAL.
	            }
	            else if (ppu.scanline == ppu.end_scanline)
	            {
	                ppu.scanline = -1;
	                ppu.rendering = (ppu.reg[1] & 0x18) != 0;
	                if (region == Region.NTSC) //if ntsc first ppu scanline is 340 cyc
	                    ppu.short_scanline ^= true;
	                else
	                    ppu.short_scanline = false;
	            }
	        }
	        else if ((ppu.scanline < 0) && (ppu.cycle == 1))
	        {
	            //vblank gets cleared a cycle later, thanks to nintendulator
	            //for this information.
	            ppu.reg[2] = 0;
	        }
	
	        /* execute code here if the PPU is rendering 
	           that is the scanline is < 240 and the bg or spr
	           is set to render in 0x2001 */
	        if (ppu.rendering)
	        {
	            ppu_sprite_run(); //put it in a separate function for clearer 
	                              //explanation
	            switch (ppu.cycle)
	            {
	                //everything in here is related to the bg layer
	                //the NES PPU has 2 layers, the BG and the SPRITE.
	                
	                /* This runs 34 times, (tiles, attribute tables, etc.)
	                   because there are 32 tiles
	                   to a scanline (256 horizontal pixels) and then
	                   2 more tiles at the end for the next scanline
	                   since the PPU renders on *every* cycle
	                   (cycle 0 to 255). every tile is processed
	                   in 8 cycles before it restarts, showing
	                   the increments of 8 below, basically the steps
	                   are as follows, it takes 2 cycles to read a data
	                   that means 2 cycles for tiles, 2 cycles for attributes,
	                   2 cycles for lo chr and then 2 cycles for high, 
	                   making it a total of 8 cycles for each tile 
	                   decompressed. */
	                
	                case   0:   case   8:   case  16:   case  24:   
	                case  32:   case  40:   case  48:   case  56:
	                case  64:   case  72:   case  80:   case  88:   
	                case  96:   case 104:   case 112:   case 120:
	                case 128:   case 136:   case 144:   case 152:   
	                case 160:   case 168:   case 176:   case 184:
	                case 192:   case 200:   case 208:   case 216:   
	                case 224:   case 232:   case 240:   case 248:
	                case 320:   case 328:
	                    /*
	                       This is why treg6 was shifted to the left 10
	                       this acts the upper 10th and 11th bit,
	                       deciding the name table ppu.reg[6] will 
	                       read from. (PPU should be allowed to wait
	                       at least one frame until you render
	                       Regs write to treg6 so it can get reloaded
	                       as we will see later reg[6] gets incremented
	                       during rendering. */
	
	                    /* This is for the bg rendering
	                       get the tile address, all it does
	                       is get the name table address written
	                       in the lower 2 bits of 0x2000, then it 
	                       gets then it gets the lower 0x3FF bytes
	                       where the tiles are stored, since
	                       the tiles are 32x30, it can use up to 960 (32*30)
	                       bytes, but why do we AND by 0x3FF? This is because
	                       the tile can be fetched from the attribute tables
	                       if the address of reg[6] (loopy_v is in that 
	                       range */
	                    
	                    //p_nt is a 4 pointer to array index to deal with 
	                    //mirroring
	                    
	                    //get the tile, the tile is written to by the 
	                    //programmer to the name table, (0x2000 to 0x2FFF)
	                    //this tells the PPU where to get the pattern table 
	                    //later from.
	                    
	                    tile = ppu.p_nt[(ppu.reg[6] & 0xC00) >> 10]
	                                [ppu.reg[6] & 0x3FF];
	                    break;
	                
	                case   1:   case   9:   case  17:   case  25:
	                case  33:   case  41:   case  49:   case  57:
	                case  65:   case  73:   case  81:   case  89:
	                case  97:   case 105:   case 113:   case 121:
	                case 129:   case 137:   case 145:   case 153:
	                case 161:   case 169:   case 177:   case 185:
	                case 193:   case 201:   case 209:   case 217:
	                case 225:   case 233:   case 241:   case 249:
	                case 321:   case 329:
	                    //this gets the pattern address
	                    //using the tile we got a cycle earlier.
	                    pat_addr = (short) ((tile << 4) | (ppu.reg[6] >> 12) | 
	                                ppu.bg_addr); 
	                    break;
	                    
	                    /* As documented above, we know that each tile is 
	                       8x8 pixels, thus we shift (tile << 4) by skipping
	                       16 bytes of data, since the lower 2 bits of the 
	                       index that accesses the palette is stored in 2
	                       different bytes 8 bytes away from each other,
	                       making each tile taking up 16 bytes each of 8 bits,
	                       for 8x8 pixels. The ppu.reg[6] >> 12 is known 
	                       as the fine y, every scanline the ppu.reg[6] 
	                       is incremented in the upper 12-14 bit, which goes 
	                       from 0-7, this tells which scanline the PPU is on 
	                       so it knows where to look, every value correspond 
	                       to every next byte such so let us say bg_addr 
	                       is one, and the tile is 0, then the value
	                       it takes is 0 to 7, which is pattern_table[0, 7].
	                       As you can see here each byte in 16 byte format
	                       is going vertically down. 
	                       
	                       Example: pattern_table[0] and pattern_table[8]
	                       contains the pixels for the first 8 pixels.
	                       pattern_table[1] and pattern_table[9] contains
	                       the first 8 pixels for the NEXT scanline, and
	                       so forth. The ppu.bg_addr in this case is the value
	                       we set in reg 0x2000, either 0 or 0x1000.
	                       this pat_addr tells the PPU where to get the 
	                       pattern table data from.
	                      
	                      */
	                      
	                case   2:   case  10:   case  18:   case  26:   
	                case  34:   case  42:   case  50:   case  58:
	                case  66:   case  74:   case  82:   case  90:   
	                case  98:   case 106:   case 114:   case 122:
	                case 130:   case 138:   case 146:   case 154:   
	                case 162:   case 170:   case 178:   case 186:
	                case 194:   case 202:   case 210:   case 218:   
	                case 226:   case 234:   case 242:   case 250:
	                    /* gets the attribute address, also get the attribute 
	                       shift, since attribute_addr only gets the address 
	                       of the attribute byte,
	                       and not which location of the 2 bits we want. 
	                       So we get it using */
	
	                    //attribute_shift which tells us how much we 
	                    //need to right shift by to get it later.
	                    attribute_addr = ppu.render_addr = (short) (0x23C0 | 
	                                     (ppu.reg[6] & 0xC00) |
	                                     (attr_loc[ppu.reg[6] & 0x3FF]));
	                    attrib_shift = attr_shift[ppu.reg[6] & 0x3FF];
	                    break;
	
	                    /* This gets the attribute address, which is the 64 
	                       bytes at the end of the name table, so the 
	                       (0x23C0 | ppu.reg[6] & 0xC00) figures out which 
	                       name table we are using, from ppu.reg[6]
	                       and then and it is looked up in a table for speed. 
	                       The code to fill the attr_loc[] table and 
	                       attr_shift_table[] 
	                       is 
	                       unsigned char attr_shift_table[0x400];
	                       unsigned char attr_loc[0x400];
	                       for (int i = 0; i != 0x400; ++i)
	                       {
	                            attr_shift_table[i] = ((i >> 4) & 0x04) 
	                                                  | (i & 0x02);
	                            attr_loc[i] = ((i >> 4) & 0x38) | ((i >> 2)) 
	                                          & 7;
	                       }
	                     */
	
	                case   3:   case  11:   case  19:   case  27:   
	                case  35:   case  43:   case  51:   case  59:
	                case  67:   case  75:   case  83:   case  91:   
	                case  99:   case 107:   case 115:   case 123:
	                case 131:   case 139:   case 147:   case 155:   
	                case 163:   case 171:   case 179:   case 187:
	                case 195:   case 203:   case 211:   case 219:   
	                case 227:   case 235:   case 243:
	                    //this time we apply the attribute byte to the
	                    //pixel buffer
	                    
	                    render_addr = attribute_addr; //to make it more clear
	
	                    attribute = (byte) (((ppu.p_nt[(render_addr & 0xC00) >> 10] 
	                        [render_addr & 0x3FF] >> attribute_shift) & 3) 
	                        << 2); 
	                    
	                    for (k = 0; k != 8; ++k)
	                        palette_index_buf[ppu.cycle+13+k] = attribute;
	                    
	                    if ((ppu.reg[6] & 0x1F) == 0x1F)
	                         ppu.reg[6] ^= 0x41F;
	                    else    
	                        ++ppu.reg[6];
	
	                    break;
	            
	                    /* 1 attribute byte covers 8 tiles, but we are
	                       doing one tile at a time, so we get 
	                       the 2 upper bits, and then copying the 
	                       attribute pixel_buf to 8 pixels places
	                       since a tile is 8 pixels each. We add indedx+13 
	                       because we are starting from 16 (3 + 13), 
	                       because the last 2 tile fetches happen at the 
	                       end of a previous scanline fill the first 16 pixels
	                       of the current scanline so it can render, as the 
	                       PPU renders a pixel per cycle.
	                       We increment the ppu.reg[6] because it is finished
	                       fetching the tile and the attribute, the increment
	                       acts as "fine x", allowing the next tile to be get
	                       on the next address, 
	                       (tile = ppu.p_nt[(ppu.reg[6] & 0xC00) >> 10]
	                               [ppu.reg[6] & 0x3FF])
	                        When it hits 31, that is when it is done drawing
	                        a scanline, since 8 * 32 = 256 pixels, it flips
	                        0x41FF to reset the position back to 0, as 
	                        0x41FF is 0b10000011111, so it resets the 5
	                        lower bits to reset the fine x to, the upper bit
	                        is used for mirroring. If we recall, 
	                        
	                        2000 write:
	                            t:xxxxABxxxxxxxxxx=d:xxxxxxAB
	                            t is treg6 in this case
	                        (0 = $2000; 1 = $2400; 2 = $2800; 3 = $2C00)
	                        If you recall the type of name table mirroring
	                        the NES has, you will find why this works,
	                        for horizontal mirroring 0x2000 and 0x2400
	                        and 0x2800 and 0x2C00 points to the same data,
	                        if you write 0x2000 or 0x2400 then the "B"
	                        value will get set or not set, but when it
	                        gets flipped, horizontal mirroring ensures
	                        you still get the same data, as for 0x2800
	                        and 0x2c00, this still works because 2 is 10b
	                        and 3 is 11b, so it will switch back and forth
	                        to locations that share the same data.
	                        As for vertical mirroring this works because
	                        0x2400 is equal to 0x2c00, so when it flips
	                        you get the vertical mirroring you expected,
	                        the same logic applies for the other 3 addresses.
	                        Single screen mirroring need not to worry about 
	                        this, since it all shares the same data, as for 4 
	                        separate name tables, it is assumed that the 
	                        programmer know what they are doing and thus made 
	                        the code fool proof to display the correct thing 
	                        when it switches.
	                     */
	                    
	                    case 323:   case 331:
	                        //this time we apply the attribute byte to the
	                        //pixel buffer
	                    
	                        render_addr = attribute_addr; 
	
	                        attribute = (byte) (((ppu.p_nt[(render_addr & 0xC00) >> 10]
	                                     [render_addr & 0x3FF] >> 
	                                     attribute_shift) & 3) << 2); 
	                    
	                        for (k = 0; k != 8; ++k)
	                            palette_index_buf[ppu.cycle-323+k] = attribute;
	                        
	                        if ((ppu.reg[6] & 0x1F) == 0x1F)
	                             ppu.reg[6] ^= 0x41F;
	                        else    
	                            ++ppu.reg[6];
	
	                        break;
	                    /* this is the last 2 attribute fetches for the next 
	                       scanline, since the PPU render a pixel 
	                       every cycle */
	
	                     case   4:  case  12:   case  20:   case  28:   
	                     case  36:  case  44:   case  52:   case  60:
	                     case  68:  case  76:   case  84:   case  92:   
	                     case 100:  case 108:   case 116:   case 124:
	                     case 132:  case 140:   case 148:   case 156:   
	                     case 164:  case 172:   case 180:   case 188:
	                     case 196:  case 204:   case 212:   case 220:   
	                     case 228:  case 236:   case 244:   case 252:
	                     case 324:  case 332:
	                        render_addr = pat_addr; 
	                    /* 
	                      this part is hardware based, the software part we 
	                      already got 
	                      pat_addr in cycle base, i guess there is some delay 
	                      in the hardware before it starts reading it.
	                      Reading/Writing takes 2 cycles each.
	                     */
	                      
	                     case   5:  case  13:   case  21:   case  29:   
	                     case  37:  case  45:   case  53:   case  61:
	                     case  69:  case  77:   case  85:   case  93:   
	                     case 101:  case 109:   case 117:   case 125:
	                     case 133:  case 141:   case 149:   case 157:   
	                     case 165:  case 173:   case 181:   case 189:
	                     case 197:  case 205:   case 213:   case 221:   
	                     case 229:  case 237:   case 245:   case 253:
	                        for (k = 0; k != 8; ++k)
	                            palette_index_buf[ppu.cycle+11+k] |= 
	                            		((pattern_table[addr] >> (k ^ 7)) & 1);
	                        break;
	                     
	                     case 325: case 333:
	                        for (k = 0; k != 8; ++k)
	                            palette_index_buf[ppu.cycle-325+k] |= 
	                                ((pattern_table[addr] >> (k ^ 7)) & 1); 
	                        break;
	                      /* The same thing applies to these 2 cycles, but we 
	                         subtract 325, because this is the last 2 tiles 
	                         fetches I was talking about,
	                         they are for the next scanline to use */
	
	                    /* We get the first lower bit from the pattern table 
	                       address and OR it with the upper 2 bits from the 
	                       address table */
	                    
	                    case   6:   case  14:   case  22:   case  30:   
	                    case  38:   case  46:   case  54:   case  62:
	                    case  70:   case  78:   case  86:   case  94:   
	                    case 102:   case 110:   case 118:   case 126:
	                    case 134:   case 142:   case 150:   case 158:   
	                    case 166:   case 174:   case 182:   case 190:
	                    case 198:   case 206:   case 214:   case 222:   
	                    case 230:   case 238:   case 246:   case 254:
	                    case 326:   case 334:
	                        render_addr = (short) (pat_addr | 8);
	                        break;
	                    /* This is for the other upper bit in the pattern 
	                       table, it is 8 bytes apart from the 1st bit, 
	                       so we OR it by 8 here. */
	                    
	                    case   7:   case  15:   case  23:   case  31:   
	                    case  39:   case  47:   case  55:   case  63:
	                    case  71:   case  79:   case  87:   case  95:   
	                    case 103:   case 111:   case 119:   case 127:
	                    case 135:   case 143:   case 151:   case 159:   
	                    case 167:   case 175:   case 183:   case 191:
	                    case 199:   case 207:   case 215:   case 223:   
	                    case 231:   case 239:   case 247:   case 255:
	                        for (k = 0; k != 8; ++k)
	                            palette_index_buf[ppu.cycle+9+k] |= 
	                                    ((((pattern_table[addr | 8] << 1) 
	                                    >> (k ^ 7)) & 2) );
	                        break;  
	                    /* This is the upper bit of the 2 bit plane that is 
	                       in the pattern table. Now we have our index 
	                       to access the palette */
	                    
	                    case 327:   case 335:
	                        for (k = 0; k != 8; ++k)
	                            palette_index_buf[ppu.cycle-327+k] |= 
	                                (((
	                                	(pattern_table[addr | 8] << 1) 
	                                	>> (k ^ 7)
	                                ) & 2) );
	                        break;
	                    //The upper bit of the 2 bit plane that is in 
	                    //the pattern table for the last 2 tiles.
	                    
	                    case 251:
	                        render_addr = attribute_addr;
	
	                        attribute = (byte) (((ppu.p_nt[(render_addr & 0xC00) >> 10]
	                                    [render_addr & 0x3FF] 
	                                    >> attribute_shift) & 3) << 2); 
	                        
	                        for (k = 0; k != 8; ++k)
	                            palette_index_buf[ppu.cycle+13+k] = attribute;
	
	                        if ((ppu.reg[6] & 0x1F) == 0x1F)
	                             ppu.reg[6] ^= 0x41F;
	                        else    
	                            ++ppu.reg[6];
	
	                        /* This is the same as above when applying the 
	                           attribute data */
	
	                        if ((ppu.reg[6] & 0x7000) == 0x7000)
	                        {
	                            tmp = (short) (ppu.reg[6] & 0x3E0);
	                            //reset tile y offset 12 - 14 in addr
	                            ppu.reg[6] &= 0xFFF;
	                            switch (tmp)
	                            {
	                                //29, flip bit 11
	                                case 0x3A0:
	                                    ppu.reg[6] ^= 0xBA0;
	                                    break;
	                                case 0x3E0: //31, back to 0
	                                    ppu.reg[6] ^= 0x3E0;
	                                    break;
	                                default: //inc y scroll if not reached
	                                    ppu.reg[6] += 0x20;
	                            }
	                        }
	                        else //inc fine y
	                            ppu.reg[6] += 0x1000;
	                        break;
	
	                        /* This is when the PPU reset the fine y with
	                           ppu.reg[6] &= 0xFFF;
	                           if it reaches 7 to index pattern table
	                           properly again.
	                           (Remember, pattern table is indexed with 
	                           ppu.reg[6] >> 12 so this clears the top bits 
	                           which is fine y.) else, it increments fine y 
	                           for the data to be get again for the pattern 
	                           table to access. As for the switch case,
	                           bits 5 to 9 represents the y scroll value, 
	                           and it gets incremented
	                           for every 8 fine y we do. It wraps back to 0 
	                           and then it flips 
	                           bit 11 when it hits 29, (for horizontal 
	                           mirroring it uses the
	                           other name table, for vertical mirroring, 
	                           this doesn't affect it)
	                           because the NES height is 240, so 8*30 = 240, 
	                           it fits the height, it is 29 because we count 0
	                           as 1. There is a quirk however,
	                           that is if you write to PPU scrolling regs to 
	                           modify the address then it goes back to 0, 
	                           and bit 11 does not get flipped, else
	                           it gets incremented by 32, because the y 
	                           scroll is at bit 5 to 9, and not 0 to 4.
	                         */
	                    /* This is the sprite's turn to grab the data, 
	                       since the PPU
	                       can do up to 8 sprites (32 bytes memory) 
	                       it takes 8*8=64 cycles
	                       to do it. The grab is just like background, 
	                       it goes from 256 to cycle 319 */
	                    
	                    /* NOTE: these render_addr equals to was gotten 
	                       from nintendulator information, since
	                       quietust and kevtris did a cycle by cycle reading
	                       of the PPU to figure out what exactly happens,
	                       I am not certain if this is what the PPU
	                       is actually reading back during these cycles.
	
	                    case 256:   case 264:   case 272:   case 280:   
	                    case 288:   case 296:   case 304:   case 312:
	                        render_addr = 0x2000 | (ppu.reg[6] & 0xFFF); 
	                    /* this is supposed to get the name table address
	                       for the title, but, the tile is stored in the 32
	                       byte buffer, so we'll just leave the render_addr
	                       like this, since the memory map of that 32 byte
	                       is not accessible from a regular memory read, 
	                       instead 0x2004 is the one that reads back 
	                       what the sprite rendering is doing. */
	                    break;
	
	                    case 257:
	                        ppu.reg[6] &= 0xFBE0;
	                        ppu.reg[6] |= (ppu.treg6 & 0x41F);
	                        render_addr = (short) (0x2000 | (ppu.reg[6] & 0xFFF)); 
	                    /* this resets every x bits and,
	                       and then reads back from the temporary reg written 
	                       in $2005. This gives the new scanline 
	                       a fresh start for the x scroll. */
	                    
	                    case 258:   case 266:   case 274:   case 282:   
	                    case 290:   case 298:   case 306:   case 314:
	                        render_addr = (short) (0x2000 | (ppu.reg[6] & 0xFFF));
	                        /* supposed to get attribute table, but we don't 
	                           do it here because
	                           we got it in the 32 byte buffer.
	
	                    case 265:   case 273:   case 281:   case 289:   
	                    case 297:   case 305:   case 313:
	                        //get pattern table here, but we don't do 
	                        //that either, because
	                        //its done during the fill
	
	                    case 259:   case 267:   case 275:   case 283:   
	                    case 291:   case 299:   case 307:   case 315:
	                        pat_addr = sprite_pat_addr;
	                        //figure out the pattern address here
	                        break;
	                    case 260:   case 268:   case 276:   case 284:   
	                    case 292:   case 300:   case 308:   case 316:
	                        render_addr = pat_addr;
	                        break;
	                        //copy pat_addr to render addr we have
	                    case 261:   case 269:   case 277:   case 285:   
	                    case 293:   case 301:   case 309:   case 317:
	                        //copy attribute code here, and then apply
	                        //low chr.
	                        for (k = 0; k != 8; ++k)
	                        {
	                            spr_pixel_index_buf[ppu.cycle-261+k] = 
	                                        attribute code here;
	                            spr_pixel_index_buf[ppu.cycle-261+k] |= 
	                                       low chr here.
	                        }
	                        /* spr_pixel_index_buf is a 64 byte buf, 
	                           since it is 8 tiles. (64 pixels) */
	                    case 262:   case 270:   case 278:   case 286:
	                    case 294:   case 302:   case 310:   case 318:
	                        render_addr = (short) (pat_addr | 8);
	                        break;
	                    //get the hi chr now...
	
	                    case 263:   case 271:   case 279:   case 287:
	                    case 295:   case 303:   case 311:   case 319:
	                        for (k = 0; k != 8; ++k)
	                            spr_pixel_index_buf[ppu.cycle-261+k] |= 0;  // hi chr here.
	                        break;
	                    case 336:   case 338:
	                        render_addr = (short) (0x2000 | (ppu.reg[6] & 0xFFF));
	                        break;
	                    case 337:   case 339:
	                        break;
	                    case 340:
	                        break;
	                    /* Now one line of scanline has been complete, 
	                       341 cycles [0, 340] has been done.
	                       Another note, the render_addr = ...
	                       is not certain that the real NES
	                       reads like this, but it does adhere
	                       to the 2 cycles per fetch basis, also
	                       no games does sprites to this accurate fetching,
	                       one can just speed up the sprite code by writing
	                       all the sprite data at once at cycle 319, since
	                       we already have all the data by cycle 255. 
	                      */
	            }
	            //The PPU render a pixel every cycle, this is ran
	            //when the PPU is on_screen and less than cycle 256
	            //(stil rendering that 256 horizontal pixels)
	            if (ppu.on_screen && (ppu.cycle < 256))
	            {
	              /* this checks if the background is visible, or the cycle 
	                 is greater than 8, or background cliping is off reg 
	                 0x2001 can configure if the 8 pixels on the left of the
	                 screen can be displayed or not. */
	
	                /* Now we know enough to explain inc_x. Since 0x2005 
	                   affects scrolling, and the NES gets a tile a 16 
	                   byte chunks covering 8x8 pixels,
	                   there isn't a way to say I want to scroll starting 
	                   from bit 6 (2nd pixel) on the first tile, 
	                   so this is where inc_x comes in, it allows pixel
	                   granularity for scrolling, of width 8, because a 
	                   byte is 8 pixels in the NES
	                   case.
	                */
	                if (ppu.bg_v && ((ppu.cycle >= 8) || ppu.bg_clip))
	                    index = ppu.palette_index_buf[ppu.cycle + ppu.inc_x]; 
	                    //covering the inc_x too
	                else
	                    index = 0; //if not, use master palette color.
	                /* this code checks if the sprite is visible and no 
	                   sprite clipping */
	
	                if (ppu.spr_v && ((ppu.cycle >= 8) || ppu.spr_clip))
	                {
	                    /* goes through the sprite (8 of them in total)
	                       to find the first sprite inline with the 
	                       x coordinate as it is rendering */
	                    for (k = 0; k < sprcount; k += 4)
	                    {
	                        tmp = k >> 1;
	                        //sprite's x pos
	                        j = ppu.cycle - ppu.sspr_data[0x20 | tmp];
	                        if (j & ~7) //not in range, keep going
	                            continue;
	                        j = ppu.spr_buf[(k << 1) | j];
	                        if (j) //the sprite is in range, figure out to 
	                               //draw it or not.
	                        {
	                            /* If sprite 0 is in the scanline, 
	                               figured out in sprite_run() and the 
	                               background is visible and it the sprite 
	                               found in range for the x position render is
	                               sprite 0, and the cycle is less than 255 
	                               (meaning sprite hit won't happen if the 
	                               NES rendering the 256th pixel, and 
	                               the index that is the bg is not 
	                               transparent, (using the master palette 
	                               color, we set a flag ppu.reg[2] |= 0x40.
	                               this is known as the sprite hit flag, and 
	                               basically it is used for timing purposes 
	                               to achieve scrolling effects. */
	                            
	                            if (ppu.spr0inline && !k &&
	                                ppu.bg_v && (ppu.cycle < 255) &&
	                                index)
	                            {
	                                ppu.reg[2] |= 0x40;
	                                ppu.spr0inline = 0;
	                            }
	                            //if the sprite priority is greater than the bg
	                            //set in the sprite ram, we replace the index
	                            //with the sprite data instead of the bg,
	                            //we |= 0x10 because the sprite palette 
	                            //address is
	                            //greater than the bg
	
	                            //do if sprite priority is greater than bg
	                            if (!(index && ppu.sspr_data[0x20 | 
	                                 (tmp | 1)]))
	                                index = j | 0x10;
	                            //OR by 0x10 since sprite palette is in
	                            //second row
	    
	                            //found first opaque sprite break out
	                            //since lower priority is displayed in
	                            //front of higher priority
	                            break;
	                        }
	                    }
	                }
	                //if ppu is off, render from palette
	                //0 or if $2006 is in 0x3FXX range
	                //use that color
	                if (!ppu.rendering)
	                {
	                    if ((ppu.reg[6] & 0x3F00) == 0x3F00)
	                        index = ppu.reg[6] & 0x1F;
	                }
	                index &= ppu.grayscale; //deal with grayscale 
	                                        //set in 0x2001
	                index |= ppu.color_emphasis; //color emphasis 
	                                             //set in 0x2001. 
	                ppu.p_pixel = ppu.p_palette[ppu.palette[index]]; 
	                //copy to the pixel buf
	                ++ppu.p_pixel;
	        }
	    }
	}

}
}
