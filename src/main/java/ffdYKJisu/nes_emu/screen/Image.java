package ffdYKJisu.nes_emu.screen;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ffdYKJisu.nes_emu.domain.Opcode;
import ffdYKJisu.nes_emu.util.HexUtils;

/** http://stackoverflow.com/a/24449867 */
public class Image {
	
	private static final Logger logger = LoggerFactory.getLogger(Image.class);
	
	private final Screen screen;
	private final BufferedImage image;
	private final int[] pixels;
	private final int _width;
	private final int _height;
	private final Frame _frame;
	
	public Image(int width_, int height_) {
		_width = width_;
		_height = height_;
		screen = new Screen(width_, height_);
		image = new BufferedImage(_width, _height, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		
		// Configure frame
		_frame = new Frame("NES");
		_frame.pack();
		_frame.add("Center", new MainCanvas());
		_frame.setSize(new Dimension(_width, _height));
		_frame.setVisible(true);
	}
	
	public void render() {
		
		Random r = new Random();
		
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			return;
		}

		
		logger.info(Arrays.toString(screen.pixels));
		for (int i = 0; i < pixels.length; i++) {
			// pixels[i] = screen.pixels[i];
			pixels[i] = r.nextInt();			
		}

		Graphics g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, _width, _height, null);
		g.dispose();
		bs.show();
		logger.info("Done rendering image");
	}
	
	public void setPixel(int x_, int y_, int value_) {
		//logger.info("Writing pixel value {} to x,y ({},{})", new Object[] {value_, x_, y_});
		screen.setPixel(x_, y_, value_);
	}

	private void createBufferStrategy(int i) {
		_frame.createBufferStrategy(3);
	}

	private BufferStrategy getBufferStrategy() {
		return _frame.getBufferStrategy();
	}

}
