package ffdYKJisu.nes_emu.system.memory;

import ffdYKJisu.nes_emu.util.UnsignedShorts;

import javax.inject.Inject;

import static ffdYKJisu.nes_emu.util.HexUtils.toHex;

public class AddressMapper {

  @Inject
  AddressMapper() {}

  public AddressLocation getAddressLocation(short address_) {
    if (inRange(address_, 0, 0x1FFF)) {
      return AddressLocation.RAM;
    } else if (inRange(address_, 0x2000, 0x3FFF)) {
      return AddressLocation.PPUio;
    } else if (inRange(address_, 0x4000, 0x4017)) {
      return AddressLocation.APUio;
    } else if (inRange(address_, 0x4020, 0xFFFF)) {
      return AddressLocation.CARTRIDGE;
    } else {
      throw new UnsupportedOperationException("Uncategorized address " + toHex(address_));
    }
  }

  private static boolean inRange(short address, int lowInclusive, int highInclusive) {
    return UnsignedShorts.compare(address, (short) lowInclusive) >= 0
        && UnsignedShorts.compare(address, (short) highInclusive) <= 0;
  }
}
