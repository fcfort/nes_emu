package ffdYKJisu.nes_emu.system.cpu;

import java.util.Deque;

import com.google.common.collect.Queues;

import ffdYKJisu.nes_emu.domain.uByte;
import ffdYKJisu.nes_emu.domain.uShort;
import ffdYKJisu.nes_emu.exceptions.InvalidAddressException;
import ffdYKJisu.nes_emu.system.memory.IMemory;


public class Stack implements IMemory {

    /**
     * Holds the current offset into the 1-page (stack) for the next 
     * available empty spot for pushing to the stack
     */
    private uByte stackPointer;
    private final uShort stackOffset;
    private final Deque<uByte> stack;
    
    private static final int STACK_SIZE = 0x100;
    
    public Stack() {
    	stack = Queues.newArrayDeque();    	
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
    	return stack.pop();
    }

    public void push(uByte val) {
        stack.push(val);
        stack.toArray(new uByte[0]);
    }
    
    private uByte uByteAt(final int i) {
    	return stack.toArray(new uByte[0])[i];
    }

	public uByte read(uShort address) {
		return uByteAt(address.get());
	}

	public void write(uShort address, uByte val) throws InvalidAddressException {
		
	}

    
	
}