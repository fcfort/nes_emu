package ffdYKJisu.nes_emu.domain;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * All possible addressing modes available to operations
 * on the system.
 * @author fe01106
 */
public enum AddressingMode {
	IMPLICIT( "Implied" ),
	ACCUMULATOR( "Accumulator" ),
	IMMEDIATE( "Immediate" ),
	ZERO_PAGE( "Zero Page" ),
	ZERO_PAGE_X( "Zero Page,X" ),
	ZERO_PAGE_Y( "Zero Page,Y" ),
	RELATIVE( "Relative" ),
	ABSOLUTE( "Absolute" ),
	ABSOLUTE_X( "Absolute,X" ),
	ABSOLUTE_Y( "Absolute,Y" ),
	INDIRECT( "Indirect" ),
	INDIRECT_X( "(Indirect,X)" ),
	INDIRECT_Y( "(Indirect),Y" );
	
	private static final Map<String, AddressingMode> lookup
		= new HashMap<String, AddressingMode>();

	static {
		for ( AddressingMode m : EnumSet.allOf( AddressingMode.class ) ) {
			lookup.put( m.getAddressingModeString(), m );
		}
	}

	private String getAddressingModeString() {
		return xmlName;
	}

	public static AddressingMode get( String mode ) {
		return lookup.get( mode );
	}
	private String xmlName;

	AddressingMode( String name ) {
		xmlName = name;
	}
	
	@Override public String toString() {
		return getAddressingModeString();
	}
}
