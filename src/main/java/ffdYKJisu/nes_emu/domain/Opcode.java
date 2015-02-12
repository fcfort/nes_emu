package ffdYKJisu.nes_emu.domain;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.UnsignedBytes;

/**
 * Stores static information about opcodes
 * @author fcf
 *
 */
public enum Opcode { 	
	BRK("00", "BRK", 7, 1, AddressingMode.IMPLICIT),
	ORAix("01", "ORA", 6, 2, AddressingMode.INDIRECT_X),
	ORAz("05", "ORA", 3, 2, AddressingMode.ZERO_PAGE),
	ASLz("06", "ASL", 5, 2, AddressingMode.ZERO_PAGE),
	PHP("08", "PHP", 3, 1, AddressingMode.IMPLICIT),
	ORAi("09", "ORA", 2, 2, AddressingMode.IMMEDIATE),
	ASLac("0A", "ASL", 2, 1, AddressingMode.ACCUMULATOR),
	ORAa("0D", "ORA", 4, 3, AddressingMode.ABSOLUTE),
	ASLa("0E", "ASL", 6, 3, AddressingMode.ABSOLUTE),
	BPL("10", "BPL", 2, 2, true, true, AddressingMode.RELATIVE),
	ORAiy("11", "ORA", 5, 2, AddressingMode.INDIRECT_Y),
	ORAzx("15", "ORA", 4, 2, AddressingMode.ZERO_PAGE_X),
	ASLzx("16", "ASL", 6, 2, AddressingMode.ZERO_PAGE_X),
	CLC("18", "CLC", 2, 1, AddressingMode.IMPLICIT),
	ORAay("19", "ORA", 4, 3, false, true, AddressingMode.ABSOLUTE_Y),
	ORAax("1D", "ORA", 4, 3, false, true, AddressingMode.ABSOLUTE_X),
	ASLax("1E", "ASL", 7, 3, AddressingMode.ABSOLUTE_X),
	JSR("20", "JSR", 6, 3, AddressingMode.ABSOLUTE),
	ANDix("21", "AND", 6, 2, AddressingMode.INDIRECT_X),
	BITz("24", "BIT", 3, 2, AddressingMode.ZERO_PAGE),
	ANDz("25", "AND", 3, 2, AddressingMode.ZERO_PAGE),
	ROLz("26", "ROL", 5, 2, AddressingMode.ZERO_PAGE),
	PLP("28", "PLP", 4, 1, AddressingMode.IMPLICIT),
	ANDi("29", "AND", 2, 2, AddressingMode.IMMEDIATE),
	ROLac("2A", "ROL", 2, 1, AddressingMode.ACCUMULATOR),
	BITa("2C", "BIT", 4, 3, AddressingMode.ABSOLUTE),
	ANDa("2D", "AND", 4, 3, AddressingMode.ABSOLUTE),
	ROLa("2E", "ROL", 6, 3, AddressingMode.ABSOLUTE),
	BMI("30", "BMI", 2, 2, true, true, AddressingMode.RELATIVE),
	ANDiy("31", "AND", 5, 2, AddressingMode.INDIRECT_Y),
	ANDzx("35", "AND", 4, 2, AddressingMode.ZERO_PAGE_X),
	ROLzx("36", "ROL", 6, 2, AddressingMode.ZERO_PAGE_X),
	SEC("38", "SEC", 2, 1, AddressingMode.IMPLICIT),
	ANDay("39", "AND", 4, 3, false, true, AddressingMode.ABSOLUTE_Y),
	ANDax("3D", "AND", 4, 3, false, true, AddressingMode.ABSOLUTE_X),
	ROLax("3E", "ROL", 7, 3, AddressingMode.ABSOLUTE_X),
	EORa("40", "EOR", 4, 3, AddressingMode.ABSOLUTE),
	EORix("41", "EOR", 6, 2, AddressingMode.INDIRECT_X),
	EORz("45", "EOR", 3, 2, AddressingMode.ZERO_PAGE),
	LSRz("46", "LSR", 5, 2, AddressingMode.ZERO_PAGE),
	PHA("48", "PHA", 3, 1, AddressingMode.IMPLICIT),
	EORi("49", "EOR", 2, 2, AddressingMode.IMMEDIATE),
	LSRac("4A", "LSR", 2, 1, AddressingMode.ACCUMULATOR),
	JMPa("4C", "JMP", 3, 3, AddressingMode.ABSOLUTE),
	RTI("4D", "RTI", 6, 1, AddressingMode.IMPLICIT),
	LSRa("4E", "LSR", 6, 3, AddressingMode.ABSOLUTE),
	BVC("50", "BVC", 2, 2, true, true, AddressingMode.RELATIVE),
	EORiy("51", "EOR", 5, 2, false, true, AddressingMode.INDIRECT_Y),
	EORzx("55", "EOR", 4, 2, AddressingMode.ZERO_PAGE_X),
	LSRzx("56", "LSR", 6, 2, AddressingMode.ZERO_PAGE_X),
	CLI("58", "CLI", 2, 1, AddressingMode.IMPLICIT),
	EORay("59", "EOR", 4, 3, false, true, AddressingMode.ABSOLUTE_Y),
	EORax("5D", "EOR", 4, 3, false, true, AddressingMode.ABSOLUTE_X),
	LSRax("5E", "LSR", 7, 3, AddressingMode.ABSOLUTE_X),
	RTS("60", "RTS", 6, 1, AddressingMode.IMPLICIT),
	ADCix("61", "ADC", 6, 2, AddressingMode.INDIRECT_X),
	ADCz("65", "ADC", 3, 2, AddressingMode.ZERO_PAGE),
	RORz("66", "ROR", 5, 2, AddressingMode.ZERO_PAGE),
	PLA("68", "PLA", 4, 1, AddressingMode.IMPLICIT),
	ADCi("69", "ADC", 2, 2, AddressingMode.IMMEDIATE),
	RORac("6A", "ROR", 2, 1, AddressingMode.ACCUMULATOR),
	JMP("6C", "JMP", 5, 3, AddressingMode.INDIRECT),
	ADCa("6D", "ADC", 4, 3, AddressingMode.ABSOLUTE),
	RORa("6E", "ROR", 6, 3, AddressingMode.ABSOLUTE),
	BVS("70", "BVS", 2, 2, true, true, AddressingMode.RELATIVE),
	ADCiy("71", "ADC", 5, 2, false, true, AddressingMode.INDIRECT_Y),
	ADCzx("75", "ADC", 4, 2, AddressingMode.ZERO_PAGE_X),
	RORzx("76", "ROR", 6, 2, AddressingMode.ZERO_PAGE_X),
	SEI("78", "SEI", 2, 1, AddressingMode.IMPLICIT),
	ADCay("79", "ADC", 4, 3, false, true, AddressingMode.ABSOLUTE_Y),
	ADCax("7D", "ADC", 4, 3, false, true, AddressingMode.ABSOLUTE_X),
	RORax("7E", "ROR", 7, 3, AddressingMode.ABSOLUTE_X),
	STAix("81", "STA", 6, 2, AddressingMode.INDIRECT_X),
	STYz("84", "STY", 3, 2, AddressingMode.ZERO_PAGE),
	STAz("85", "STA", 3, 2, AddressingMode.ZERO_PAGE),
	STXz("86", "STX", 3, 2, AddressingMode.ZERO_PAGE),
	DEY("88", "DEY", 2, 1, AddressingMode.IMPLICIT),
	TXA("8A", "TXA", 2, 1, AddressingMode.IMPLICIT),
	STYa("8C", "STY", 4, 3, AddressingMode.ABSOLUTE),
	STAa("8D", "STA", 4, 3, AddressingMode.ABSOLUTE),
	STXa("8E", "STX", 4, 3, AddressingMode.ABSOLUTE),
	BCC("90", "BCC", 2, 2, true, true, AddressingMode.RELATIVE),
	STAiy("91", "STA", 6, 2, AddressingMode.INDIRECT_Y),
	STYzx("94", "STY", 4, 2, AddressingMode.ZERO_PAGE_X),
	STAzx("95", "STA", 4, 2, AddressingMode.ZERO_PAGE_X),
	STXzy("96", "STX", 4, 2, AddressingMode.ZERO_PAGE_Y),
	TYA("98", "TYA", 2, 1, AddressingMode.IMPLICIT),
	STAay("99", "STA", 5, 3, AddressingMode.ABSOLUTE_Y),
	TXS("9A", "TXS", 2, 1, AddressingMode.IMPLICIT),
	STAax("9D", "STA", 5, 3, AddressingMode.ABSOLUTE_X),
	LDYi("A0", "LDY", 2, 2, AddressingMode.IMMEDIATE),
	LDAix("A1", "LDA", 6, 2, AddressingMode.INDIRECT_X),
	LDXi("A2", "LDX", 2, 2, AddressingMode.IMMEDIATE),
	LDYz("A4", "LDY", 3, 2, AddressingMode.ZERO_PAGE),
	LDAz("A5", "LDA", 3, 2, AddressingMode.ZERO_PAGE),
	LDXz("A6", "LDX", 3, 2, AddressingMode.ZERO_PAGE),
	TAY("A8", "TAY", 2, 1, AddressingMode.IMPLICIT),
	LDAi("A9", "LDA", 2, 2, AddressingMode.IMMEDIATE),
	TAX("AA", "TAX", 2, 1, AddressingMode.IMPLICIT),
	LDYa("AC", "LDY", 4, 3, AddressingMode.ABSOLUTE),
	LDAa("AD", "LDA", 4, 3, AddressingMode.ABSOLUTE),
	LDXa("AE", "LDX", 4, 3, AddressingMode.ABSOLUTE),
	BCS("B0", "BCS", 2, 2, true, true, AddressingMode.RELATIVE),
	LDAiy("B1", "LDA", 5, 2, false, true, AddressingMode.INDIRECT_Y),
	LDYzx("B4", "LDY", 4, 2, AddressingMode.ZERO_PAGE_X),
	LDAzx("B5", "LDA", 4, 2, AddressingMode.ZERO_PAGE_X),
	LDXzy("B6", "LDX", 4, 2, AddressingMode.ZERO_PAGE_Y),
	CLV("B8", "CLV", 2, 1, AddressingMode.IMPLICIT),
	LDAay("B9", "LDA", 4, 3, false, true, AddressingMode.ABSOLUTE_Y),
	TSX("BA", "TSX", 2, 1, AddressingMode.IMPLICIT),
	LDYax("BC", "LDY", 4, 3, false, true, AddressingMode.ABSOLUTE_X),
	LDAax("BD", "LDA", 4, 3, false, true, AddressingMode.ABSOLUTE_X),
	LDXay("BE", "LDX", 4, 3, false, true, AddressingMode.ABSOLUTE_Y),
	CPYi("C0", "CPY", 2, 2, AddressingMode.IMMEDIATE),
	CMPix("C1", "CMP", 6, 2, AddressingMode.INDIRECT_X),
	CPYz("C4", "CPY", 3, 2, AddressingMode.ZERO_PAGE),
	CMPz("C5", "CMP", 3, 2, AddressingMode.ZERO_PAGE),
	DECz("C6", "DEC", 5, 2, AddressingMode.ZERO_PAGE),
	INY("C8", "INY", 2, 1, AddressingMode.IMPLICIT),
	CMPi("C9", "CMP", 2, 2, AddressingMode.IMMEDIATE),
	DEX("CA", "DEX", 2, 1, AddressingMode.IMPLICIT),
	CPYa("CC", "CPY", 4, 3, AddressingMode.ABSOLUTE),
	CMPa("CD", "CMP", 4, 3, AddressingMode.ABSOLUTE),
	DECa("CE", "DEC", 6, 3, AddressingMode.ABSOLUTE),
	BNE("D0", "BNE", 2, 2, true, true, AddressingMode.RELATIVE),
	CMPiy("D1", "CMP", 5, 2, false, true, AddressingMode.INDIRECT_Y),
	CMPzx("D5", "CMP", 4, 2, AddressingMode.ZERO_PAGE_X),
	DECzx("D6", "DEC", 6, 2, AddressingMode.ZERO_PAGE_X),
	CLD("D8", "CLD", 2, 1, AddressingMode.IMPLICIT),
	CMPay("D9", "CMP", 4, 3, false, true, AddressingMode.ABSOLUTE_Y),
	CMPax("DD", "CMP", 4, 3, false, true, AddressingMode.ABSOLUTE_X),
	DECax("DE", "DEC", 7, 3, AddressingMode.ABSOLUTE_X),
	CPXi("E0", "CPX", 2, 2, AddressingMode.IMMEDIATE),
	SBCix("E1", "SBC", 6, 2, AddressingMode.INDIRECT_X),
	CPXz("E4", "CPX", 3, 2, AddressingMode.ZERO_PAGE),
	SBCz("E5", "SBC", 3, 2, AddressingMode.ZERO_PAGE),
	INCz("E6", "INC", 5, 2, AddressingMode.ZERO_PAGE),
	INX("E8", "INX", 2, 1, AddressingMode.IMPLICIT),
	SBCi("E9", "SBC", 2, 2, AddressingMode.IMMEDIATE),
	NOP("EA", "NOP", 2, 1, AddressingMode.IMPLICIT),
	CPXa("EC", "CPX", 4, 3, AddressingMode.ABSOLUTE),
	SBCa("ED", "SBC", 4, 3, AddressingMode.ABSOLUTE),
	INCa("EE", "INC", 6, 3, AddressingMode.ABSOLUTE),
	BEQ("F0", "BEQ", 2, 2, true, true, AddressingMode.RELATIVE),
	SBCiy("F1", "SBC", 5, 2, AddressingMode.INDIRECT_Y),
	SBCzx("F5", "SBC", 4, 2, AddressingMode.ZERO_PAGE_X),
	INCzx("F6", "INC", 6, 2, AddressingMode.ZERO_PAGE_X),
	SED("F8", "SED", 2, 1, AddressingMode.IMPLICIT),
	SBCay("F9", "SBC", 4, 3, false, true, AddressingMode.ABSOLUTE_Y),
	SBCax("FD", "SBC", 4, 3, false, true, AddressingMode.ABSOLUTE_X),
	INCax("FE", "INC", 7, 3, AddressingMode.ABSOLUTE_X);

	private final Logger logger = LoggerFactory.getLogger(Opcode.class);
	
	private final String opcodeBytes;
	private final String codeName;
	private final int cycles;
	private final int length;
	private final boolean extraCycleOnBranch;
	private final boolean extraCycleOnPageJump;
	private final AddressingMode addressingMode;

	private final static int OPCODE_NUMBER_BASE = 16;

	private static final Map<Byte, Opcode> opcodeMap;
	static {
		opcodeMap = Maps.newHashMap();		
		for (Opcode o : Opcode.values()) {
			opcodeMap.put(o.getOpcodeBytes(), o);
			// logger.info("Mapping byte {} to opcode {}", o.getOpcodeBytes(), o);
		}
	}

	Opcode(String opcodeBytes, String codeName, int cycles, int length,
			AddressingMode addressingMode) {
		this(opcodeBytes, codeName, cycles, length, false, false,
				addressingMode);
	}

	Opcode(String opcodeBytes, String codeName, int cycles, int length,
			boolean extraCycleOnBranch, boolean extraCycleOnPageJump,
			AddressingMode addressingMode) {
		this.opcodeBytes = opcodeBytes;
		this.codeName = codeName;
		this.cycles = cycles;
		this.length = length;
		this.extraCycleOnBranch = extraCycleOnBranch;
		this.extraCycleOnPageJump = extraCycleOnPageJump;
		this.addressingMode = addressingMode;
		// logger.info("Done creating opcode {}", codeName);
	}

	public byte getOpcodeBytes() {	
		return UnsignedBytes.parseUnsignedByte(opcodeBytes, OPCODE_NUMBER_BASE);
	}

	public static Opcode getOpcodeByBytes(byte b) {		
		return opcodeMap.get(b);
	}

	public int getCycles() {
		return cycles;
	}

	public int getLength() {
		return length;
	}

	public AddressingMode getAddressingMode() {
		return addressingMode;
	}

	public String getCodeName() {
		return codeName;
	}

	public int getCycles(boolean isBranch, boolean isJump) {
		int cyclesTaken = cycles;
		if (isBranch && extraCycleOnBranch) {
			cyclesTaken++;
		}
		if (isJump && extraCycleOnPageJump) {
			cyclesTaken++;
		}
		return cyclesTaken;
	}

	// Opcode[opcodeBytes=A9,codeName=LDA,cycles=2,length=2,extraCycleOnBranch=false,
	// extraCycleOnPageJump=false,addressingMode=Immediate,name=LDAi,ordinal=97]
	@Override public String toString() {
		// return ToStringBuilder.reflectionToString(this,ToStringStyle.SHORT_PREFIX_STYLE);
		return "Opcode["+this.name()+","+this.getOpcodeBytes()+","+this.addressingMode+"]";
	}
	
}
