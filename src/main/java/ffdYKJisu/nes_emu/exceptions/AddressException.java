/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ffdYKJisu.nes_emu.exceptions;

/**
 *
 * @author Administrator
 */
public class AddressException extends Exception {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6702393265914768379L;

	public AddressException(String msg) {
          super(msg);
	}
}
