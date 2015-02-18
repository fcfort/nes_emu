package ffdYKJisu.nes_emu.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * https://code.google.com/p/guava-libraries/issues/attachmentText?id=670&aid=6700003000&name=UnsignedShorts.java&token=d0e15f7447d60d53f0301dd87d3bbc44
 */
public final class UnsignedShorts {
	
	private static Logger logger = LoggerFactory.getLogger(UnsignedShorts.class);
	
	private UnsignedShorts() {}

	public static int compare(short address1_, short address2_) {
		int unsignedAddr1 = Short.toUnsignedInt(address1_);
		int unsignedAddr2 = Short.toUnsignedInt(address2_);				
		int result = Integer.compare(unsignedAddr1, unsignedAddr2);
		logger.debug("Comparing address {} to {}, got result {}", new Object[] {
				HexUtils.toHex(address1_),
				HexUtils.toHex(address2_),
				result
		});
		return result;
	}
}
