package ffdYKJisu.nes_emu.system.cpu;

import java.util.Deque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Queues;

import ffdYKJisu.nes_emu.domain.uByte;
import ffdYKJisu.nes_emu.domain.uShort;
import ffdYKJisu.nes_emu.exceptions.InvalidAddressException;
import ffdYKJisu.nes_emu.system.memory.IMemory;


public class Stack implements IMemory {

	private static final Logger logger = LoggerFactory.getLogger(Stack.class);
	
    /**
     * Holds the current offset into the 1-page (stack) for the next 
     * available empty spot for pushing to the stack
     */
    // private final uShort stackOffset;
    private final Deque<uByte> stack;
    
    private static final int STACK_SIZE = 0x100;
    
    public Stack() {
    	stack = Queues.newArrayDeque();    	
    	stackOffset  = new uShort(STACK_SIZE);
    }
    
    public uByte pop() {   
    	stackPointer--;
    	return stack.pop();
    }

    public void push(uByte val) {
    	if(stack.size() > STACK_SIZE) {
    		throw new UnsupportedOperationException("Stack cannot be greater than stack size " + STACK_SIZE);
    	}
    	
        stack.push(val);
        stack.toArray(new uByte[0]);
    }
    
    private uByte uByteAt(final int i) {
    	return stack.toArray(new uByte[0])[i];
    }

	public uByte read(uShort address) {
		if(address.get() > STACK_SIZE) {
			throw new InvalidAddressException(address + " is greater than stack size " + STACK_SIZE);
		}
		logger.warn("Manual read of stack at address {}", address);
		return uByteAt(address.get());
	}

	public void write(uShort address, uByte val) {
		throw new UnsupportedOperationException("Not possible to write to an address inside the stack space");
	}

    
	
}