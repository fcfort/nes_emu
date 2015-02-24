package ffdYKJisu.nes_emu.exceptions;

public class OpcodeExecutionException extends RuntimeException {

	private static final long serialVersionUID = 1923103205866993061L;

	public OpcodeExecutionException(Throwable e_, String template_, Object ... args_) {		
          super(String.format(template_, args_), e_);
	}
}
