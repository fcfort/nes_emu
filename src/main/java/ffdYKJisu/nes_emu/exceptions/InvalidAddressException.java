package ffdYKJisu.nes_emu.exceptions;

/**
 * Invalid address exception
 * @author fcf
 */
public class InvalidAddressException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6702393265914768379L;

	public InvalidAddressException(String msg) {
          super(msg);
	}
}
