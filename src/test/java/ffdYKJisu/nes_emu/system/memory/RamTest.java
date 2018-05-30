package ffdYKJisu.nes_emu.system.memory;

import com.google.common.flogger.FluentLogger;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Tests for {@link Ram}. */
public class RamTest {

    private static final FluentLogger logger = FluentLogger.forEnclosingClass();

    Ram ram;

    @Before
    public void setUp() {
        ram = new Ram();
    }

    @Test
    public void assertWriteAndReadAtZeroReturnSameValue() {
        ram.write((short) 0, (byte) 5);

        assertEquals(5, ram.read((short) 0));
    }


    @Test
    public void testReadRAM() {
        assertEquals(0, ram.read((short)0));
    }

    @Test
    public void testWriteRAM() {
        ram.write((short)0,(byte)1);
        assertEquals(1, ram.read((byte)0));
        assertEquals(1, ram.read((short)0));
    }

    @Test
    public void testWriteRAMHigh() {
        ram.write((short)0x100,(byte)10);
        assertEquals(10, ram.read((short)0x100));
    }

}
