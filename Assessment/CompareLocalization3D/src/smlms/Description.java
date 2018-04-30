// =========================================================================================
//
// Single-Molecule Localization Microscopy Challenge 2016
// http://bigwww.epfl.ch/smlm/
//
// Author:
// Daniel Sage, http://bigwww.epfl.ch/sage/
// Biomedical Imaging Group (BIG)
// Ecole Polytechnique Federale de Lausanne (EPFL), CH-1015 Lausanne,
// Switzerland
//
// Reference:
// D. Sage, H. Kirshner, T. Pengo, N. Stuurman, J. Min, S. Manley, M. Unser
// Quantitative Evaluation of Software Packages for Single-Molecule Localization
// Microscopy
// Nature Methods 12, August 2015.
//
// Conditions of use:
// You'll be free to use this software for research purposes, but you
// should not redistribute it without our consent. In addition, we expect you to
// include a
// citation or acknowledgment whenever you present or publish results that are
// based on it.
//
// =========================================================================================

package smlms;

public class Description {
	public String	name			= "noname";
	public double	pixelsize		= 100;
	public double	zstep			= 10;
	public double	shiftX			= 0;
	public double	shiftY			= 0;
	public double	shiftZ			= 0;
	public double	shiftFrame		= 0;
	public int		colX			= 2;
	public int		colY			= 3;
	public int		colZ			= 4;
	public int		colFrame		= 1;
	public int		colIntensity	= 5;
	public int		firstRow		= 0;
	
	public Description() {
		
	}
	
	public Description(String name, int colX, int colY, int colZ, int colFrame, int colIntensity, double pixelsize, double zstep) {
		this.name = name;
		this.colX = colX;
		this.colY = colY;
		this.colZ = colZ;
		this.colFrame = colFrame;
		this.colIntensity = colIntensity;
		this.pixelsize = pixelsize;
		this.zstep = zstep;
	}
	
	public void shift(double shiftX, double shiftY) {
		this.shiftX = shiftX;
		this.shiftY = shiftY;
	}
}
