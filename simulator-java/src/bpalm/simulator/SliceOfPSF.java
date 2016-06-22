package bpalm.simulator;

import ij.IJ;
import bpalm.model.KirchhoffDiffractionSimpson;

// This class generates a slice of single source according to the the Gibson-Lanni PSF model.
//	@author Hagai Kirshner, Biomedical Imaging Group, Ecole Polytechnique Federale de Lausanne (EPFL)
/**
 * 
 * @author Hagai Kirshner, Biomedical Imaging Group, Ecole Polytechnique Federale de Lausanne (EPFL)
 * @author Thomas Pengo, Laboratory of Experimental Biophysics, Ecole Polytechnique Federale de Lausanne (EPFL)
 *
 */
public class SliceOfPSF implements Runnable{
	    
		static int	OVER_SAMPLING = 1; //5
		float[][] zStack;
		private BPALMParameters p;
		int z;	// slice index
		Particle cur;
		double radius;	// the range of 'r' values for which we calculate the psf values.
		int nx, ny;
		double progress = 0;

		// Constructor.
		public SliceOfPSF(float[][] zStack, BPALMParameters p, int z, Particle cur, double radius) {
			this.zStack = zStack;
			nx = zStack.length;
			ny = zStack[0].length;
			this.z = z;
			this.cur = cur;
			this.radius = radius;
			// making a new copy of the parameters is required, as different threads use different parameters.
			this.p = new BPALMParameters(p);
		}
		
		public void run() {
			// Initializing the Gibson-Lanni psf model
			p.ts = cur.z;
			p.ti = p.ti0 - z*p.axialResolution - p.depth;	// The smaller the immersion layer is, the farther is the focal plane of the objective

			//IJ.log("zp="+cur.z + " [nm]");
			p.calculateConstants();
			KirchhoffDiffractionSimpson I = new KirchhoffDiffractionSimpson(p, p.accuracy);		
					
			// PSF values at radial locations
			// We consider a larger radius than the radius parameter because we evaluate on a rectangle 
			int nr = (int)Math.round(Math.sqrt(2.0)*(radius+1)*OVER_SAMPLING);
			IJ.log(">> " + IJ.d2s(p.ti, 10)  + " z " + z + " " + p.depth + " p.ts= " + cur.z + " nr " + nr);
			double[] r = new double[nr];
			double[] h = new double[nr];
			for (int nn=0; nn<nr; nn++) {
				r[nn] = ((double)nn)/((double)OVER_SAMPLING);
				h[nn] = I.calculate(r[nn]*p.pixelSize*p.M);
				progress = progress + 1.0/(r.length+1);
			}
					
			// Linear interpolation of the pixels values
			// Interpolation is carried out in the neighborhood of (x,y) particle position
			double xp = cur.x;
			double yp = cur.y;
			double rPixel, value;
			int index;
			
			int xLow = Math.max(0,(int)Math.ceil(cur.x-radius)); 
			int xHigh = Math.min(nx-1,(int)Math.floor(cur.x+radius));
			int yLow = Math.max(0,(int)Math.ceil(cur.y-radius)); 
			int yHigh = Math.min(ny-1,(int)Math.floor(cur.y+radius));
			for (int x=xLow; x<=xHigh; x++)
				for (int y=yLow; y<=yHigh; y++) {
					rPixel = Math.sqrt((x-xp)*(x-xp)+(y-yp)*(y-yp));								// radius of the current pixel in units of [pixels]
					index = (int)(rPixel*OVER_SAMPLING);									// Index of nearest coordinate from bellow
					value = h[index] + (h[index+1]-h[index])*(rPixel-r[index])*OVER_SAMPLING ;		// Interpolated value.	
					zStack[x][y] += value;
				}
			progress = progress + 1.0/(r.length+1); // progress = 1
		}		
		
		public double getProgress() { return progress; }
}
