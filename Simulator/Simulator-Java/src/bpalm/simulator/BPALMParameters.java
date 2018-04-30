package bpalm.simulator;
import bpalm.model.GibsonLanniParameters;

/**
 * This class manages all parameters of the acquisition, output and sample tabs of the BiplaneDataGenerator plugin.
 * 
 * @author Hagai Kirshner, Biomedical Imaging Group, Ecole Polytechnique Federale de Lausanne (EPFL)
 * @author Thomas Pengo, Laboratory for Experimental Biophysics, Ecole Polytechnique Federale de Lausanne (EPFL)
 */

public class BPALMParameters extends GibsonLanniParameters {

	public static enum StructureType {
		UNIFORM ("Uniform"),
		SINGLE ("One particle centered, at chosen max depth"),
		SPIRAL_10_REVOLUTIONS ("Spiral 10 revolutions"),
		SPIRAL_1_REVOLUTION ("Spiral 1 revolution");
		
		String str;
		private StructureType(String s) {str=s;};
		public String toString() {return str;}
	}
	
	// Output parameters
	public double maxValue;
	public int bits, accuracy;
	public String intensityScale, lut;
	
	
	// Split parameters
	public boolean doSplit;
	public String orientation;
	public double rotation;
	public double scale;
	public double dx;
	public double dy;
	public double border;
	
	public double thick;
	// Sequence
	public boolean doSequence;
	public int noFrames;
	public boolean saveParticles;

	public double delta_z2;
	
	public BPALMParameters() {}
	
	public BPALMParameters(BPALMParameters p) {
		super(p);		
		this.thick 		= p.thick;
		this.doSplit 	= p.doSplit;
		this.rotation 	= p.rotation;
		this.scale 		= p.scale;
		this.dx 		= p.dx;
		this.dy 		= p.dy;
		this.border 	= p.border;
		this.delta_z2 		= p.delta_z2;
		this.doSequence 	= true;
		this.orientation 	= p.orientation;
		this.noFrames 		= 1;
		this.maxValue 		= 1.0; 		
		this.bits 			= 32;		
		this.intensityScale = "Linear";
		this.lut 			= "Fire";
		this.accuracy 		= 0;	// Good, fastest
		this.saveParticles 	= false;
	}

	public void calculateConstants() {
		super.calculateConstants();
	}
}