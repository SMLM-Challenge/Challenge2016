package bpalm.simulator.common.generators;

public class SpiralFunction implements Function {
	double turns;
	
	public SpiralFunction() {
		this(1);
	}
	
	public SpiralFunction(double turns) {
		this.turns = turns;
	}
	
	@Override public double getX(double t) { return (t*Math.sin(t*2*Math.PI*turns)/2+.5); }
	@Override public double getY(double t) { return (t*Math.cos(t*2*Math.PI*turns)/2+.5); }
	@Override public double getZ(double t) { return t; }
}