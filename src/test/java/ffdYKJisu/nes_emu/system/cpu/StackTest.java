package ffdYKJisu.nes_emu.system.cpu;

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import ffdYKJisu.nes_emu.domain.uByte;

public class StackTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testStackPushPop() {
		Stack s = new Stack();
		s.push(new uByte(1));
		uByte val = s.pop();
		assertTrue(val.get() == 1);;
	}
	

	@Test
	public void testStackSize() {
		Stack s = new Stack();
		for(int i = 0; i < 256; i++) {
			s.push(new uByte(0));
		}
	}
	
}
