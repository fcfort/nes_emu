/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nes;

/**
 *
 * @author fe01106
 */
public interface Memory {
    
    
    uByte read(uShort address);
    
    uByte read(uByte addrH, uByte addrL);

    uByte read(uByte zeroPageAddress);

    void write(uShort address, uByte val) throws addressException;

    void write(uByte addrH, uByte addrL, uByte val) throws addressException;
}
