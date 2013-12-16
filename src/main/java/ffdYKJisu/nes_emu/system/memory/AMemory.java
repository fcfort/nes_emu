/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ffdYKJisu.nes_emu.system.memory;

import ffdYKJisu.nes_emu.domain.uByte;
import ffdYKJisu.nes_emu.domain.uShort;
import ffdYKJisu.nes_emu.exceptions.InvalidAddressException;

/**
 *
 * @author fe01106
 */
public interface AMemory {     
    uByte read(uShort address);
    
    uByte read(uByte addrH, uByte addrL);

    uByte read(uByte zeroPageAddress);

    void write(uShort address, uByte val) throws InvalidAddressException;

    void write(uByte addrH, uByte addrL, uByte val) throws InvalidAddressException;
}
