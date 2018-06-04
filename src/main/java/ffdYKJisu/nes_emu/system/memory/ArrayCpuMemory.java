/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ffdYKJisu.nes_emu.system.memory;

import ffdYKJisu.nes_emu.exceptions.BankNotFoundException;
import ffdYKJisu.nes_emu.exceptions.InvalidAddressException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.cpu.CPU;
import ffdYKJisu.nes_emu.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ffdYKJisu.nes_emu.util.HexUtils.toHex;

/**
 * New version of memory based on shorts and bytes instead of encapsulated data types uShort and
 * uByte;
 */
public class ArrayCpuMemory implements Addressable {

  private static Logger logger = LoggerFactory.getLogger(ArrayCpuMemory.class);

  private static final int SRAM_LEN = 0x2000;

  private static final int APU_IO_LEN = 0x20;
  private static final int APU_IO_OFFSET = 0x4000;

  private static final int BANK_LEN = 0x4000; // 16kB
  private static final int PRGROM_LEN = BANK_LEN * 2;
  private static final int PRGROM_OFFSET = 0x8000;
  private final byte[] PRGROM;
  private static final short STACK_OFFSET = 0x100;
  private final byte[] SRAM;
  private final byte[] EROM;

  private final CPU cpu;
  private final Ram ram;
  private final PpuIo ppuIo;
  private final AddressMapper addressMapper;

  public ArrayCpuMemory(CPU cpu_) {
    cpu = cpu_;
    EROM = null;
    ram = new Ram();
    addressMapper = new AddressMapper();
    ppuIo = new PpuIo();
    PRGROM = new byte[PRGROM_LEN];
    SRAM = new byte[SRAM_LEN];
  }

  @Override
  public byte read(short address) {
    byte val;

    switch (addressMapper.getAddressLocation(address)) {
      case PPUio:
        val = ppuIo.read(address);
        break;
      case PRGROM:
        int romAddress = address - (short) PRGROM_OFFSET;
        val = PRGROM[romAddress];
        break;
      case RAM:
        val = ram.read(address);
        break;
      case APUio:
        logger.info("Ignoring APU I/O read at address {}", HexUtils.toHex(address));
        val = 0;
        break;
      case SRAM:
      default:
        throw new UnsupportedOperationException("Unrecognized address " + toHex(address));
    }

    logger.info(
        "Read of {} at address {} got val {}",
        new Object[] {addressMapper.getAddressLocation(address).toString(), toHex(address), toHex(val)});

    return val;
  }

  @Override
  public void write(short address, byte val) throws InvalidAddressException {
    logger.info("Writing {} to address {}", toHex(val), toHex(address));
    switch (addressMapper.getAddressLocation(address)) {
      case PPUio:
        ppuIo.write(address, val);
        break;
      case CARTRIDGE:
        throw new InvalidAddressException("In PRGROM");
      case RAM:
        ram.write(address, val);
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

}
