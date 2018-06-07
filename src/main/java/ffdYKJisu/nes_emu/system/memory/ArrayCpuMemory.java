/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ffdYKJisu.nes_emu.system.memory;

import ffdYKJisu.nes_emu.exceptions.InvalidAddressException;
import ffdYKJisu.nes_emu.system.cartridge.Cartridge;
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

  private final Ram ram;
  private final PpuIo ppuIo;
  private final AddressMapper addressMapper;
  private Cartridge cartridge;

  public ArrayCpuMemory(Cartridge cartridge) {
    this.cartridge = cartridge;
    this.ram = new Ram();
    this.addressMapper = new AddressMapper();
    this.ppuIo = new PpuIo();
  }

  @Override
  public byte read(short address) {
    byte val;

    switch (addressMapper.getAddressLocation(address)) {
      case PPUio:
        val = ppuIo.read(address);
        break;
      case CARTRIDGE:
        val = cartridge.read(address);
        break;
      case RAM:
        val = ram.read(address);
        break;
      case APUio:
        logger.info("Ignoring APU I/O read at address {}", HexUtils.toHex(address));
        val = 0;
        break;
      default:
        throw new UnsupportedOperationException("Unrecognized address " + toHex(address));
    }

    logger.debug(
        "Read of {} at address {} got val {}",
        new Object[] {
          addressMapper.getAddressLocation(address).toString(), toHex(address), toHex(val)
        });

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
        cartridge.write(address, val);
      case RAM:
        ram.write(address, val);
        break;
      case APUio:
        logger.info(
            "Ignoring APU I/O write of {} to address {}",
            HexUtils.toHex(val),
            HexUtils.toHex(address));
        break;
      default:
        throw new UnsupportedOperationException();
    }
  }

  public void setCartridge(Cartridge cartridge) {
    this.cartridge = cartridge;
  }
}
