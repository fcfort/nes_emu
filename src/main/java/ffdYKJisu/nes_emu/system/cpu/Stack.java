package ffdYKJisu.nes_emu.system.cpu;

import java.util.logging.Level;
import java.util.logging.Logger;

import ffdYKJisu.nes_emu.domain.uByte;
import ffdYKJisu.nes_emu.domain.uShort;
import ffdYKJisu.nes_emu.exceptions.AddressException;
import ffdYKJisu.nes_emu.system.memory.Memory;

public class Stack implements Memory {

    /**
     * Holds the current offset into the 1-page (stack) for the next 
     * available
     * empty spot for pushing to the stack
     */
	private uByte[] stack;
    private uByte stackPointer;
    private final uShort stackOffset;
    
    private static final int STACK_SIZE = 0x100;
    
    public Stack() {
    	stack = new uByte[STACK_SIZE];
    	stackPointer = new uByte(STACK_SIZE - 1);
    	stackOffset  = new uShort(STACK_SIZE);
    }
    
    public uByte get() {
        return stackPointer;
    }

    public void set(uByte sp) {
        stackPointer = sp;
    }

    public uByte pop() {
        stackPointer = new uByte(stackPointer.increment());
        uShort addr = new uShort(stackPointer.get() + stackOffset.get());
        uByte val = this.read(addr);
        //System.out.println("Pulling " + val + " from " + addr);
        return val;
    }

    public void push(uByte val) {
        uShort addr = new uShort(stackPointer.get() + stackOffset.get());
        try {
            this.write(addr, val);
        //System.out.println("Pushing " + val + " to " + addr);
        } catch (AddressException ex) {
            Logger.getLogger(CPU.class.getName()).log(Level.SEVERE, null,
                ex + "Error pushing " + val + " to " + addr);
        }
        stackPointer = new uByte(stackPointer.decrement());
    }

	public uByte read(uShort address) {
		return stack[address.get()];
	}

	public uByte read(uByte addrH, uByte addrL) {
		return stack[addrL.get()];
	}

	public uByte read(uByte zeroPageAddress) {
		return stack[zeroPageAddress.get()];
	}

	public void write(uShort address, uByte val) throws AddressException {
		stack[address.get()] = val;
	}

	public void write(uByte addrH, uByte addrL, uByte val)
			throws AddressException {
		stack[addrL.get()] = val;
	}
	
}