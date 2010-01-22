package fractals;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;


public class Main {
	
	//Things to improve:
	// 1. Progress meter
	// 2. Allow adjustable num iterations
	// 3. Allow adjustable colours
	// 4. Draw low res preview
	// 5. Make canvas draggable

	static public void main(String[] args) {
		
		final JFrame f = new JFrame("Mandelbrot");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		int width = 600;
		int height = 500;
		
		final Fractal fractal = new Mandelbrot(width, height);
		final Canvas canvas = new FractalCanvas(fractal);


		f.getContentPane().setPreferredSize(new Dimension(width, height));
		
		f.getContentPane().add(canvas);
		f.pack();
		f.setLocationRelativeTo(null);
		
		final FractalRenderer executor = new FractalRenderer(canvas, fractal, 600, 500);
		
		canvas.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					executor.zoomIn(e.getX(), e.getY());
				}else if (e.getButton() == MouseEvent.BUTTON3){
					executor.zoomOut(e.getX(), e.getY());
					
				}
			}
		});
		
		canvas.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				int width = e.getComponent().getWidth();
				int height = e.getComponent().getHeight();
				
				executor.resize(width, height);
			}
			
		});
		
		f.setVisible(true);

		Thread executorThread = new Thread(executor);
		executorThread.setDaemon(true);
		executorThread.start();

		canvas.repaint();
	}
	
	@SuppressWarnings("serial")
	static class FractalCanvas extends Canvas {
		private Fractal fractal;
		
		public FractalCanvas(Fractal fractal) {
			this.fractal = fractal;
		}
		
		public void paint(Graphics g) {
			g.drawImage(fractal.getBufferedImage(), 0, 0, Color.red, null);
		}
		
		@Override
		public void update(Graphics g) {
			paint(g);
		}
	}
	
	
}
