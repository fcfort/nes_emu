package ffdYKJisu.nes_emu.screen;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Random;

import javax.swing.JFrame;

/** http://www.java-gaming.org/index.php?topic=30362.0 */
public class Image extends Canvas {
	
	private final BufferedImage bimg;
	private final int[] pixels;
	private final int _width;
	private final JFrame frame;
	
	public Image(int width, int height) {
		_width = width;
		bimg = new BufferedImage(_width, height, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) bimg.getRaster().getDataBuffer()).getData();
		
		Dimension size = new Dimension(width, height);	
		this.setMaximumSize(size);
		this.setSize(size);
		this.setMinimumSize(size);
		
		frame = new JFrame();
		frame.add(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	public void setPixel(int x, int y, int pixel) {
		pixels[_width * y + x] = pixel;
	}

	public void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if (bs == null) {
			if (this.getFocusCycleRootAncestor() != null)
				this.createBufferStrategy(2);
			return;
		}

		Graphics g = bs.getDrawGraphics();

		g.drawImage(bimg, 0, 0, this.getWidth(), this.getHeight(), null);

		g.dispose();
		bs.show();
	}

	public static void main(String[] args) {
		Image game = new Image(256, 240);

		
		for (int i = 0; i < 6; i++) {
			Random r = new Random();
			for (int x = 0; x < 256; x++) {
				for (int y = 0; y < 240; y++) {
					game.setPixel(x, y, r.nextInt());
				}
			}
			game.render();
		}
	}
}