package ffdYKJisu.nes_emu.system.cpu;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.common.io.Resources;
import com.google.common.primitives.UnsignedBytes;

import ffdYKJisu.nes_emu.domain.Opcode;
import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.NES;
import ffdYKJisu.nes_emu.system.memory.CPUMemory;
import ffdYKJisu.nes_emu.util.HexUtils;
import ffdYKJisu.nes_emu.util.UnsignedShorts;

/** 
 * nestest fairly thoroughly tests CPU operation. This is the best test to start 
 * with when getting a CPU emulator working for the first time. Start execution 
 * at $C000 and compare execution with a log from Nintendulator, whose CPU works 
 * (apart from some details of the power-up state).
 * 
 * http://wiki.nesdev.com/w/index.php/Emulator_tests
 */
public class NesTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(NesTest.class);
	
	NES _n;
	CPU _c;
	CPUMemory _mem; 
	List<CPUState> _nesTestLog;
	
	@Before
	public void initialize() throws UnableToLoadRomException, IOException {
		Cartridge c = new Cartridge(ClassLoader.getSystemResourceAsStream("nestest.nes"));
		NES _nes = new NES();
		_c = _nes.getCPU();
		_mem = _c.getCPUMemory();
		_mem.writeCartToMemory(c);
		_c.reset();
		_nesTestLog = readLogFile();
	}
	
	private static List<CPUState> readLogFile() throws IOException {
		return Resources.readLines(NesTest.class.getClassLoader().getResource("nestest.log"), Charsets.UTF_8, new LineProcessor<List<CPUState>>(){
			List<CPUState> states = Lists.newLinkedList();
			
			@Override
			public boolean processLine(String line_) throws IOException {
				String opcodeBytes = line_.substring(6, 14);
				String tokens[] = opcodeBytes.split("\\s+");
				
				byte operands[] = new byte[tokens.length];
				int i = 0;
				for(String token : tokens) {
					// LOGGER.info("Parsing byte {}", token);					
					operands[i++] = UnsignedBytes.parseUnsignedByte(token, 16);
				}				
				
				CPUState s = new CPUState(
						UnsignedShorts.parseUnsignedShort(line_.substring(0, 4), 16),
						operands,
						getRangeAsByte(line_, 50, 52),
						getRangeAsByte(line_, 55, 57),
						getRangeAsByte(line_, 60, 62),
						getRangeAsByte(line_, 65, 67),
						getRangeAsByte(line_, 71, 73)
				);
				
				// LOGGER.info("Got line {} and CPU state {}", line_, s);
				
				return states.add(s);
			}

			@Override
			public List<CPUState> getResult() {
				LOGGER.info("Parsed {} instructions", states.size());
				return states; 
			} 
		
		});
	}

	private static byte getRangeAsByte(String input_, int beginIndex, int endIndex) {
		return UnsignedBytes.parseUnsignedByte(input_.substring(beginIndex, endIndex), 16);
	}
	
	private static class CPUState {
		final short _PC;		
		final byte[] _operands;
		final byte _A;
		final byte _X;
		final byte _Y;
		final byte _SR;
		final byte _SP;
		
		public CPUState(short PC_, byte[] operands_, byte a_, byte x_,
				byte y_, byte SR_, byte SP_) {
			super();
			_PC = PC_;			
			_operands = operands_;
			_A = a_;
			_X = x_;
			_Y = y_;
			_SR = SR_;
			_SP = SP_;
		}
		
		@Override public boolean equals(Object o_) { return EqualsBuilder.reflectionEquals(this, o_); }	
		
		@Override public String toString() {
			return String.format("PC: %04X A: %02X X: %02X Y: %02X P: %02X SP: %02X",
					_PC,
					_A,
					_X,
					_Y,
					_SR,
					_SP
			);
		}
	}
	
	@Test
	public void runNesTestAndCompare() {
		_c.setPC((short) 0xC000);		
		
		int i = 0;
		for(CPUState s : _nesTestLog) {
			assertEquals(s, getState());
			LOGGER.info("({}): Asserting {} against {}", new Object[] {i++, s, getState()});
			_c.runStep();
		}
		
	}
	
	private CPUState getState() {		
		Opcode o = _c.getOpcode();
		byte[] operands = new byte[o.getLength()];
		for(int i = 0; i < o.getLength(); i++) {
			operands[i] = _mem.read((short) (_c.getPC() + i));
		}
		return new CPUState(_c.getPC(), operands, _c.getA(), _c.getX(), _c.getY(), _c.getSR(), _c.getSP());
	}
}