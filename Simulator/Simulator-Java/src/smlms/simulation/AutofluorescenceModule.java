package smlms.simulation;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import imageware.Builder;
import imageware.ImageWare;

import java.util.Vector;

import org.apache.commons.math3.distribution.PoissonDistribution;

import smlms.file.Fluorophores;
import smlms.tools.ArrayOperations;
import smlms.tools.Chrono;
import smlms.tools.PsRandom;
import smlms.tools.Tools;

public class AutofluorescenceModule {
	
	final static public int NONE = 0;
	final static public int STATIC = 1;
	final static public int DYNAMIC = 2;

	final static public String[] evolutions = new String[] {"None", "Static", "Dynamic"};

	final static public int STRUCTURE = 1;
	final static public int RANDOM = 2;
	
	final static public String[] names = new String[] {"None", "Structure", "Random"};

	private int evolution = NONE;
	private float[][] background;
	private double backGain;
	public double backPoisson;
	
	private int nbSources;
	private int nbScale;
	private double diffusion;
	private double displacement;
	private double size;
	private int type;
	private double defocus;
	private float gain;
	private double change;
	
	private float offset;
	
	private Viewport viewport;
	private PsRandom psrand;
	private Vector<AutofluorescenceSource> list[] = null;
	
	public AutofluorescenceModule(PsRandom psrand, Viewport viewport, int evolution) {
		this.psrand = psrand;
		this.viewport = viewport;
		this.evolution = evolution;
	}
	
	public void setBackground(double backGain, double backPoisson) {
		this.backGain = backGain;
		this.backPoisson = backPoisson;		
	}
	
	public void setSources(int type, int nbSources, int nbScale, double defocus, double diffusion, double displacement, double size, double change, double gain) {
		this.type = type;
		this.nbSources = nbSources;
		this.nbScale = nbScale;		
		this.defocus = defocus;
		this.diffusion = diffusion;
		this.displacement = displacement;
		this.size = size;		
		this.gain = (float)gain;
		this.change = change;		
	}
	
	public void test(int nbFrames, Fluorophores fluos) {
		int nx = viewport.getFoVXPixel();
		int ny = viewport.getFoVYPixel();
		ImageStack stack = new ImageStack(nx, ny);
		for(int frame=0; frame<nbFrames; frame++) {
			Chrono.reset();
			float[][] image = new float[nx][ny];
			create(nx, ny, fluos, 1);
			add(image);
			FloatProcessor fp = new FloatProcessor(image);
			stack.addSlice("", fp);
			Chrono.print("add " + frame);
		}
		ImagePlus imp = new ImagePlus("Test Background", stack);
		imp.show();	
	}
	
	public void add(float image[][]) {
		if (image == null)
			return;
		if (background == null)
			return;
		ArrayOperations.add(image, background);
	}
	
	public void create(int nx, int ny, Fluorophores fluos, double correctionSampling) {
		if (evolution == NONE)
			return;
		
		viewport.setThicknessNano(Double.MAX_VALUE);

		background = new float[nx][ny];
		
		for (int i=0; i<background.length; i++)
		for (int j=0; j<background[0].length; j++)
			background[i][j] = (float)(backGain* (new PoissonDistribution(backPoisson*correctionSampling).sample()));
		/*
		if (list == null) {
			list = create(viewport, fluos);
			place(background, list);
			//offset = (float)psrand.nextGaussian(offsetMean, offsetStdv);
			
			if (type != NONE)
				ArrayOperations.normalize(background, gain, offset);
			else
				ArrayOperations.increment(background, offset);
				
 		}
		
		if (evolution == DYNAMIC) {
			move(list, change);
			place(background, list);
			if (type != NONE)
				ArrayOperations.normalize(background, gain, offset);
			else
				ArrayOperations.increment(background, offset);
		}
		*/
	}

	private void move(Vector<AutofluorescenceSource>[] list, double percentageOfChange) {
		int nbScale = list.length;
		Vector<AutofluorescenceSource> flatlist = new Vector<AutofluorescenceSource>();
		for(int k=0; k<nbScale; k++) {
			int ns = list[k].size();
			for(int i=0; i<ns; i++) {
				flatlist.add(list[k].get(i));
			}
		}
		int n = flatlist.size();
		int nchange = Tools.round(n * percentageOfChange / 100.0);
		for(int k=0; k<nchange; k++) {
			int index = (int)(psrand.nextDouble()*n);
			flatlist.get(index).move(displacement);
		}
	}
	
	private void place(float[][] background, Vector<AutofluorescenceSource>[] list) {
		int n = background.length;
		int m = background[0].length;
		int nbScale = list.length;
		ImageWare sum = Builder.create(n, m, 1, ImageWare.FLOAT);
		for(int k=0; k<nbScale; k++) {
			double sigma = (k+1)*viewport.convertIntegerPixel(defocus)/nbScale;
			int ns = list[k].size();
			if (ns > 0) {
				ImageWare im = Builder.create(n, m, 1, ImageWare.FLOAT);
				for(int i=0; i<ns; i++) {
					list[k].get(i).draw(im);
				}
				im.smoothGaussian(sigma);
				sum.add(im);
			}
		}
		sum.getBlockXY(0, 0, 0, background, ImageWare.MIRROR);
	}

	private Vector<AutofluorescenceSource>[] create(Viewport viewport, Fluorophores fluorophoresAll) {
		int n = viewport.getFoVXPixel();
		int m = viewport.getFoVYPixel();
	
		ImageWare sum = Builder.create(n, m, 1, ImageWare.FLOAT);
		double nbSourceMean = nbSources / (double)nbScale;
		int nsources[] = new int[nbScale];
		
		for(int k=0; k<nbScale; k++) {
			nsources[k] =  (int)(0.7*nbSourceMean + psrand.nextGaussian(nbSourceMean*0.3, 1));
		}
		
		Vector<AutofluorescenceSource>[] list = new Vector[nbScale];
		
		for(int k=0; k<nbScale; k++) {
			double sigma = (k+1)*viewport.convertIntegerPixel(defocus)/nbScale;
			ImageWare im = Builder.create(n, m, 1, ImageWare.FLOAT);
			list[k] = new Vector<AutofluorescenceSource>();
			if (type == STRUCTURE) {
				for(int i=0; i<nsources[k]; i++) {
					int nf = fluorophoresAll.size();
					double min = Double.MAX_VALUE;
					double xo = 0;
					double yo = 0;
					for(int a=0; a<10; a++) {
						int index = Math.max(0, Math.min(nf-1, (int)(psrand.nextDouble() * nf)));
						double xr = fluorophoresAll.get(index).x;
						double yr = fluorophoresAll.get(index).y;
						double v = sum.getPixel((int)viewport.screenX(xr), (int)viewport.screenY(yr), 0);
						if (v < min) {
							min = v;
							xo = xr;
							yo = yr;
						}
					}
					AutofluorescenceSource source = new AutofluorescenceSource(psrand, viewport, xo, yo, sigma, size, diffusion);
					list[k].add(source);
					source.draw(im);
				}
			}
			if (type == RANDOM) {
				for(int i=0; i<nsources[k]; i++) {
					AutofluorescenceSource source = new AutofluorescenceSource(psrand, viewport, sigma, size, diffusion);
					list[k].add(source);
					source.draw(im);
				}
			}
			im.smoothGaussian(sigma);
			sum.add(im);
		}
		return list;
	}

}
