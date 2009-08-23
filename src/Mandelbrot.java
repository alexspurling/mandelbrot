import java.awt.Color;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class Mandelbrot {
	
	private WritableRaster wr;
	private int width;
	private int height;
	
	private static final int[] gradientA = new int[] {50, 50, 150};
	private static final int[] gradientB = new int[] {255, 255, 100};
	
	
	private static final List<Color> gradients = new ArrayList<Color>();
	
	private static final int numColours = 500;
	private static int[][] colorTable = new int[numColours][];
	
	private double initialWidth;
	private double initialHeight;
	
	private double curXStart;
	private double curYStart;
	private long curMagnification = 1;
	private boolean cancelled;
	
	private ExecutorService executor;
	
	public Mandelbrot(WritableRaster wr, int width, int height) {
		this.wr = wr;
		this.width = width;
		this.height = height;
		
		//We always have an initial width of 3
		initialWidth = 3;
		initialHeight = initialWidth / width * height;
		
		curXStart = -0.5-initialWidth / 2;
		curYStart = 0-initialHeight / 2;

		gradients.add(new Color(0, 0, 90)); //Navy
		gradients.add(new Color(170, 255, 255)); //Light blue
		gradients.add(new Color(255, 225, 50));  //Yellow
		gradients.add(new Color(157, 58, 17));  //Brown
		
		int curColour = 0;
		int gradesPerColour = numColours / gradients.size();  
		for (int grad = 0; grad < gradients.size(); grad++) {
			Color fromColour = gradients.get(grad);
			Color toColour = gradients.get((grad+1)%gradients.size());
			int startIndex = curColour * gradesPerColour;
			for (int i = startIndex; i < startIndex+gradesPerColour; i++) {
				int r = fromColour.getRed() + (int) ((toColour.getRed() - fromColour.getRed()) * (i-startIndex) / gradesPerColour);
				int g = fromColour.getGreen() + (int) ((toColour.getGreen() - fromColour.getGreen()) * (i-startIndex) / gradesPerColour);
				int b = fromColour.getBlue() + (int) ((toColour.getBlue() - fromColour.getBlue()) * (i-startIndex) / gradesPerColour);
				colorTable[i] = new int[] {r, g, b};
			}
			curColour++;
		}
	}

	public boolean drawFractal(double xPos, double yPos, long magnification, int iterations) {
		
		double xSize = initialWidth / magnification;
		double ySize = initialHeight / magnification;
		
		double xStart = curXStart + (initialWidth / curMagnification * xPos/width) - xSize / 2;
		double yStart = curYStart + (initialHeight / curMagnification * yPos/height) - ySize / 2;

		//Reset the cancel flag before starting
		cancelled = false;
		
		long startTime = System.currentTimeMillis();

		/*
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int i = 0;
				double xc = 0;
				double yc = 0;
				//Get the coordinates of the x/y point on the complex plane
				//based on our current top right coordinate (x/yStart), and the
				//width/height of the screen at the current magnification (x/ySize) 
				double x0 = xStart + xSize * ((double)x / width);
				double y0 = yStart + ySize * ((double)y / height);
				while (xc*xc + yc*yc < 2*2 && i < iterations) {
					double xtemp = xc*xc - yc*yc;
				    yc = 2*xc*yc + y0;
				    xc = xtemp + x0;
				    i++;
				}
				
				wr.setPixel(x, y, getColor(i, iterations));
			}
		}
		*/
		System.out.println(executor);
		executor = Executors.newFixedThreadPool(4);
		
		for (int i = 0; i < 4; i++) {
			int x = 0;
			int y = i * (height / 4);
			int xLimit = x + width;
			int yLimit = y + (height / 4);
			executor.execute(new FractalRenderer(xStart, xSize, yStart, ySize, width, height, x, y, xLimit, yLimit, iterations));
		}
		executor.shutdown();
		try {
			executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(System.currentTimeMillis() - startTime);
		
		curXStart = xStart;
		curYStart = yStart;
		curMagnification = magnification;
		
		return !cancelled;
	}
	
	private class FractalRenderer implements Runnable {

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
		
		public FractalRenderer(double xStart, double xSize, double yStart, double ySize, 
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
				if (cancelled) {
					System.out.println("Cancelled " + xSize);
					break;
				}
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
					
					wr.setPixel(x, y, getColor(i, iterations));
				}
			}
		}
		
	}
	
	private int[] getColor(int n, int maxN) {
		//TODO: HOw does ThIs CoLuR coDE woRk?
		if (n == maxN) return colorTable[0];
		if (maxN < numColours) return colorTable[(int) ((double)(numColours-1) * n / maxN)];
		return colorTable[n % numColours];
	}

	public void setSize(int width, int height, WritableRaster wr) {
		this.width = width;
		this.height = height;
		this.wr = wr;
	}

	public void cancel() {
		cancelled = true;
		try {
			if (executor != null) {
				executor.awaitTermination(10, TimeUnit.SECONDS);
				executor = null;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
