/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ffdYKJisu.nes_emu.system;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

import ffdYKJisu.nes_emu.exceptions.bankNotFoundException;

/**
 *
 * @author fe01106
 */
public class Cartridge {

    byte[] romData;
    int num16PRGBanks = 0;
    int num8CHRBanks = 0;
    int num8RAMBanks = 1;
    private final int iNESOffset = 16;

    public Cartridge(InputStream is) {
        this.loadRom(is);
    }
    
    public Cartridge(File file) {
        this.loadRomFromFile(file);
        this.setValuesFromHeader();
    }
    
    /**
     * Defines the various kinds of banks possible in a cartridge
     * <p>
     * <code>PRG16</code> - 16KB Program code
     * <p>
     * <code>CHR8</code> - 8KB Character data
     * <p>
     * <code>RAM8</code> - 8KB RAM space
     */
    enum Bank {

        PRG16(0x4000), //16KB
        CHR8(0x2000), // 8KB
        RAM8(0x2000);	// 8KB
        public final int length;

        Bank(double size) {
            this.length = (int) size;
        }
    }

    enum Mirroring {
        VERTICAL, HORIZONTAL
    };
    
    /** Stores the mirroring (horizontal/vertical) of the cartridge */
    Mirroring mirroring;
    
    Byte getByte(int index) {
        try {
            return (Byte) romData[index];
        } catch (ArrayIndexOutOfBoundsException ex) {
            Logger.getLogger(Cartridge.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
    }

    void loadRom(InputStream is) {
        
    }
    
    void loadRomFromFile(File file) {
        try {
            romData = getBytesFromInput(new FileInputStream(file));
        } catch (IOException ex) {
            Logger.getLogger(Cartridge.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    byte[] getBytesFromInput(InputStream is) throws IOException {
        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.

        // Create the byte array to hold the data
        List<Byte> bytes = Lists.newArrayList();
        byte[] bytes = new byte[(int) length];
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        DataInputStream dis = null;

        try {
            fis = new FileInputStream(file);

            // Here BufferedInputStream is added for fast reading.
            bis = new BufferedInputStream(fis);
            dis = new DataInputStream(bis);
            int i = 0;
            // dis.available() returns 0 if the file does not have more lines.
            while (dis.available() != 0) {

                // this statement reads the line from the file and print it to
                // the console.

                dis.readFully(bytes);
            }

            // dispose all the resources after using them.
            fis.close();
            bis.close();
            dis.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    String printcartridgeData() {
        StringBuffer output = new StringBuffer();
        output.append("There are " + this.num16PRGBanks + " PRG16 Bank(s)\n");
        for (int i = 0; i < this.num16PRGBanks; i++) {
            output.append("PRG16 Bank " + i + "\n");
            Byte[] temp = null;
            try {
                temp = this.get16PRGBank(i);
            } catch (bankNotFoundException ex) {
                Logger.getLogger(Cartridge.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (int j = 0; j < temp.length; j++) {
                //System.out.printf("%X", temp[j]);
                output.append(String.format("%X", temp[j]));
            }
        }
        //System.out.print("There are " + this.num8CHRBanks + " CHR8 Bank(s)\n");
        output.append("\nThere are " + this.num8CHRBanks + " CHR8 Bank(s)\n");
        for (int i = 0; i < this.num8CHRBanks; i++) {
            output.append("CHR8 Bank " + i + "\n");
            Byte[] temp = null;
            try {
                temp = this.get8CHRBank(i);
            } catch (bankNotFoundException ex) {
                Logger.getLogger(Cartridge.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (int j = 0; j < temp.length; j++) {
                //System.out.printf("%X", temp[j]);
                output.append(String.format("%X", temp[j]));
            }
            output.append("\n");
        }
        return output.toString();
    }

    @Override public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("");
        sb.append(printHeaders());
        sb.append(printcartridgeData());
        return sb.toString();
    }
    
    /**
     * Read cartridge and read iNes header and store to cartridge
     */
    void setValuesFromHeader() {
        num16PRGBanks = (int) romData[4];
        num8CHRBanks = (int) romData[5];
        if ((romData[6] & 0x1) == 0) {
            mirroring = Mirroring.HORIZONTAL;
        } else {
            mirroring = Mirroring.VERTICAL;
        }
        if (romData[8] == 0) {
            num8RAMBanks = 1;
        } else {
            num8RAMBanks = romData[8];
        }
    }

    /**
     * Returns the values read from the iNES header as a String
     * @return Values from the iNES header presented in an easy-to-read String
     */
    String printHeaders() {
        StringBuffer sb = new StringBuffer();
        sb.append(num16PRGBanks + " 16KB PRG Banks" + "\n");
        sb.append(num8CHRBanks + " 8KB CHR Banks" + "\n");
        sb.append(num8RAMBanks + " 8KB RAM Banks" + "\n");
        sb.append(mirroring + " Mirroring" + "\n");
        //System.out.print(sb.toString());
        return sb.toString();
    }

    /**
     * Reads the specified PRG-ROM bank from the cartridge and returns a Byte[]
     * array of the bytes contained in the bank. Always 16KB in size.
     * @param bankNum Number of the bank you want from the cartridge starting
     * from zero
     * @return Byte array of the specific bank requested
     * @throws nes.bankNotFoundException
     */
    Byte[] get16PRGBank(int bankNum) throws bankNotFoundException {
        if (bankNum > this.num16PRGBanks) {
            throw new bankNotFoundException("Bank " + bankNum + " doesn't exist");
        }
        int bankLength = Bank.PRG16.length;
        Byte[] bank = new Byte[bankLength];
        for (int i = 0; i < bankLength; i++) {
            //System.out.println("Bank length " + bankLength + " Bank offset " +
            //	this.getBankOffset(Bank.PRG16, bankNum) + " index " + i);
            bank[i] = romData[this.getBankOffset(Bank.PRG16, bankNum) + i];
        }
        return bank;
    }

        /**
     * Reads the specified CHR-ROM bank from the cartridge and returns a Byte[]
     * array of the bytes contained in the bank. Always 8KB in size.
     * @param bankNum Number of the bank you want from the cartridge starting
     * from zero
     * @return Byte array of the specific bank requested
     * @throws nes.bankNotFoundException
     */
    Byte[] get8CHRBank(int bankNum) throws bankNotFoundException {
        if (bankNum > this.num8CHRBanks) {
            throw new bankNotFoundException("Bank " + bankNum + " doesn't exist");
        }
        int bankLength = Bank.CHR8.length;
        Byte[] bank = new Byte[bankLength];
        for (int i = 0; i < bankLength; i++) {
            bank[i] = romData[this.getBankOffset(Bank.CHR8, bankNum) + i];
        }
        return bank;
    }

    private int getBankOffset(Bank bankType, int bankNum) {
        if (bankType.equals(Bank.PRG16)) {
            return this.iNESOffset + Bank.PRG16.length * bankNum;
        } else if (bankType.equals(Bank.CHR8)) {
            int offset = this.iNESOffset +
                    Bank.PRG16.length * this.num16PRGBanks +
                    Bank.CHR8.length * bankNum;
            return offset;
        } else {
            return this.iNESOffset;
        }
    }
}
