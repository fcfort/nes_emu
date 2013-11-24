package ffdYKJisu.nes_emu.system.cpu;

import java.util.logging.Level;
import java.util.logging.Logger;

import ffdYKJisu.nes_emu.domain.uByte;
import ffdYKJisu.nes_emu.domain.uShort;
import ffdYKJisu.nes_emu.exceptions.addressException;



public class Stack {

    /**
     * Holds the current offset into the 1-page (stack) for the next 
     * available
     * empty spot for pushing to the stack
     */
    private uByte stackPointer = new uByte(0xFF);
    private final uShort stackOffset = new uShort(0x100);

    uByte get() {
        return stackPointer;
    }

    void set(uByte sp) {
        stackPointer = sp;
    }

    uByte pull() {
        stackPointer = new uByte(stackPointer.increment());
        uShort addr = new uShort(stackPointer.get() + stackOffset.get());
        uByte val = CPU.this.memory.read(addr);
        //System.out.println("Pulling " + val + " from " + addr);
        return val;
    }

    void push(uByte val) {
        uShort addr = new uShort(stackPointer.get() + stackOffset.get());
        try {
            CPU.this.memory.write(addr, val);
        //System.out.println("Pushing " + val + " to " + addr);
        } catch (addressException ex) {
            Logger.getLogger(CPU.class.getName()).log(Level.SEVERE, null,
                ex + "Error pushing " + val + " to " + addr);
        }
        stackPointer = new uByte(stackPointer.decrement());
    }
}