package ffdYKJisu.nes_emu.cartridge;

import static com.google.common.truth.Truth.assertThat;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.cartridge.Cartridge;
import ffdYKJisu.nes_emu.system.cartridge.CartridgeFactory;
import ffdYKJisu.nes_emu.system.cartridge.Mirroring;
import ffdYKJisu.nes_emu.system.cartridge.RomHeader;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link CartridgeFactory}. */
public class CartridgeFactoryTest {

  Cartridge c;

  @Before
  public void setUp() throws UnableToLoadRomException {
    c =
        new CartridgeFactory()
            .fromInputStream(ClassLoader.getSystemResourceAsStream("Pac-Man (U) [!].nes"));
  }

  @Test
  public void testPacManHasOnePrgBankOneChrBankAndHorizontalMirroring() {
    assertThat(c.getHeader()).isEqualTo(RomHeader.create(0, 1, 0, 1, Mirroring.HORIZONTAL));
  }
}
