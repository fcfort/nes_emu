package ffdYKJisu.nes_emu.system.cartridge;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class RomHeader {

  public static RomHeader create(
      int mapperNumber,
      int chrBanksCount,
      int ramBanksCount,
      int prgBanksCount,
      Mirroring mirroring) {
    return new AutoValue_RomHeader(
        mapperNumber, chrBanksCount, ramBanksCount, prgBanksCount, mirroring);
  }

  abstract int mapperNumber();

  abstract int chrBanksCount();

  abstract int ramBanksCount();

  abstract int prgBanksCount();

  abstract Mirroring mirroring();
}
