import java.awt.Canvas;


/**
 * The FractalExecutor coordinates and executes events on the Fractal object. 
 * @author Alex Spurling
 *
 */
public class FractalExecutor implements Runnable {

	private Fractal fractal;
	private Canvas canvas;
	private boolean resized = false;
	private int width;
	private int height;
	private long magnification = 1;
	private int iterations = 100;
	public final Object redrawLock = new Object();
	
	//The x and y to centre on
	private int x;
	private int y;
	
	public FractalExecutor(Canvas canvas, Fractal fractal, int width, int height) {
		this.canvas = canvas;
		this.fractal = fractal;
		resize(width, height);
	}
	
	public void zoomIn(int x, int y) {
		zoom(x, y, magnification * 2, iterations + 75);
	}
	
	public void zoomOut(int x, int y) {
		if (magnification > 1) {
			zoom(x, y, magnification / 2, iterations - 75);
		}
	}
	
	private void zoom(int x, int y, long magnification, int iterations) {
		this.x = x;
		this.y = y;
		this.magnification = magnification;
		this.iterations = iterations;
		cancel();
		tiggerEvent();
	}
	
	private void setSize(int width, int height) {
		this.width = width;
		this.height = height;
		x = width / 2;
		y = height / 2;
	}

	public void resize(int width, int height) {
		setSize(width, height);
		resized = true;
		cancel();
		tiggerEvent();
	}

	/**
	 * Runs an infinite loop in order to synchronise events for
	 * drawing and resizing the Fractal
	 */
	@Override
	public void run() {
		while(true) {
			if (resized) {
				System.out.println("resizing canvas");
				//Create a buffered image to store the rendered fractal
				//I = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				fractal.setSize(width, height);
				resized = false;
			}
			synchronized (redrawLock) {
				if (fractal.drawFractal(x, y, magnification, iterations)) {
					//Only repaint if the fractal rendered without cancellation
					canvas.repaint();
				}
				waitForEvent();
			}
		}
	}
	
	/**
	 * Waits on the redrawLock object for new events
	 */
	private void waitForEvent() {
		try {
			synchronized(redrawLock) {
				redrawLock.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void tiggerEvent() {
		synchronized(redrawLock) {
			redrawLock.notify();
		}
	}
	
	public void cancel() {
		fractal.cancel();
	}
	
}