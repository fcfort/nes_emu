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
	
	public InvalidAddressException(Throwable e_, String template_, Object ... args_) {		
        super(String.format(template_, args_), e_);
	}
	
	public InvalidAddressException(String template_, Object ... args_) {		
      super(String.format(template_, args_));
	}
}
