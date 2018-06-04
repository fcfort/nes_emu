package ffdYKJisu.nes_emu.system.cartridge;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import java.io.IOException;
import java.io.InputStream;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// https://wiki.nesdev.com/w/index.php/INES
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

    return new Cartridge(header);
  }

  private static RomHeader readHeader(byte[] romData) {
    int num16PRGBanks = (int) romData[4];
    int num8CHRBanks = (int) romData[5];

    Mirroring mirroring = ((romData[6] & 0x1) == 0) ? Mirroring.HORIZONTAL : Mirroring.VERTICAL;

    byte mapperLowerNybble = (byte) ((romData[6] & 0xF0) >>> 4);
    byte mapperUpperNybble = (byte) ((romData[7] & 0xF0));
    byte mapperNumber = (byte) (mapperUpperNybble | mapperLowerNybble);

    boolean hasSram = (romData[6] & 0x10) >> 1 == 1;

    logger.info(
        "Mapper number: {} from lower {} and upper {}",
        new Object[] {mapperLowerNybble, mapperUpperNybble, mapperNumber});

    int num8RAMBanks = 0;

    if (hasSram) {
      num8RAMBanks = (romData[8] == 0) ? 1 : romData[8];
    }

    logger.info(
        "Got {} 16 PRG Banks and {} 8 CHR banks and {} ram banks",
        new Object[] {num16PRGBanks, num8CHRBanks, num8RAMBanks});

    return RomHeader.create(mapperNumber, num8CHRBanks, num8RAMBanks, num16PRGBanks, mirroring);
  }
}
