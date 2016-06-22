package smlms.tools;

import java.util.ArrayList;

public class NormalizedVariable {

	public ArrayList<double[]>	linears		= new ArrayList<double[]>();
	public ArrayList<double[]>	cosines		= new ArrayList<double[]>();
	public ArrayList<double[]>	sigmoids	= new ArrayList<double[]>();
	public double				constant	= 0.0;
	private PsRandom			rand;

	public double[]				uniform		= { 0.0, 0.0, 0.0 };
	public double[]				gaussian	= { 0.0, 0.0, 0.0 };
	public double[]				poisson		= { 0.0, 0.0, 0.0 };
	public double[]				lorentz		= { 0.0, 0.0, 0.0 };
	public double[]				rayleigh	= { 0.0, 0.0, 0.0 };

	public NormalizedVariable(double constant) {
		this.constant = constant;
		rand = new PsRandom(123);
	}

	public NormalizedVariable(double constant, double uniformNoise) {
		this.constant = constant;
		rand = new PsRandom(123);
		uniform[0] = 1.0;
		uniform[1] = constant-uniformNoise;
		uniform[2] = constant+uniformNoise;	
	}

	// x is value between 0 and 1
	public double get(double x) {
		double value = constant;
		for(double[] param : linears)
			value += param[0] * x + param[1];
		for(double[] param : cosines)
			value += param[0] * Math.cos(2*Math.PI*(x*param[1]-param[2]));
		for(double[] param : sigmoids)
			value += param[0] / (1.0 + Math.exp(param[2]*(x-param[1])));
		if (uniform[0] > 0) 
			value += rand.nextDouble(uniform[1], uniform[2]);
		if (gaussian[0] > 0)
			value += rand.nextGaussian(gaussian[1], gaussian[2]);
		if (poisson[0] > 0)
			value += rand.nextPoissonian(poisson[1]);
		if (lorentz[0] > 0)
			value += rand.nextLorentzian(lorentz[1], lorentz[2]);
		if (rayleigh[0] > 0)
			value += rand.nextRayleigh(rayleigh[1]);
		
		return value;
	}

	public void addUniformRandom(double bottom, double top) {
		uniform[0] = 1;
		uniform[1] = bottom;
		uniform[2] = top;
	}

	public void addGaussianRandom(double mean, double sd) {
		gaussian[0] = 1;
		gaussian[1] = mean;
		gaussian[2] = sd;
	}

	public void addPoissonRandom(double mean) {
		poisson[0] = 1;
		poisson[1] = mean;
		poisson[2] = 0;
	}

	public void addLorentzRandom(double mu, double gamma) {
		lorentz[0] = 1;
		lorentz[1] = mu;
		lorentz[2] = gamma;
	}

	public void addRayleighRandom(double sigma) {
		rayleigh[0] = 1;
		rayleigh[1] = sigma;
	}

	public void addLinear(double valueAtMin, double valueAtMax) {
		linears.add(new double[] { valueAtMax - valueAtMin, valueAtMin, 0.0 });
	}

	public void addCosine(double amplitude, double nbCycle, double phase) {
		cosines.add(new double[] { amplitude, nbCycle, phase });
	}

	public void addSigmoid(double amplitude, double position, double widthSlope) {
		sigmoids.add(new double[] { amplitude, position, 6.0 / widthSlope });
	}
	
	public String write() {
		String s = "" + constant + ", ";
		for(double[] param : linears)
			s += "linear, " + param[0] + ", " + param[1] + ", ";
		for(double[] param : cosines)
			s += "cosine, " + param[0] + ", " + param[1] + ", " + param[2] + ", ";
		for(double[] param : sigmoids)
			s += "sigmoid, " + param[0] + ", " + param[1] + ", " + param[2] + ", ";
		if (uniform[0] > 0) 
			s += "uniform, " + uniform[1] + ", " + uniform[2] + ", ";
		if (gaussian[0] > 0)
			s += "gaussian, " + gaussian[1] + ", " + gaussian[2] + ", ";
		if (poisson[0] > 0)
			s += "poisson, " + poisson[1] + ", ";
		if (lorentz[0] > 0)
			s += "lorentz, " + lorentz[1] + ", " + lorentz[2] + ", ";
		if (rayleigh[0] > 0)
			s += "rayleigh, " + rayleigh[1] + ", ";
		return s;
	}
	
	public static NormalizedVariable read(String line) {
		String[] tokens = line.split("[,]");
		if (tokens.length >= 0) {
			NormalizedVariable var = new NormalizedVariable(Double.parseDouble(tokens[0]));
			for(int i=1; i<tokens.length; i++) {
				if (tokens[i].startsWith("uniform"))
					var.addUniformRandom(Double.parseDouble(tokens[++i]), Double.parseDouble(tokens[++i]));
				else if (tokens[i].startsWith("gaussian"))
					var.addGaussianRandom(Double.parseDouble(tokens[++i]), Double.parseDouble(tokens[++i]));
				else if (tokens[i].startsWith("poisson"))
					var.addPoissonRandom(Double.parseDouble(tokens[++i]));
				else if (tokens[i].startsWith("lorentz"))
					var.addLorentzRandom(Double.parseDouble(tokens[++i]), Double.parseDouble(tokens[++i]));
				else if (tokens[i].startsWith("rayleigh"))
					var.addRayleighRandom(Double.parseDouble(tokens[++i]));
				else if (tokens[i].startsWith("linear"))
					var.addLinear(Double.parseDouble(tokens[++i]), Double.parseDouble(tokens[++i]));
				else if (tokens[i].startsWith("cosine"))
					var.addCosine(Double.parseDouble(tokens[++i]), Double.parseDouble(tokens[++i]), Double.parseDouble(tokens[++i]));
				else if (tokens[i].startsWith("sigmoid"))
					var.addSigmoid(Double.parseDouble(tokens[++i]), Double.parseDouble(tokens[++i]), Double.parseDouble(tokens[++i]));
			}
			return var;
		}
		else {
			return new NormalizedVariable(0);
		}
	}

}
