package ffdYKJisu.nes_emu.util;

import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.Beta;

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
	
	public static short parseUnsignedShort(String string, int radix) {
		int parse = Integer.parseInt(checkNotNull(string), radix);
		// We need to throw a NumberFormatException, so we have to duplicate checkedCast. =(
		if (parse >> Short.SIZE == 0) {
			return (short) parse;
		} else {
			throw new NumberFormatException("out of range: " + parse);
		}
	}

	public static short setBitRange(short src_, short dest_, int startIndex_, int endIndex_) {
		// 0b0000_0000
		// 0b7654_3210
		// 0b0011_1100, start of 5 end of 2
		
		int len = startIndex_ - endIndex_ + 1;
		
		// create mask
		short mask =(short) ((2 << len - 1) - 1) ;
		
		// shift mask over
		short shiftedMask = (short) (mask << endIndex_);
		
		logger.info("Got len {}, mask {}, shifted mask {}", 
				new Object[] {len, HexUtils.toHex(mask), HexUtils.toHex(shiftedMask)});
		// apply mask
		return (short) ((dest_ & ~shiftedMask) | (src_ & shiftedMask));
	}
}
