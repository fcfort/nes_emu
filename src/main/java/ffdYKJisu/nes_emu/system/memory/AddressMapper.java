package ffdYKJisu.nes_emu.system.memory;

import ffdYKJisu.nes_emu.util.UnsignedShorts;

import static ffdYKJisu.nes_emu.util.HexUtils.toHex;

public class AddressMapper {

    public CanonicalAddress getCanonicalAddress(short address) {
        int unsignedAddress = Short.toUnsignedInt(address);
        int ramOffset = unsignedAddress % 0x800;
//        RAM[ramOffset] = val;
//        RAM[ramOffset + 0x0800] = val;
//        RAM[ramOffset + 0x1000] = val;
//        RAM[ramOffset + 0x1800] = val;
    }

    public AddressLocation getAddressLocation(short address_) {
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