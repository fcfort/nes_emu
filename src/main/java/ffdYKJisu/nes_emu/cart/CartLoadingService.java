package ffdYKJisu.nes_emu.cart;

import com.google.common.flogger.FluentLogger;
import ffdYKJisu.nes_emu.exceptions.BankNotFoundException;
import ffdYKJisu.nes_emu.system.Cartridge;
import ffdYKJisu.nes_emu.system.memory.Addressable;

public final class CartLoadingService {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private static final int BANK_LEN = 0x4000; // 16kB

  private final Addressable cpuMemory;

  public CartLoadingService(Addressable cpuMemory) {
    this.cpuMemory = cpuMemory;
  }

  public void writeCartToMemory(Cartridge cart) throws BankNotFoundException {
    logger.atInfo().log("Loading cart into memory");

    byte[] bank = cart.get16PRGBank(0);

    // Copy to lower bank
    for (int i = 0; i < BANK_LEN; i++) {
//      cpuMemory.write();
//      PRGROM[i] = bank[i];
    }

    // Copy to upper bank
    for (int i = 0; i < BANK_LEN; i++) {
//      PRGROM[i + BANK_LEN] = bank[i];
    }
  }
}
