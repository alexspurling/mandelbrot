
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.SwingWorker;


public class Main {
	
	//Things to improve:
	// 1. Progress meter
	// 2. Allow adjustable num iterations
	// 3. Allow adjustable colours
	// 4. Draw low res preview
	// 5. Make canvas draggable

	static int X = 600, Y = 500;
	static BufferedImage I = new BufferedImage(X, Y, BufferedImage.TYPE_INT_RGB);
	
	private static FractalExecutor executor;
	
	//static double xStart = -0.743643887037158704752191506114774 - 3 / 2;
	//static double yStart = 0.131825904205311970493132056385139 - 2 / 2;

	private static long magnification = 1;
	private static int iterations = 100;
//	private static double xStart = 0;
//	private static double yStart = 0;
	
	@SuppressWarnings("serial")
	static class MainCanvas extends Canvas {
		public void paint(Graphics g) {
			g.drawImage(Main.I, 0, 0, Color.red, null);
		}
	}

	static public void main(String[] args) {
		
		final JFrame f = new JFrame("Mandelbrot");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//f.setUndecorated(true); 
		
		//JPanel panel = new JPanel();
		//panel.setPreferredSize(new Dimension(X, Y));
		//f.setContentPane(panel);
		
		final Mandelbrot fractal = new Mandelbrot(I.getRaster(), X, Y);
		final Canvas canvas = new MainCanvas();

		f.getContentPane().setPreferredSize(new Dimension(X, Y));
		
		f.getContentPane().add(canvas);
		f.pack();
		f.setLocationRelativeTo(null);
		//f.setExtendedState(Frame.MAXIMIZED_BOTH);  
		
		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					magnification *= 2;
					iterations += 75;
					if (executor != null) {
						executor.cancel();
					}
					executor = new FractalExecutor(canvas, fractal, e.getX(), e.getY());
					executor.execute();
					//fractal.drawFractal(e.getX(), e.getY(), magnification, iterations);
					System.out.println("Drawn fractal at mag=" + magnification + ", iter=" + iterations);
					
					
				}else if (e.getButton() == MouseEvent.BUTTON3 && magnification > 1){
					magnification /= 2;
					iterations -= 75;

					if (executor != null) {
						executor.cancel();
					}
					executor = new FractalExecutor(canvas, fractal, e.getX(), e.getY());
					executor.execute();
					System.out.println("Drawn fractal at mag=" + magnification + ", iter=" + iterations);
					
				}
			}
		});
		
		canvas.addComponentListener(new ComponentListener() {

			@Override
			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void componentResized(ComponentEvent e) {
				System.out.println(e);
				X = e.getComponent().getWidth();
				Y = e.getComponent().getHeight();
				
				if (executor != null) {
					executor.cancel();
				}
				
				I = new BufferedImage(X, Y, BufferedImage.TYPE_INT_RGB);
				fractal.setSize(X, Y, I.getRaster());
				
				executor = new FractalExecutor(canvas, fractal, X/2, Y/2);
				executor.execute();
			}

			@Override
			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		f.setVisible(true);
		

		fractal.drawFractal(X/2.0, Y/2.0, magnification, iterations);

		canvas.repaint();
	}
	
	private static class FractalExecutor extends SwingWorker<Void, Void> {

		private int x;
		private int y;
		private Mandelbrot fractal;
		private Canvas canvas;
		
		public FractalExecutor(Canvas canvas, Mandelbrot fractal, int x, int y) {
			this.canvas = canvas;
			this.fractal = fractal;
			this.x = x;
			this.y = y;
		}
		
		@Override
		protected Void doInBackground() throws Exception {
			if (fractal.drawFractal(x, y, magnification, iterations)) {
				canvas.repaint();
			}
			return null;
		}
		
		public void cancel() {
			fractal.cancel();
		}
		
	}
	
	
}
