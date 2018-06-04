package ffdYKJisu.nes_emu.system;

import com.google.common.collect.Lists;
import ffdYKJisu.nes_emu.exceptions.BankNotFoundException;
import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.cartridge.Mirroring;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * A cartridge that can be loaded into an NES 
 * @author fe01106
 */
public class Cartridge {

    private static final Logger logger = LoggerFactory.getLogger(Cartridge.class);
    
    private byte[] romData;
    private int num16PRGBanks = 0;
    private int num8CHRBanks = 0;
    private int num8RAMBanks = 1;
    private static final int iNESOffset = 16;
    private int mapperNumber;
    /** Stores the mirroring (horizontal/vertical) of the cartridge */
    private Mirroring mirroring;
    
    public Cartridge(InputStream is) throws UnableToLoadRomException {
        this.loadRom(is);
        this.setValuesFromHeader();
    }

    public Cartridge(File file) throws UnableToLoadRomException {
        this.loadRomFromFile(file);
        this.setValuesFromHeader();
    }

    /**
     * Defines the various kinds of banks possible in a cartridge
     * <p>
     * <code>ProgramRom</code> - 16KB Program code
     * <p>
     * <code>CharacterRom</code> - 8KB Character data
     * <p>
     * <code>RAM8</code> - 8KB RAM space
     */
    public enum Bank {
        PRG16(0x4000), // 16KB
        CHR8(0x2000), // 8KB
        RAM8(0x2000); // 8KB
        public final int length;

        Bank(double size) {
            this.length = (int) size;
        }
    }

    Byte getByte(int index) {
        try {
            return (Byte) romData[index];
        } catch (ArrayIndexOutOfBoundsException ex) {
            logger.error("Accessing romdata outside of bounds");
        }
        return 0;
    }

    public void loadRom(InputStream is) throws UnableToLoadRomException {
        try {
            romData = getBytesFromInput(is);
        } catch (IOException e) {
            throw new UnableToLoadRomException("Unable to load rom from input stream ");
        }
    }

    public void loadRomFromFile(File file) throws UnableToLoadRomException {
        try {
            romData = getBytesFromInput(new FileInputStream(file));
        } catch (IOException ex) {
            throw new UnableToLoadRomException("Failed to load rom from file " + file);
        }
    }

    private byte[] getBytesFromInput(InputStream is) throws IOException {
        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.

        // Create the byte array to hold the data
        List<Byte> bytes = Lists.newArrayList();

        boolean isDoneReading = false;
        // dis.available() returns 0 if the file does not have more lines.
        while (!isDoneReading) {
            int byteValue = is.read();
            if (byteValue != -1) {
                bytes.add((byte) byteValue);
            } else {
                isDoneReading = true;
            }
        }
        is.close();
        return ArrayUtils.toPrimitive(bytes.toArray(new Byte[] {}));
    }

    String printCartridgeData() {
        StringBuilder output = new StringBuilder();
        output.append("There are " + this.num16PRGBanks + " ProgramRom Bank(s)\n");
        for (int i = 0; i < this.num16PRGBanks; i++) {
            output.append("ProgramRom Bank " + i + "\n");
            byte[] temp = null;
            try {
                temp = this.get16PRGBank(i);
            } catch (BankNotFoundException ex) {
                logger.warn("Could not find bank at index " + i);
            }
            for (int j = 0; j < temp.length; j++) {
                // System.out.printf("%X", temp[j]);
                output.append(String.format("%X", temp[j]));
            }
        }
        // System.out.print("There are " + this.num8CHRBanks +
        // " CharacterRom Bank(s)\n");
        output.append("\nThere are " + this.num8CHRBanks + " CharacterRom Bank(s)\n");
        for (int i = 0; i < this.num8CHRBanks; i++) {
            output.append("CharacterRom Bank " + i + "\n");
            byte[] temp = null;
            try {
                temp = this.get8CHRBank(i);
            } catch (BankNotFoundException ex) {
                logger.warn("Could not find bank at index " + i);
            }
            for (int j = 0; j < temp.length; j++) {
                // System.out.printf("%X", temp[j]);
                output.append(String.format("%X", temp[j]));
            }
            output.append("\n");
        }
        return output.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(printHeaders());
        sb.append(printCartridgeData());
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
        
        byte mapperLowerNybble = (byte) ((romData[6] & 0xF0) >>> 4);
        byte mapperUpperNybble = (byte) ((romData[7] & 0xF0));
        byte mapperNumber = (byte) (mapperUpperNybble | mapperLowerNybble);
        
        logger.info("Mapper number: {} from lower {} and upper {}", new Object[] {mapperLowerNybble, mapperUpperNybble, mapperNumber});
        
        if (romData[8] == 0) {
            num8RAMBanks = 1;
        } else {
            num8RAMBanks = romData[8];
        }
        
        logger.info("Got {} 16 PRG Banks and {} 8 CHR banks", num16PRGBanks, num8CHRBanks);
    }

    /**
     * Returns the values read from the iNES header as a String
     * 
     * @return Values from the iNES header presented in an easy-to-read String
     */
    String printHeaders() {
        StringBuffer sb = new StringBuffer();
        sb.append(num16PRGBanks + " 16KB PRG Banks" + "\n");
        sb.append(num8CHRBanks + " 8KB CHR Banks" + "\n");
        sb.append(num8RAMBanks + " 8KB RAM Banks" + "\n");
        sb.append(mirroring + " Mirroring" + "\n");
        // System.out.print(sb.toString());
        return sb.toString();
    }

    /**
     * Reads the specified PRG-ROM bank from the cartridge and returns a Byte[]
     * array of the bytes contained in the bank. Always 16KB in size.
     * 
     * @param bankNum
     *            Number of the bank you want from the cartridge starting from
     *            zero
     * @return Byte array of the specific bank requested
     * @throws BankNotFoundException.bankNotFoundException
     */
    public byte[] get16PRGBank(int bankNum) throws BankNotFoundException {
        if (bankNum > this.num16PRGBanks) {
            throw new BankNotFoundException("Bank " + bankNum
                    + " doesn't exist");
        }
        int bankLength = Bank.PRG16.length;
        byte[] bank = new byte[bankLength];
        for (int i = 0; i < bankLength; i++) {
            // System.out.println("Bank length " + bankLength + " Bank offset "
            // +
            // this.getBankOffset(Bank.ProgramRom, bankNum) + " index " + i);
            bank[i] = romData[this.getBankOffset(Bank.PRG16, bankNum) + i];
        }
        return bank;
    }

    /**
     * Reads the specified CHR-ROM bank from the cartridge and returns a Byte[]
     * array of the bytes contained in the bank. Always 8KB in size.
     * 
     * @param bankNum
     *            Number of the bank you want from the cartridge starting from
     *            zero
     * @return Byte array of the specific bank requested
     * @throws BankNotFoundException.bankNotFoundException
     */
    public byte[] get8CHRBank(int bankNum) throws BankNotFoundException {
        if (bankNum > this.num8CHRBanks) {
            throw new BankNotFoundException("Bank " + bankNum
                    + " doesn't exist");
        }
        int bankLength = Bank.CHR8.length;
        byte[] bank = new byte[bankLength];
        for (int i = 0; i < bankLength; i++) {
            bank[i] = romData[this.getBankOffset(Bank.CHR8, bankNum) + i];
        }
        return bank;
    }

    private int getBankOffset(Bank bankType, int bankNum) {
        if (bankType.equals(Bank.PRG16)) {
            return iNESOffset + Bank.PRG16.length * bankNum;
        } else if (bankType.equals(Bank.CHR8)) {
            int offset = iNESOffset + Bank.PRG16.length
                    * this.num16PRGBanks + Bank.CHR8.length * bankNum;
            return offset;
        } else {
            return iNESOffset;
        }
    }
}
