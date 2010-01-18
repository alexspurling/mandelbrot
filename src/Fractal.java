import java.awt.image.BufferedImage;

public interface Fractal {

	/**
	 * Renders the fractal. The method returns either when rendering is complete,
	 * or when rendering is cancelled. If rendering was cancelled, the method
	 * returns false, otherwise it returns true.
	 * @param xPos
	 * @param yPos
	 * @param magnification
	 * @param iterations
	 * @return
	 */
	public boolean drawFractal(double xPos, double yPos, long magnification, int iterations);

	/**
	 * Resizes the fractal to the given width and height in pixels. 
	 * @param width
	 * @param height
	 */
	public void setSize(int width, int height);

	/**
	 * Returns the buffered image containing the latest rendered fractal. This
	 * can be read even when the fractal is currently being rendered.
	 * @return
	 */
	public BufferedImage getBufferedImage();

	/**
	 * Cancels any rendering and returns only when rendering has stopped.
	 */
	public void cancel();

}