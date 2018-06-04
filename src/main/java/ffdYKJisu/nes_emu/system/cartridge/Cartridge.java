package ffdYKJisu.nes_emu.system.cartridge;

import javax.inject.Inject;

public class Cartridge {

    private RomHeader header;

    Cartridge(RomHeader header) {
        this.header = header;
    }

    public RomHeader getHeader() {
        return header;
    }
}
