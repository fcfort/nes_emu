package ffdYKJisu.nes_emu.screen;

/** http://stackoverflow.com/a/24449867 */
public class Screen {

	private int width, height;

	public int[] pixels;

	public Screen(int width, int height) {
		this.width = width;
		this.height = height;

		pixels = new int[width * height];
	}

	public void render() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				pixels[x + y * width] = 0xFFFFFF; // make every pixel white
			}
		}
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