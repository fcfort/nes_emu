package ffdYKJisu.nes_emu.exceptions;

public class UnableToLoadRomException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6840972783398820100L;

	public UnableToLoadRomException() {
        super();
    }

    public UnableToLoadRomException(String message) {
        super(message);
    }
}
