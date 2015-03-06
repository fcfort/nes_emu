package ffdYKJisu.nes_emu.system;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import ffdYKJisu.nes_emu.screen.Image;

public class ImageTest {

	Image i;
	private static int WIDTH = 256;
	private static int HEIGHT = 240;
	
	@Before
	public void initialize() {
		i = new Image(WIDTH, HEIGHT);
	}
	
	@Test
	public void test() throws InterruptedException {
		Random r = new Random(0);
		for(int x = 0; x < WIDTH; x++) {
			for(int y = 0; y < HEIGHT; y++) {
				i.setPixel(x, y, r.nextInt());
			}
		}
		i.render();
		Thread.currentThread().sleep(1000);
		i.render();
		Thread.currentThread().sleep(1000);
	}

}

