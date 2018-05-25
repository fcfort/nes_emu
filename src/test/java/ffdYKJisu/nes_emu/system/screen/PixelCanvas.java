package ffdYKJisu.nes_emu.system.screen;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import javax.swing.JFrame;

public class PixelCanvas extends Canvas {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 400;
    private static final Random random = new Random();

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        for(int x = 0; x < WIDTH; x++) {
            for(int y = 0; y < HEIGHT; y++) {
                g.setColor(randomColor());
                g.drawLine(x, y, x, y);
            }
        }
    }

    private Color randomColor() {
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();

        frame.setSize(WIDTH, HEIGHT);
        frame.add(new PixelCanvas());

        frame.setVisible(true);
    }
}