package ffdYKJisu.nes_emu.screen;

import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/** http://stackoverflow.com/a/24449867 */
public class Image {
	
	private final Screen screen;
	private final BufferedImage image;
	private final int[] pixels;
	private final int _width;
	private final int _height;
	
	public Image(int width_, int height_) {
		_width = width_;
		_height = height_;
		screen = new Screen(width_, height_);
		image = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
	}
	
	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			return;
		}

		screen.clear();
		screen.render();

		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = screen.pixels[i];
		}

		Graphics g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, _width, _height, null);
		g.dispose();
		bs.show();
	}
	
	public void setPixel(int x_, int y_, int value_) {
		screen.setPixel(x_, y_, value_);
	}

	private void createBufferStrategy(int i) {
		// TODO Auto-generated method stub

	}

	private BufferStrategy getBufferStrategy() {
		// TODO Auto-generated method stub
		return null;
	}

}
