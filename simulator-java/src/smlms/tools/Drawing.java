package smlms.tools;

import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.process.ByteProcessor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;

public abstract class Drawing extends ImageCanvas {

	private ImageCanvas		canvasOriginal;
	private Dimension		dim;
	private Image			offscreen;
	private Graphics2D		bufferGraphics;
	protected double 		pixelsize = 100;
	
	abstract public void draw(Graphics2D g);

	public Drawing(int nx, int ny, double pixelsize) {
		super(new ImagePlus("", new ByteProcessor(nx, ny)));
		this.pixelsize = pixelsize;
		imp.show();
		canvasOriginal = imp.getCanvas();
		imp.setWindow(new ImageWindow(imp, this));
		resetBuffer();
	}

	public Drawing(ImagePlus imp, double pixelsize) {
		super(imp);
		this.pixelsize = pixelsize;
		canvasOriginal = imp.getCanvas();
		imp.setWindow(new ImageWindow(imp, this));
		resetBuffer();
	}

	public void resetBuffer() {
		if (bufferGraphics != null) {
			bufferGraphics.dispose();
			bufferGraphics = null;
		}
		if (offscreen != null) {
			offscreen.flush();
			offscreen = null;
		}
		dim = getSize();
		offscreen = createImage(dim.width, dim.height);
		bufferGraphics = (Graphics2D) offscreen.getGraphics();
	}
	
	public void close() {
		if (imp != null)
			if (canvasOriginal != null)
				imp.setWindow(new ImageWindow(imp, canvasOriginal));
	}

	@Override
	public void paint(Graphics g) {
		if (imp == null)
			return;
		if (bufferGraphics == null)
			return;
		
		if (dim.width != getSize().width || dim.height != getSize().height || bufferGraphics == null || offscreen == null)
			resetBuffer();

		super.paint(bufferGraphics);
		
		draw(bufferGraphics);
		
		g.drawImage(offscreen, 0, 0, this);
	}
	
	public void drawCross(double x, double y, double length, Color color, float stroke) {
		double mag = this.getMagnification();
		int rm = (int) Math.round(mag * length / pixelsize);
		int xp = screenXD(x/pixelsize);
		int yp = screenYD(y/pixelsize);
		bufferGraphics.setColor(color);
		bufferGraphics.setStroke(new BasicStroke(stroke));
		bufferGraphics.drawLine(xp - rm, yp, xp + rm, yp);
		bufferGraphics.drawLine(xp, yp - rm, xp, yp + rm);
	}

	public void drawCross(ArrayList<Point3D> points, double length, Color color, float stroke) {
		for(Point3D point : points) 
			drawCross(point.x, point.y, length, color, stroke);
	}

	public void drawPolyline(ArrayList<Point3D> points, Color color, float stroke) {
		bufferGraphics.setColor(color);
		bufferGraphics.setStroke(new BasicStroke(stroke));
		for(int i=0; i<points.size()-1; i++) {
			Point3D p1 = points.get(i);
			Point3D p2 = points.get(i+1);
			int xp1 = screenXD(p1.x/pixelsize);
			int yp1 = screenYD(p1.y/pixelsize);
			int xp2 = screenXD(p2.x/pixelsize);
			int yp2 = screenYD(p2.y/pixelsize);
			bufferGraphics.drawLine(xp1, yp1, xp2, yp2);
		}
	}

	public void print(int x, int y, String message, Color color, int fontsize) {
		double distWhite = Math.abs(255 - color.getGreen()) + Math.abs(255 - color.getRed()) + Math.abs(255 - color.getBlue());
		double distBlack = color.getGreen() + color.getRed() + color.getBlue();
		bufferGraphics.setFont(new Font("Monospace", fontsize, Font.PLAIN));
		bufferGraphics.setColor(distWhite < distBlack ? Color.WHITE : Color.BLACK);
		bufferGraphics.drawString(message, x + 1, y + 1);
		bufferGraphics.setColor(color);
		bufferGraphics.drawString(message, x, y);
	}

}
