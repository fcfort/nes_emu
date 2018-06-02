package ffdYKJisu.nes_emu.system.memory;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/** Tests for {@link PpuIo}. */
public class PpuIoTest {

  private PpuIo ppuIo;

  @Before
  public void setUp() {
    ppuIo = new PpuIo();
  }

  @Test
  public void assertWriteAndReadAtFirstRegisterReturnsSameValue() {
    ppuIo.write((short) 0x2000, (byte) 5);

    assertThat(ppuIo.read((short) 0x2000)).isEqualTo(5);
  }

  @Test
  public void assertWriteAndReadWithMaskReturnsSameValue() {
    ppuIo.write((short) 0x2007, (byte) 5);

    assertThat(ppuIo.read((short) 0x3FFF)).isEqualTo(5);
  }
}
