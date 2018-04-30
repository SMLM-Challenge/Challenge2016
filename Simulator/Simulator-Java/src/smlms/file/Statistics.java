package smlms.file;

public class Statistics {

	public String name;
	public int count;
	public double min;
	public double max;
	public double mean;
	public double stdev;
	public double histo[];
	public double domain[];
	public double evolution[] = new double[1];
	
	private int nbins = 100;
	private boolean init = false;
	
	public Statistics(String name) {
		this.name = name;
		this.count = 0;
		this.min = Double.MAX_VALUE;
		this.max = -Double.MAX_VALUE;
		this.mean = 0.0;
		this.stdev = 0.0;
		this.domain = new double[nbins];
		this.histo = new double[nbins];
	}
	
	public void initHisto() {
		for(int i=0; i<nbins; i++) 
			domain[i] =  min + i * (max - min) / nbins;
		init = true;
	}
	
	public void addHisto(double x) {
		if (init == false)
			initHisto();
		int h = (int)(nbins * (x - min) / (max - min));
		if (h >= 0)
			if (h < nbins)
				histo[h]++;
	}
}
