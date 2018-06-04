package ffdYKJisu.nes_emu.system.cartridge;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.Cartridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

public final class CartridgeFactory {

  private static final Logger logger = LoggerFactory.getLogger(CartridgeFactory.class);

  @Inject
  public CartridgeFactory() {}

  public Cartridge fromInputStream(InputStream is) {
    byte[] romData;
    try {
      romData = is.readAllBytes();
    } catch (IOException e) {
      throw new UnableToLoadRomException(e);
    }

    RomHeader header = readHeader(romData);

    return null;
  }

  private static RomHeader readHeader(byte[] romData) {
    int num16PRGBanks = (int) romData[4];
    int num8CHRBanks = (int) romData[5];

    Mirroring mirroring = ((romData[6] & 0x1) == 0) ? Mirroring.HORIZONTAL : Mirroring.VERTICAL;

    byte mapperLowerNybble = (byte) ((romData[6] & 0xF0) >>> 4);
    byte mapperUpperNybble = (byte) ((romData[7] & 0xF0));
    byte mapperNumber = (byte) (mapperUpperNybble | mapperLowerNybble);

    logger.info(
        "Mapper number: {} from lower {} and upper {}",
        new Object[] {mapperLowerNybble, mapperUpperNybble, mapperNumber});

    int num8RAMBanks = (romData[8] == 0) ? 1 : romData[8];

    logger.info("Got {} 16 PRG Banks and {} 8 CHR banks", num16PRGBanks, num8CHRBanks);

    return RomHeader.create(num8CHRBanks, num8RAMBanks, num16PRGBanks, mapperNumber, mirroring);
  }
}
