package ffdYKJisu.nes_emu.system.memory;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/** Tests for {@link Ram}. */
public class RamTest {

  private Ram ram;

  @Before
  public void setUp() {
    ram = new Ram();
  }

  @Test
  public void assertWriteAndReadAtZeroReturnSameValue() {
    ram.write((short) 0, (byte) 5);

    assertThat(ram.read((short) 0)).isEqualTo(5);
  }

  @Test
  public void shouldReturnSameValueAtNextMask() {
    ram.write((short) 0, (byte) 5);
    assertThat(ram.read((short) 0)).isEqualTo(5);
    assertThat(ram.read((short) 0x0800)).isEqualTo(5);
    assertThat(ram.read((short) 0x1000)).isEqualTo(5);
    assertThat(ram.read((short) 0x1800)).isEqualTo(5);
  }

  @Test
  public void writeAtEndOfRamShouldReadSameValue() {
    ram.write((short) 0x1FFF, (byte) 5);
    assertThat(ram.read((short) 0x07FF)).isEqualTo(5);
  }
}
