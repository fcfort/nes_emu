package ffdYKJisu.nes_emu.screen;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Image2 {

	BufferStrategy _bs;
	
	Image2(int width_, int height_) {
		// create a frame to contain our game
		JFrame container = new JFrame("Space Invaders 101");
				
		// get hold the content of the frame and set up the 
		// resolution of the game
		JPanel panel = (JPanel) container.getContentPane();
		panel.setPreferredSize(new Dimension(width_, height_));
		panel.setLayout(null);
				
		// setup our canvas size and put it into the content of the frame
		Canvas c = new Canvas();
		c.setBounds(0,0,width_,height_);
		panel.add(c, this);
		// Since the canvas we're working with is going to be actively redrawn (i.e. 
		// accelerated graphics) we need to prevent Java AWT attempting to redraw our 
		// surface. Finally, we get the window to resolve its size, prevent the user 
		// resizing it and make it visible.
		// Tell AWT not to bother repainting our canvas since we're
		// going to do that our self in accelerated mode
		c.setIgnoreRepaint(true);
				
		// finally make the window visible 
		container.pack();
		container.setResizable(false);
		container.setVisible(true);
		
		c.createBufferStrategy(2);
		_bs = c.getBufferStrategy();
	}
	
	public static void main(String... args_) {
		Image2 i = new Image2(10, 10);
		
		Graphics2D g = (Graphics2D) i._bs.getDrawGraphics();
	}
}
