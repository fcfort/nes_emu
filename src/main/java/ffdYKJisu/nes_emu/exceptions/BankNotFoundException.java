/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ffdYKJisu.nes_emu.exceptions;

/**
 *
 * @author Administrator
 */
public class BankNotFoundException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1540532991088396774L;

	public BankNotFoundException(String msg) {
          super(msg);
    }

}
