package ffdYKJisu.nes_emu.cartridge;

import ffdYKJisu.nes_emu.exceptions.UnableToLoadRomException;
import ffdYKJisu.nes_emu.system.cartridge.Cartridge;
import ffdYKJisu.nes_emu.system.cartridge.CartridgeFactory;
import ffdYKJisu.nes_emu.system.cartridge.Mirroring;
import ffdYKJisu.nes_emu.system.cartridge.RomHeader;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/** Tests for {@link CartridgeFactory}. */
public class CartridgeFactoryTest {

  private static final String PAC_MAN_ROM_RESOURCE_NAME = "Pac-Man (U) [!].nes";

  private Cartridge c;

  @Before
  public void setUp() throws UnableToLoadRomException {
    c =
        new CartridgeFactory()
            .fromInputStream(ClassLoader.getSystemResourceAsStream(PAC_MAN_ROM_RESOURCE_NAME));
  }

  @Test
  public void testPacManHasOnePrgBankOneChrBankAndHorizontalMirroring() {
    assertThat(c.getHeader()).isEqualTo(RomHeader.create(0, 1, 0, 1, Mirroring.HORIZONTAL));
  }

  @Test
  public void testPacManHasCAtFirstPrgBank() {
    assertThat(c.read((short) 0x8000)).isEqualTo('C');
  }

  @Test
  public void testPacManResetVectorIsFf00() {
    assertThat(c.read((short) 0xFFFC)).isEqualTo(0);
    assertThat(c.read((short) 0xFFFD)).isEqualTo((byte) 0xFF);
  }
}
