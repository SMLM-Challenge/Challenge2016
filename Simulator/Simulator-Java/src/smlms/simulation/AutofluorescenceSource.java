package smlms.simulation;

import imageware.ImageWare;
import smlms.tools.PsRandom;

public class AutofluorescenceSource {

	private double xo;
	private double yo;
	private double scale;
	private double size;
	private double xdiffusion[] = new double[10];
	private double ydiffusion[] = new double[10];
	private PsRandom psrand;
	private Viewport viewport;

	public AutofluorescenceSource(PsRandom psrand, Viewport viewport, double scale, double size, double diffusion) {
		this.xo = psrand.nextDouble() * viewport.getFoVXNano();
		this.yo = psrand.nextDouble() * viewport.getFoVYNano();
		this.scale = scale;
		this.size = size;
		this.psrand = psrand;
		this.viewport = viewport;
		xdiffusion[0] = 0;
		ydiffusion[0] = 0;
		double dir = (psrand.nextDouble()-0.5)*Math.PI*4.0;
		for(int d=1; d<10; d++) {
			xdiffusion[d] = xdiffusion[d-1] + diffusion * Math.sin(dir);
			ydiffusion[d] = ydiffusion[d-1] + diffusion * Math.cos(dir);
			if (d==5)
				dir = (psrand.nextDouble()-0.5)*Math.PI*4.0;
		}
	}

	public AutofluorescenceSource(PsRandom psrand, Viewport viewport, double xo, double yo, double scale, double size, double diffusion) {
		this.xo = xo;
		this.yo = yo;
		this.scale = scale;
		this.size = size;
		this.psrand = psrand;
		this.viewport = viewport;
		xdiffusion[0] = 0;
		ydiffusion[0] = 0;
		double dir = (psrand.nextDouble()-0.5)*Math.PI*4.0;
		for(int d=1; d<10; d++) {
			xdiffusion[d] = xdiffusion[d-1] + diffusion * Math.sin(dir);
			ydiffusion[d] = ydiffusion[d-1] + diffusion * Math.cos(dir);
			if (d==5)
				dir = (psrand.nextDouble()-0.5)*Math.PI*4.0;
		}
	}

	public void move(double displacement) {
		double dir = (psrand.nextDouble()-0.5)*Math.PI*4.0;
		xo += displacement * Math.sin(dir);
		yo += displacement * Math.cos(dir);		
	}

	public void draw(ImageWare image) {
		int sizePixel = (int)Math.ceil(viewport.convertPixel(size));
		for(int d=0; d<10; d++) {
			int x = (int)viewport.screenX(xo + xdiffusion[d]);
			int y = (int)viewport.screenY(yo + ydiffusion[d]);
			double value = 1.8*Math.PI*scale*scale;
			for(int u=-sizePixel; u<=sizePixel; u++)
			for(int v=-sizePixel; v<=sizePixel; v++) {
				double r = u*u + v*v;
				if (r < sizePixel*sizePixel)
					image.putPixel(x+u,  y+v,  0, Math.max(image.getPixel(x+u, y+v, 0), value));
			}
		}
	}
}
