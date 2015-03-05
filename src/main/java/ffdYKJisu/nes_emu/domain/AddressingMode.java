package ffdYKJisu.nes_emu.domain;

import java.util.EnumSet;
import java.util.Map;

import com.google.common.collect.Maps;

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
	
	private static final Map<String, AddressingMode> nameMap = Maps.newHashMap();

	static {
		for ( AddressingMode m : EnumSet.allOf( AddressingMode.class ) ) {
			nameMap.put( m.getName(), m );
		}
	}
	
	private final String _name;
	
	AddressingMode( String name ) {
		_name = name;
	}

	private String getName() {
		return _name;
	}

	public static AddressingMode get( String mode ) {
		return nameMap.get( mode );
	}
	
	@Override public String toString() {
		return getName();
	}
}
