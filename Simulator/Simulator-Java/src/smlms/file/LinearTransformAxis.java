package smlms.file;

import java.util.ArrayList;

public class LinearTransformAxis {
	
	public double a = 1.0;
	public double b = 0.0;

	public LinearTransformAxis() {
	}

	public LinearTransformAxis(double b) {
		this.b = b;
	}
	
	public LinearTransformAxis(double a, double b) {
		this.a = a;
		this.b = b;
	}

	public double transform(double x) {
		return a * x + b;
	}
	
	public void set(ArrayList<Double> params) {
		if (params.size() == 1)
			this.b = params.get(0);
		if (params.size() > 1) {
			this.a = params.get(0);
			this.b = params.get(1);
		}
			
	}

}
