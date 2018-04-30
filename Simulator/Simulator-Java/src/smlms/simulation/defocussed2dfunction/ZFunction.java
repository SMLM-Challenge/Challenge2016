package smlms.simulation.defocussed2dfunction;

public class ZFunction {
	
	final static public int ZFUNC_EXPO		 	= 0;
	final static public int ZFUNC_ANGLE 		= 1;
	final static public int ZFUNC_EXPO2 		= 2;
	final static public int ZFUNC_CONSTANT 		= 3;

	static public String[] names	 = new String[] {"Exponential", "Linear (angle)", "Exponential (max. 2)", "Constant"};

	private int func1D = 1;
	private double zdefocus = 1.0;
	private double zfocal = 1.0;
	
	public ZFunction(int func1D, double zdefocus, double zfocal) {
		this.func1D = func1D;
		this.zdefocus = zdefocus;
		this.zfocal = zfocal;
	}
	
	public double getDefocusFactor(double z) {
		double a, za, zf;
		zf = z - zfocal;
		switch(func1D) {
		case ZFUNC_EXPO:
			double K = -1.38629436 *0.5; // log(0.5)
			za = (zf<0?zf:-zf);
			return Math.exp(za*K/zdefocus);
				
		case ZFUNC_EXPO2:
			double K2 = -1.38629436 *0.5; // log(0.5)
			za = (zf<0?zf:-zf);
			return Math.min(2, Math.exp(za*K2/zdefocus));
			
		case ZFUNC_CONSTANT:
			return 1.0;
			
		case ZFUNC_ANGLE:
			a = Math.PI*0.5/(zdefocus-zfocal);
			return a * zf;
		}
		return 1.0;
	}
	
	public String getName() {
		return names[func1D];
	}
	
	public String toString() {
		return names[func1D] + " " + zfocal + " " + zdefocus;
	}
}
