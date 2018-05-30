package ffdYKJisu.nes_emu.system.memory;

public class CanonicalAddress {

    private final short address;
    private final AddressLocation addressLocation;

    public CanonicalAddress(short address, AddressLocation addressLocation) {
        this.address = address;
        this.addressLocation = addressLocation;
    }

    public short getAddress() {
        return address;
    }

    public AddressLocation getAddressLocation() {
        return addressLocation;
    }
}
