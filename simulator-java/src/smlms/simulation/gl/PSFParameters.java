package smlms.simulation.gl;


//import ij.IJ;

public class PSFParameters {

	public double affineTransformA[][] = new double[][] {{1, 0}, {0, 1}};
	public double affineTransformB[] = new double[] {0, 0};
	
	/* Optical acquisition Parameters (from the user)   */
	/* ------------------------------------------------ */
	/** Stage displacement relative to the working distance. **/
	/** A negative value indicates that the immersion layer thickness is less than the working distance.**/
	public double delta_ti;
		
	/** Immersion medium refractive index (experimental value).*/
	public double ni;
	
	/** Sample refractive index.*/
	public double ns;
	
	/** Emission wavelength of the fluorophoes.*/
	public double lambda; 	
	
	/** Numerical aperture */
	public double NA;		

	/** Magnification of the objective */
	public double M;		

	/** Effective size of a single pixels (physical size divided by the magnification).*/
	public double pixelSize; 
	
	/** Tube length */
	public double zd_star;
	

	/* Working distance of the objective (design value). This is also the width of the immersion layer.*/
	//public double ti0;	
	
	/* Working distance of the objective (experimental value). influenced by the stage displacement.*/
	//public double ti;
	
	/* Effective size of a single stage displacement.*/
	//public double axialResolution;
	
	/* Additional stage offset [nm]*/
	//public double depth;
	
	
	/* Particle Location */
	/* ----------------- */
	/** Axial position of the particle **/
	public double zp;
	
	/** Lateral position of the particle in image domain in [pixels] **/
	public double xp,yp;
	
	
	/* Detector Location */
	/* ----------------- */
	/** Axial distance of the detector relative to the design value <em>in terms of stage displacement</em> [nm]*/
	public double delta_z;
	
	/** Lateral position of the detector in image domain in [pixels] **/
	public double xd,yd;

	
	
	/* Calculated parameters */
	/* --------------------- */
	
	/** Aperture of the objective, projected onto the tube length (Gibson Lanni 1992, page 156) **/
	public double a; 
	
	/** Distance of the defocused plane relative to the back focal plane **/
	public double zd;
	
	/** Wave number **/
	public double k0;
	
	/** Radial distance **/
	public double r;
	

	public double k_a_over_zd;
	public double const2;
	public double ns_ts;
	public double ni_delta_ti;
	public double NA_over_ni_squared;
	public double NA_over_ns_squared;
	public double xpMinusXd,ypMinusYd;

	public int oversamplingLateral = 1;
	public int oversamplingAxial = 1;
	
	public PSFParameters() {
		M = 100;	// Magnification		
		zd_star = 0.2;
		delta_z = 0;
		zd = 0.2;
	}
	
	public PSFParameters(PSFParameters p) {
		this.delta_ti = p.delta_ti;
		this.ni = p.ni;
		this.ns = p.ns;
		this.delta_z = p.delta_z;
		this.lambda = p.lambda;
		this.NA = p.NA;
		this.M = p.M;
		this.pixelSize = p.pixelSize;
		this.zd_star = p.zd_star;
		this.zd = p.zd;
		this.zp = p.zp;
		this.xp = p.xp;
		this.yp = p.yp;
		this.xd = p.xd;
		this.yd = p.yd;
	}
	
	public void calculateConstants() {
		k0 = 2*Math.PI/lambda;
		a = zd_star*NA/Math.sqrt(M*M-NA*NA);	// From equation page 51 Gibson thesis, bottom
		zd = (zd_star*a*a*ni)/(delta_z*zd_star*NA*NA+a*a*ni); // From equation page 70 Gibson thesis, bottom
		k_a_over_zd = k0*a/zd;
		const2 = ((zd_star-zd)*a*a)/(2*zd*zd_star);
		ns_ts = ns*zp;
		ni_delta_ti = ni*delta_ti;
		NA_over_ni_squared = (NA/ni)*(NA/ni);
		NA_over_ns_squared = (NA/ns)*(NA/ns);
		xpMinusXd = xp-xd;
		ypMinusYd = yp-yd;
		r = Math.sqrt(xpMinusXd*xpMinusXd+ypMinusYd*ypMinusYd)*M;
	}
	
	public String toString() {
		String t = new String();
		t = t + "NA=" + NA + ", r=" + r + " zp="+zp;
		return t;
	}
		
}		