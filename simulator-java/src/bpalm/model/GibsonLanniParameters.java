package bpalm.model;

import ij.IJ;

import java.io.Serializable;

/**
 * This class manages all parameters of the Gibson-Lanni model.
 * 
 * @author Hagai Kirshner, Biomedical Imaging Group, Ecole Polytechnique Federale de Lausanne (EPFL)
 */

public class GibsonLanniParameters implements Serializable {

	/** Working distance of the objective (design value). This is also the width of the immersion layer.*/
	public double ti0;	
	
	/** Working distance of the objective (experimental value). influenced by the stage displacement.*/
	public double ti;		
	
	/** Immersion medium refractive index (experimental value).*/
	public double ni;
	
	/** Sample refractive index.*/
	public double ns;
	
	/** Distance between imaging planes <em>in terms of stage displacement</em> [nm]*/
	public double delta_z;

	/** Additional stage offset [nm]*/
	public double depth;

	/** Emission wavelength of the fluorophoes.*/
	public double lambda; 	
	
	/** Numerical aperture (normalized by ni0) */
	public double NA;		

	/** Magnification of the objective */
	public double M;		

	/** Effective size of a single pixels (physical size divided by the magnification).*/
	public double pixelSize; 

	/** Effective size of a single stage displacement.*/
	public double axialResolution; 
	
	/** Aperture of the objective, projected onto the tube length (Gibson Lanni 1992, page 156) */
	public double a; 
	
	/** Tube length */
	public double zd_star;
	
	/** Distance of the defocused plane relative to the back focal plane */
	public double zd;
	
	/** Axial position of the particle */
	public double ts;
	
	/** Constants for fast computations */
	public double k0;			// wave number
	public double k_a_over_zd;
	public double const2;
	public double ns_ts;
	public double NA_over_ni_squared;
	public double NA_over_ns_squared;
	//public double ni_over_ns_squared;
	
	public GibsonLanniParameters() {}
	public GibsonLanniParameters(GibsonLanniParameters p) {
		this.ti0 = p.ti0;
		this.ti = p.ti;
		this.ni = p.ni;
		this.ns = p.ns;
		this.delta_z = p.delta_z;
		this.depth = p.depth;
		this.lambda = p.lambda;
		this.NA = p.NA;
		this.M = p.M;
		this.pixelSize = p.pixelSize;
		this.axialResolution = p.axialResolution;
		this.a = p.a;
		this.zd_star = p.zd_star;
		this.zd = p.zd;
		this.ts = p.ts;
		
		calculateConstants();
	}
	
	public void calculateConstants() {
		k0 = 2*Math.PI/lambda;
		
		a = zd_star*NA/Math.sqrt(M*M-NA*NA);	// From equation page 51 Gibson thesis, bottom
		zd = (zd_star*a*a*ni)/(delta_z*zd_star*NA*NA+a*a*ni); // From equation page 70 Gibson thesis, bottom
		k_a_over_zd = k0*a/zd;
		const2 = ((zd_star-zd)*a*a)/(2*zd*zd_star);
		ns_ts = ns*ts;
		NA_over_ni_squared = (NA/ni)*(NA/ni);
		NA_over_ns_squared = (NA/ns)*(NA/ns);
	
		IJ.log(" GLP ns_ts " + ns_ts);
		
	}		
}