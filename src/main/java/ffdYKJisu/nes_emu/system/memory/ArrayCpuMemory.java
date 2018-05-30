/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ffdYKJisu.nes_emu.system.memory;

import static ffdYKJisu.nes_emu.util.HexUtils.toHex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Shorts;

import ffdYKJisu.nes_emu.exceptions.BankNotFoundException;
import ffdYKJisu.nes_emu.exceptions.InvalidAddressException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.cpu.CPU;
import ffdYKJisu.nes_emu.util.HexUtils;
import ffdYKJisu.nes_emu.util.UnsignedShorts;

/**
 * New version of memory based on shorts and bytes instead of encapsulated data types uShort and
 * uByte;
 */
public class ArrayCpuMemory implements CpuMemory {

  private static Logger logger = LoggerFactory.getLogger(ArrayCpuMemory.class);

  private static final int SRAM_LEN = 0x2000;
  private static final int RAM_LEN = 0x2000;

  private static final int PPU_IO_LEN = 8;
  private static final int PPU_IO_OFFSET = 0x2000;

  private static final int APU_IO_LEN = 0x20;
  private static final int APU_IO_OFFSET = 0x4000;

  private static final int BANK_LEN = 0x4000; // 16kB
  private static final int PRGROM_LEN = BANK_LEN * 2;
  private static final int PRGROM_OFFSET = 0x8000;
  private final byte[] PRGROM;
  private static final short STACK_OFFSET = 0x100;
  private final byte[] SRAM;
  private final byte[] EROM;
  private final byte[] PPUio;
  private final byte[] RAM;

  private final CPU _cpu;

  public ArrayCpuMemory(CPU cpu_) {
    _cpu = cpu_;
    EROM = null;
    RAM = new byte[RAM_LEN];
    PRGROM = new byte[PRGROM_LEN];
    SRAM = new byte[SRAM_LEN];
    PPUio = new byte[PPU_IO_LEN];
  }

  @Override
  public byte read(short address) {
    byte val = 0;
    switch (getAddressLocation(address)) {
      case PPUio:
        int ppuOffset = ppuioAddress(address);
        val = _cpu.getNES().getPPU().read(address);
        logger.info("PPU/VRAM I/O read value {} at address {}", toHex(val), toHex(address));
        return val;
      case PRGROM:
        int romAddress = address - (short) PRGROM_OFFSET;
        val = PRGROM[romAddress];
        logger.debug(
            "PGR-ROM read value {} at address {} with array index {}",
            new Object[] {toHex(val), toHex(address), toHex((short) romAddress)});
        return val;
      case RAM:
        val = RAM[address]; // PPU/VRAM I/O Registers
        logger.debug("RAM I/O read value {} at address {}", toHex(val), toHex(address));
        return val;
      case APUio:
        logger.info("Ignoring APU I/O read at address {}", HexUtils.toHex(address));
        return 0;
      case SRAM:
      default:
        throw new UnsupportedOperationException("Unrecognized address " + toHex(address));
    }
  }

  @Override
  public void write(short address, byte val) throws InvalidAddressException {
    logger.info("Writing {} to address {}", toHex(val), toHex(address));
    switch (getAddressLocation(address)) {
      case PPUio:
        PPUio[ppuioAddress(address)] = val;
        break;
      case PRGROM:
        throw new InvalidAddressException("In PRGROM");
      case RAM:
        RAMwrite(address, val);
        break;
      case APUio:
        logger.info(
            "Ignoring APU I/O write of {} to address {}",
            HexUtils.toHex(val),
            HexUtils.toHex(address));
        break;
      case SRAM:
      default:
        throw new UnsupportedOperationException();
    }
  }

  public void writeCartToMemory(Cartridge cart) throws BankNotFoundException {
    logger.info("Loading cart into memory");

    byte[] bank = cart.get16PRGBank(0);
    // Copy to lower bank
    // System.out.print(PRGROM[i] + " ");
    System.arraycopy(bank, 0, PRGROM, 0, BANK_LEN);
    // Copy to upper bank
    System.arraycopy(bank, 0, PRGROM, 16384, BANK_LEN);
  }

  public void push(byte address_, byte val_) {
    write((short) (Byte.toUnsignedInt(address_) + STACK_OFFSET), val_);
  }

  public byte pop(byte address_) {
    return read((short) (Byte.toUnsignedInt(address_) + STACK_OFFSET));
  }

  private byte read(byte addrH, byte addrL) {
    short address = (short) (addrH << 8 + addrL);
    return read(address);
  }

  /**
   * Used to access zero page address using a byte as the address
   *
   * @param zeroPageAddress A byte referencing a zero page address (first page).
   * @return byte from that address on the zero page.
   */
  private byte read(byte zeroPageAddress) {
    return read(Shorts.fromBytes((byte) 0x00, zeroPageAddress));
  }

  private int ppuioAddress(short address) {
    short relativeIndex = (short) (address - PPU_IO_OFFSET);
    int ppuOffset = relativeIndex % PPU_IO_LEN;
    return ppuOffset;
  }

  private void write(byte zeroPageAddress, byte val) throws InvalidAddressException {
    write((short) zeroPageAddress, val);
  }

  private void write(byte addrH, byte addrL, byte val) throws InvalidAddressException {
    write((short) (addrH << 8 + addrL), val);
  }

  private void RAMwrite(short address, byte val) {
    int unsignedAddress = Short.toUnsignedInt(address);
    int ramOffset = unsignedAddress % 0x800;
    RAM[ramOffset] = val;
    RAM[ramOffset + 0x0800] = val;
    RAM[ramOffset + 0x1000] = val;
    RAM[ramOffset + 0x1800] = val;
  }

  private static AddressLocation getAddressLocation(short address_) {
    if (UnsignedShorts.compare(address_, (short) 0x2000) < 0) {
      return AddressLocation.RAM;
    } else if (UnsignedShorts.compare(address_, (short) 0x2000) >= 0
        && UnsignedShorts.compare(address_, (short) 0x4000) < 0) {
      return AddressLocation.PPUio;
    } else if (UnsignedShorts.compare(address_, (short) 0x4000) >= 0
        && UnsignedShorts.compare(address_, (short) 0x4020) < 0) {
      return AddressLocation.APUio;
    } else if (UnsignedShorts.compare(address_, (short) 0x8000) >= 0) {
      return AddressLocation.PRGROM;
    } else {
      throw new UnsupportedOperationException("Uncategorized address " + toHex(address_));
    }
  }
}
