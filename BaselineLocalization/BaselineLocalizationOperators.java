//=========================================================================================
//
// Single-Molecule Localization Microscopy Challenge 2016
// http://bigwww.epfl.ch/smlm/
//
// Author: 
// Daniel Sage, http://bigwww.epfl.ch/sage/
// Biomedical Imaging Group (BIG)
// Ecole Polytechnique Federale de Lausanne (EPFL), CH-1015 Lausanne, Switzerland
//
// Reference: 
// D. Sage, H. Kirshner, T. Pengo, N. Stuurman, J. Min, S. Manley, M. Unser
// Quantitative Evaluation of Software Packages for Single-Molecule Localization Microscopy 
// Nature Methods 12, August 2015.
// 
// Conditions of use: 
// You'll be free to use this software for research purposes, but you 
// should not redistribute it without our consent. In addition, we expect you to include a
// citation or acknowledgment whenever you present or publish results that are based on it.
//
//=========================================================================================

import ij.IJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;

import java.util.ArrayList;

public class BaselineLocalizationOperators {

	private float radiusPix;
	private double minSignal;
	private boolean display;
	private double minSNR;
	private double pixelsize;
	private boolean verbose;
	
	public  BaselineLocalizationOperators(float radiusPix, double minSignal, double minSNR, double pixelsize, boolean verbose, boolean display) {
		this.radiusPix = radiusPix;
		this.minSignal = minSignal;
		this.minSNR = minSNR;
		this.pixelsize = pixelsize;
		this.verbose = verbose;
		this.display = display;
	}
	
	public FloatProcessor filterDoG(FloatProcessor source, int frame) {
		int nx = source.getWidth();
		int ny = source.getHeight();
		float signal[] = (float[]) source.getPixels();
		int n = signal.length;
		float signal1[] = new float[n];
		float signal2[] = new float[n];
		float dog[] = new float[n];
		System.arraycopy(signal, 0, signal1, 0, n);
		System.arraycopy(signal, 0, signal2, 0, n);
		new FastGaussianSmoothing(signal1, radiusPix * 2f, nx, ny).run();
		new FastGaussianSmoothing(signal2, radiusPix * 0.5f, nx, ny).run();
		for (int k = 0; k < signal1.length; k++)
			dog[k] = (signal2[k] > signal1[k] ? signal2[k] - signal1[k] : 0f);
		FloatProcessor fp = new FloatProcessor(nx, ny, dog, null);
		if (display && frame == 1)
			new ImagePlus("Prefilter frame 1", fp).show();
		return fp;
	}

	public ArrayList<int[]> detect(FloatProcessor source, FloatProcessor prefiltered, boolean calibration) {
		int nx = prefiltered.getWidth();
		int ny = prefiltered.getHeight();
		int h = (int) Math.ceil(radiusPix/2);
		ArrayList<int[]> lmall = new ArrayList<int[]>();
		double max = -Double.MAX_VALUE;
		int imax = nx/2;
		int jmax = ny/2;
		// Find the local max
		for (int i = h; i < nx - h; i++) {
			for (int j = h; j < ny - h; j++) {
				if (source.getf(i, j) > minSignal) {
					float v = prefiltered.getf(i, j);
					if (v > prefiltered.getf(i - 1, j))
					if (v > prefiltered.getf(i + 1, j)) 
					if (v > prefiltered.getf(i - 1, j - 1))
					if (v > prefiltered.getf(i, j - 1))
					if (v > prefiltered.getf(i + 1, j - 1))
					if (v > prefiltered.getf(i - 1, j + 1)) 
					if (v > prefiltered.getf(i, j + 1))
					if (v > prefiltered.getf(i + 1, j + 1)) {
						if (v > max ) {
							imax = i;
							jmax = j;
							max = v;
						}
						lmall.add(new int[] { i, j });
					}
				}
			}
		}
		// Only 1 local max for calibration, all otherwise
		ArrayList<int[]> lmcal = new ArrayList<int[]>();
		lmcal.add(new int[] {imax, jmax});
		return (calibration ? lmcal : lmall);
	}

	protected ArrayList<BaselineLocalizationParticle> localize(ArrayList<int[]> candidates, FloatProcessor source, int frame) {
		ArrayList<BaselineLocalizationParticle> spots = new ArrayList<BaselineLocalizationParticle>();
		for (int[] candidate : candidates) {
			double snr = computeSNR(candidate, source, radiusPix);
			if (snr > minSNR) {
				double[] mxy = computeMoments(candidate, source, radiusPix);
				double signal = source.getPixelValue(candidate[0], candidate[1]);
				double x = mxy[0] * pixelsize;
				double y = mxy[1] * pixelsize;
				String pos = " (" + IJ.d2s(x, 5) + " " + IJ.d2s(y, 5) + ")";

				if (verbose) 
					IJ.log(" " + pos + " signal:" + IJ.d2s(signal, 5) + " snr:" + IJ.d2s(snr,5) + " sigma:" + IJ.d2s(mxy[2], 5) + " " + IJ.d2s(mxy[3], 5));
				double mxynm[] = new double[] {mxy[0] * pixelsize, mxy[1] * pixelsize, mxy[2], mxy[3]};
				BaselineLocalizationParticle particle = new BaselineLocalizationParticle(frame, mxynm, signal, snr);
				spots.add(particle);
			}
		}
		return spots;
	}

	protected double[] computeMoments(int candidate[], FloatProcessor fp, float fwhmPixel) {
		int h = (int) Math.ceil(fwhmPixel);
		int i = candidate[0];
		int j = candidate[1];
		float v = 0.0f;
		double sum = 0.0;
		double x1 = 0.0;
		double y1 = 0.0;
		for (int x = i - h; x <= i + h; x++)
			for (int y = j - h; y <= j + h; y++) {
				v = fp.getPixelValue(x, y);
				x1 += x * v;
				y1 += y * v;
				sum += v;
			}
		if (sum > 0) {
			x1 /= sum;
			y1 /= sum;
		}
		double x2 = 0.0;
		double y2 = 0.0;
		for (int x = i - h; x <= i + h; x++)
			for (int y = j - h; y <= j + h; y++) {
				v = fp.getPixelValue(x, y);
				x2 += (x - x1) * (x - x1) * v;
				y2 += (y - y1) * (y - y1) * v;
			}
		if (sum > 0) {
			x2 = Math.sqrt(x2 / sum);
			y2 = Math.sqrt(y2 / sum);
		}

		return new double[] { x1 + 0.5, y1 + 0.5, x2, y2 };
	}

	protected double computeSNR(int[] candidate, FloatProcessor source, float fwhmPixel) {
		int h = (int) Math.ceil(fwhmPixel);
		int nx = source.getWidth();
		int ny = source.getHeight();
		int i = candidate[0];
		int j = candidate[1];
		if (i - h < 0) return -Double.MAX_VALUE;
		if (j - h < 0) return -Double.MAX_VALUE;
		if (j + h > nx - 1) return -Double.MAX_VALUE;
		if (j + h > ny - 1) return -Double.MAX_VALUE;

		float[] pix = (float[]) source.getPixels();

		// Signal
		double meanSignal = 0.0;
		double signal = -Double.MAX_VALUE;
		int countSignal = 0;
		for (int x = i - h; x <= i + h; x++)
			for (int y = j - h; y <= j + h; y++) {
				meanSignal += pix[x + j * nx];
				countSignal++;
				if (pix[x + j * nx] > signal) signal = pix[x + j * nx];
			}
		meanSignal = meanSignal / countSignal;

		double noiseSignal = 0;
		for (int x = i - h; x <= i + h; x++)
			for (int y = j - h; y <= j + h; y++) {
				noiseSignal += (pix[x + j * nx] - meanSignal) * (pix[x + j * nx] - meanSignal);
			}
		noiseSignal = Math.sqrt(noiseSignal / countSignal);

		// background
		double meanBackground = 0.0;
		int countBackground = 0;
		int n = 2 * h + 1;
		for (int x = i - n; x < i - h; x++)
			for (int y = j - h; y <= j + h; y++) {
				countBackground++;
				meanBackground += pix[x + j * nx];
			}
		for (int x = i + h + 1; x <= i + n; x++)
			for (int y = j - h; y <= j + h; y++) {
				countBackground++;
				meanBackground += pix[x + j * nx];
			}
		for (int x = i - h; x <= i + h; x++)
			for (int y = j - n; y < j - h; y++) {
				countBackground++;
				meanBackground += pix[x + j * nx];
			}
		for (int x = i - h; x <= i + h; x++)
			for (int y = j + h + 1; y <= j + n; y++) {
				countBackground++;
				meanBackground += pix[x + j * nx];
			}
		meanBackground /= countBackground;

		double noiseBackground = 0;
		for (int x = i - n; x < i - h; x++)
			for (int y = j - h; y <= j + h; y++) {
				noiseBackground += (pix[x + j * nx] - meanBackground) * (pix[x + j * nx] - meanBackground);
			}
		for (int x = i + h + 1; x <= i + n; x++)
			for (int y = j - h; y <= j + h; y++) {
				noiseBackground += (pix[x + j * nx] - meanBackground) * (pix[x + j * nx] - meanBackground);
			}
		for (int x = i - h; x <= i + h; x++)
			for (int y = j - n; y < j - h; y++) {
				noiseBackground += (pix[x + j * nx] - meanBackground) * (pix[x + j * nx] - meanBackground);
			}
		for (int x = i - h; x <= i + h; x++)
			for (int y = j + h + 1; y <= j + n; y++) {
				noiseBackground += (pix[x + j * nx] - meanBackground) * (pix[x + j * nx] - meanBackground);
			}
		noiseBackground = Math.sqrt(noiseBackground / countBackground);
		double snr = (signal - meanBackground) / noiseBackground;
		return snr;
	}

	/**
	 * Gaussian filter class. Implementation of the Gaussian filter as a cascade
	 * of 3 exponential filters. The boundary conditions are mirroring. Threaded
	 * or directly by calling the run()
	 */
	protected class FastGaussianSmoothing implements Runnable {

		private float	signal[];
		private float	sigma;
		private int		nx;
		private int		ny;
		public FastGaussianSmoothing(float signal[], float sigma, int nx, int ny) {
			this.signal = signal;
			this.sigma = sigma;
			this.nx = nx;
			this.ny = ny;
		}

		public void run() {
			if (nx > 1 & sigma > 0) {
				float row[] = new float[nx];
				float s2 = sigma * sigma;
				float pole = 1.0f + (3.0f / s2) - (float) (Math.sqrt(9.0 + 6.0 * s2) / s2);
				for (int y = 0; y < ny * nx; y += nx) {
					System.arraycopy(signal, y, row, 0, nx);
					row = convolveIIR_TriplePole(row, pole);
					System.arraycopy(row, 0, signal, y, nx);
				}
			}

			if (ny > 1 & sigma > 0) {
				float liney[] = new float[ny];
				float s2 = sigma * sigma;
				float pole = 1.0f + (3.0f / s2) - (float) (Math.sqrt(9.0 + 6.0 * s2) / s2);
				for (int x = 0; x < nx; x++) {
					for (int y = 0; y < ny; y++)
						liney[y] = signal[y * nx + x];
					liney = convolveIIR_TriplePole(liney, pole);
					for (int y = 0; y < ny; y++)
						signal[y * nx + x] = liney[y];
				}
			}
		}

		private float[] convolveIIR_TriplePole(float[] signal, float pole) {
			int l = signal.length;

			float lambda = 1.0f;
			float[] output = new float[l];
			for (int k = 0; k < 3; k++) {
				lambda = lambda * (1.0f - pole) * (1.0f - 1.0f / pole);
			}
			for (int n = 0; n < l; n++) {
				output[n] = signal[n] * lambda;
			}
			for (int k = 0; k < 3; k++) {
				output[0] = getInitialCausalCoefficientMirror(output, pole);
				for (int n = 1; n < l; n++) {
					output[n] = output[n] + pole * output[n - 1];
				}
				output[l - 1] = getInitialAntiCausalCoefficientMirror(output, pole);
				for (int n = l - 2; 0 <= n; n--) {
					output[n] = pole * (output[n + 1] - output[n]);
				}
			}
			return output;
		}

		private float getInitialAntiCausalCoefficientMirror(float[] c, float z) {
			return ((z * c[c.length - 2] + c[c.length - 1]) * z / (z * z - 1.0f));
		}

		private float getInitialCausalCoefficientMirror(float[] c, float z) {
			float tolerance = 10e-6f;
			float z1 = z;
			float zn = (float) Math.pow(z, c.length - 1);
			float sum = c[0] + zn * c[c.length - 1];
			int horizon = c.length;

			if (tolerance > 0.0f) {
				horizon = 2 + (int) (Math.log(tolerance) / Math.log(Math.abs(z)));
				horizon = (horizon < c.length) ? (horizon) : (c.length);
			}
			zn = zn * zn;
			for (int n = 1; n < horizon - 1; n++) {
				zn = zn / z;
				sum = sum + (z1 + zn) * c[n];
				z1 = z1 * z;
			}
			return (sum / (1.0f - (float) Math.pow(z, 2 * c.length - 2)));
		}
	}

}
