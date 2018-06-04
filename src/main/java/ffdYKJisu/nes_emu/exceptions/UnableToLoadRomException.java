package ffdYKJisu.nes_emu.exceptions;

public class UnableToLoadRomException extends RuntimeException {

  public UnableToLoadRomException(String message) {
    super(message);
  }

  public UnableToLoadRomException(Exception e) {
    super(e);
  }
}
