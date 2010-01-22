package fractals;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Renders the Mandelbrot set fractal
 * @author Alex Spurling
 *
 */
public class Mandelbrot implements Fractal {

	private BufferedImage bufferedImage;
	private WritableRaster wr;
	
	private int width;
	private int height;
	
	private Gradient gradient;
	/** The maximum number of iterations to use for the gradient cycle */
	private final int MAX_GRAD_ITERATIONS = 500;
	
	private double initialWidth;
	private double initialHeight;
	
	private double curXStart;
	private double curYStart;
	private long curMagnification = 1;
	private volatile boolean cancelled;
	
	private volatile ExecutorService executor;
	
	public Mandelbrot(int width, int height) {
		setSize(width, height);
		
		//We always have an initial width of 3
		initialWidth = 3;
		initialHeight = initialWidth / width * height;
		
		curXStart = -0.5-initialWidth / 2;
		curYStart = 0-initialHeight / 2;

		gradient = new Gradient(MAX_GRAD_ITERATIONS,
								new Color(0, 0, 90), //Navy
								new Color(170, 255, 255), //Light blue
								new Color(255, 225, 50),  //Yellow
								new Color(157, 58, 17));  //Brown
	}

	/* (non-Javadoc)
	 * @see Fractal#drawFractal(double, double, long, int)
	 */
	public boolean drawFractal(double xPos, double yPos, long magnification, int iterations) {
		
		//Calculate the size of the rendering window in complex coordinates
		double xSize = initialWidth / magnification;
		double ySize = initialHeight / magnification;
		
		//Calculate the top and left points of the rendering window in the complex plane
		double xStart = curXStart + (initialWidth / curMagnification * xPos/width) - xSize / 2;
		double yStart = curYStart + (initialHeight / curMagnification * yPos/height) - ySize / 2;

		//Reset the cancel flag before starting
		cancelled = false;
		
		executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		for (int i = 0; i < 4; i++) {
			int x = 0;
			int y = i * (height / 4);
			int xLimit = x + width;
			int yLimit = y + (height / 4);
			executor.execute(new MandelbrotRenderer(xStart, xSize, yStart, ySize, width, height, x, y, xLimit, yLimit, iterations));
		}
		executor.shutdown();
		
		try {
			executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			System.err.println("Rendering interrupted");
			e.printStackTrace();
		}
		
		curXStart = xStart;
		curYStart = yStart;
		curMagnification = magnification;
		
		return !cancelled;
	}
	
	/**
	 * Renders a given portion of the Mandelbrot fractal. Implemented
	 * as a Runnable inner class to allow parallel calculations
	 * @author Alex Spurling
	 *
	 */
	private class MandelbrotRenderer implements Runnable {

		private double xStart;
		private double xSize;
		private double yStart;
		private double ySize;
		
		private int width;
		private int height;
		
		private int xPos;
		private int yPos;
		private int xLimit;
		private int yLimit;
		
		private int iterations;
		
		public MandelbrotRenderer(double xStart, double xSize, double yStart, double ySize, 
				int width, int height,
				int x, int y, int xLimit, int yLimit,
				int iterations) {
			
			this.xStart = xStart;
			this.xSize = xSize;
			this.yStart = yStart;
			this.ySize = ySize;
			
			this.width = width;
			this.height = height;
			
			this.xPos = x;
			this.yPos = y;
			this.yLimit = yLimit;
			this.xLimit = xLimit;
			
			this.iterations = iterations;
		}
		
		@Override
		public void run() {
			for (int y = yPos; y < yLimit; y++) {
				if (cancelled) break;
				for (int x = xPos; x < xLimit; x++) {
					int i = 0;
					double xc = 0;
					double yc = 0;
					//Get the coordinates of the x/y point on the complex plane
					//based on our current top right coordinate (xStart,yStart), and the
					//width/height of the screen at the current magnification (xSize,ySize) 
					double x0 = xStart + xSize * ((double)x / width);
					double y0 = yStart + ySize * ((double)y / height);
					while (xc*xc + yc*yc < 2*2 && i < iterations) {
						double xtemp = xc*xc - yc*yc;
					    yc = 2*xc*yc + y0;
					    xc = xtemp + x0;
					    i++;
					}
					
					wr.setPixel(x, y, getColour(i, iterations));
				}
			}
		}
		
	}
	
	private int[] getColour(int i, int maxIters) {
		//Calculate the gradient factor
		//This should be a value between 0..1 
		double gradientFactor;
		if (i == maxIters) gradientFactor = 0;
		else if (maxIters < MAX_GRAD_ITERATIONS) gradientFactor = i / (double)maxIters;
		else gradientFactor = i % MAX_GRAD_ITERATIONS / (double)MAX_GRAD_ITERATIONS;
		
		return gradient.getColor(gradientFactor);
	}

	/* (non-Javadoc)
	 * @see Fractal#setSize(int, int)
	 */
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
		bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		this.wr = bufferedImage.getRaster();
	}

	/* (non-Javadoc)
	 * @see Fractal#getBufferedImage()
	 */
	public BufferedImage getBufferedImage() {
		return bufferedImage;
	}

	/* (non-Javadoc)
	 * @see Fractal#cancel()
	 */
	public void cancel() {
		cancelled = true;
		try {
			if (executor != null) {
				executor.awaitTermination(10, TimeUnit.SECONDS);
				executor = null;
			}
		} catch (InterruptedException e) {
			System.err.println("Rendering interrupted");
			e.printStackTrace();
		}
	}

	
}
