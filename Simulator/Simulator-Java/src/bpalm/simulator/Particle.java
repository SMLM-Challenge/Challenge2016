package bpalm.simulator;

public class Particle {
	public double x,y,z,t;

	public Particle() {
		x=0; y=0; z=0; t=0;
	}
	
	public Particle(Particle p) {
		x=p.x; 
		y=p.y; 
		z=p.z; 
		t=p.t;
	}
	
	public Particle scale(double fX, double fY, double fZ) {
		x=x*fX; y=y*fY; z=z*fZ;
		return this;
	}

	public Particle offset(double fX, double fY, double fZ) {
		x=x+fX; y=y+fY; z=z+fZ;
		return this;
	}
}
