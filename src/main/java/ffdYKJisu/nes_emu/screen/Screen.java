package ffdYKJisu.nes_emu.screen;

/** http://stackoverflow.com/a/24449867 */
public class Screen {

	private int width;
	
	public int[] pixels;

	public Screen(int width, int height) {
		pixels = new int[width * height];
	}
	
	public void setPixel(int x_, int y_, int value_) {
		pixels[x_ + y_ * width] = value_;
	}

	public void clear() {
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = 0; // make every pixel black
		}
	}

}