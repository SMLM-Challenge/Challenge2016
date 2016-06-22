package smlms.simulation;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.process.FloatProcessor;
import imageware.ImageWare;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import smlms.file.Fluorophore;
import smlms.file.Fluorophores;
import smlms.file.Statistics;
import smlms.file.TableStatistics;
import smlms.tools.Chart;
import smlms.tools.Chrono;
import smlms.tools.Tools;
import smlms.tools.Verbose;

public class SequenceFactory {

	public static String names[] = new String[] {"Off-1 Thread", "On-Adaptive", "On 1 Core", "On 2 Cores", "On 4 Cores", "On 8 Cores", "On 16 Cores"};
	public int MTHREAD_OFF = 0;
	public int MTHREAD_ADAPTATIVE = 1;
	public int MTHREAD_1_CORE = 2;
	public int MTHREAD_2_CORE = 3;
	public int MTHREAD_4_CORE = 4;
	public int MTHREAD_8_CORE = 5;
	public int MTHREAD_16_CORE = 6;

	public static String modes[]  = new String[]  {"PSF+RAM+Cam", "PSF+File+Cam", "Load+Cam", "PSF+Store"}; 
	private AutofluorescenceModule 	autofluo;
	private NoiseModule 			noise;
	private ProjectionModule 		projection;
	private DownsamplingModule 		downsampling;
	private ArrayList<PSFModule> 	psfs;
	private CameraModule 			camera;
	
	private int upsamplingConvolve;
	private int upsamplingWorking;
	private Viewport viewportCamera;
	
	private String pathOracle;
	private String pathConvolution;
	private String pathProjection;
	private String pathSequence;
	private String pathFluosFrame;
	private String dataset;
	
	private int first = 0;
	private int last = 0;
	private int interval = 0;
	private ImageStack stackSequence;
	
	public SequenceFactory(String dataset, int first, int last, int interval, 
			CameraModule camera, ArrayList<PSFModule> psfs, NoiseModule noise, AutofluorescenceModule autofluo, 
			Viewport viewportCamera, int upsamplingConvolve , int upsamplingWorking) {
		
		this.dataset = dataset;
		this.first = first;
		this.last = last;
		this.interval = interval;
		
		this.camera = camera;
		this.psfs = psfs;
		this.noise = noise;
		this.autofluo = autofluo;
		
		this.upsamplingConvolve  = upsamplingConvolve;
		this.upsamplingWorking = upsamplingWorking;
		this.viewportCamera = viewportCamera;
		downsampling = new DownsamplingModule();	
		double pxc = viewportCamera.getPixelsize();
		Verbose.talk(" Working " + (pxc / upsamplingWorking) + " Convolve " + (pxc / (upsamplingWorking * upsamplingConvolve)));
	}

	public void generate(String path, int multithread, Fluorophores[] fluorophoresAll, double fwhmNano, int mode, boolean project, boolean stats, SequenceReporting report) {
		int cores = getCores(multithread);
		int nax = viewportCamera.getFoVXPixel() * upsamplingConvolve;
		int nay = viewportCamera.getFoVYPixel() * upsamplingConvolve;					
		Fluorophores fluosAll = new Fluorophores();
		for(Fluorophores fluos : fluorophoresAll)
			fluosAll.addAll(fluos);

		if (autofluo != null && autofluo.backPoisson > 0) {
			double corr = ((double)upsamplingWorking/upsamplingConvolve);
			autofluo.create(nax, nay, fluosAll, corr*corr);
		}
		
		for(PSFModule psf : psfs) {
			IJ.log("\n\n PSF " + psf.getName() + " " + psfs.size());
			stackSequence = null;
			String pathDataset = new File(path).getParent();
			String pathMain = pathDataset + File.separator + psf.getName() + File.separator;
			Verbose.talk("PSF " + pathMain);
			pathOracle = pathMain + "oracle" + File.separator;
			pathSequence = pathMain + "sequence" + File.separator;
			pathFluosFrame = pathMain + "fluorophores" + File.separator;
			pathProjection = pathOracle + "projection" + File.separator;
			pathConvolution = pathMain + "convolution" + File.separator;
			(new File(pathMain)).mkdir();
			(new File(pathOracle)).mkdir();
			(new File(pathSequence)).mkdir();
			(new File(pathFluosFrame)).mkdir();
			(new File(pathProjection)).mkdir();
			(new File(pathConvolution)).mkdir();
			generate(pathMain, psf, cores, fluorophoresAll, fwhmNano, mode, project, stats);
			report.reportWeb(dataset, pathMain, pathOracle, pathSequence, psf);
		}
	}
	
	/**
	 */
	public void generate(String pathMain, PSFModule psf, int cores, Fluorophores[] fluorophoresAll, double fwhmNano, int mode, boolean project, boolean stats) {
		
		last = Math.min(last, fluorophoresAll.length-1);
		if (project)
			projection = new ProjectionModule(viewportCamera, upsamplingConvolve);
		
		Verbose.talk("\nGenerate for frames " + first + " to " + last + " every " + interval + " on cores: " + cores + " mode (" + mode + ") " + modes[mode]);

		Chrono.reset(6);

		BufferedWriter buffer = null;
		try {
			if (stats)
				buffer = new BufferedWriter(new FileWriter(new File(pathOracle + "activation-snr.csv")));
		} 
		catch(Exception ex) {};
		boolean actions[] = new boolean[4];
		actions[0] = mode != 2; // Convolve
		actions[1] = mode == 1 || mode == 3; // Store
		actions[2] = mode == 1 || mode == 2; // Load
		actions[3] = mode != 3; // generate	
		Fluorophores fluorophoresProcessed = new Fluorophores();
		
		if (cores > 0) {
			int nbFrames = last-first+1;
			LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(nbFrames);
			ExecutorService executor = new ThreadPoolExecutor(cores, cores, 50000L, TimeUnit.MILLISECONDS, queue);
			Vector<Future<FrameGenerator>> futures = new Vector<Future<FrameGenerator>>();
			for(int frame = first; frame<=last; frame+=interval) {	
				FrameGenerator task = new FrameGenerator(pathSequence, psf, buffer, fluorophoresAll[frame], frame, fwhmNano, actions, project, stats, fluorophoresProcessed);
				Future<FrameGenerator> f = executor.submit(task, task);
				futures.add(f);
			}
			executor.shutdown();
			try {
				for (int i=0; i<futures.size(); i++) {
					Future<?> f = futures.get(i);
					f.get();
				}
			}
			catch(Exception ex) {
				IJ.log("Error " + ex);
			}
		}
		else {
			for(int frame = first; frame<=last; frame+=interval) {
				FrameGenerator task = new FrameGenerator(pathSequence, psf, buffer, fluorophoresAll[frame], frame, fwhmNano, actions, project, stats, fluorophoresProcessed);
				task.run();
			}
		}

		try { 
			if (stats)
				buffer.close(); 
		} catch(Exception ex) {};
		
		if (project) {
			projection.store(pathProjection, camera.getFormat());
			//projection.show();
		}
		
		if (stats) {
			fluorophoresProcessed.computeStats();
			Statistics statistics[] = fluorophoresProcessed.getStats();
			TableStatistics table = new TableStatistics("Statistics " + psf.getName(), statistics);
			//table.show(500, 500);
			table.saveCVS(pathOracle + "stats-snr.csv");
			String pathHisto = pathOracle + "histo" + File.separator;
			new File(pathHisto).mkdir();
			
			for(int i=1; i<statistics.length; i++) 
				if (statistics[i].max > statistics[i].min) {
					Chart chart = new Chart(statistics[i].name + "-" + dataset + "-" + psf.getName(), "Number of fluorophores", statistics[i].domain);
					chart.add(statistics[i].name, statistics[i].histo);
					if (statistics[i].name.endsWith("X"))
						chart.add(statistics[i+1].name, statistics[i+1].histo);		
					//if (statistics[i].name.endsWith("NR"))
					//	chart.show(statistics[i].name, 800, 400);
					if (!statistics[i].name.endsWith("ID") && !statistics[i].name.endsWith("Count") && !statistics[i].name.endsWith("Y"))
						chart.savePNG(pathHisto + "Histogram-" + statistics[i].name + ".png", 800, 400);
				}
		}
		Chrono.print("End", 6);
		
		if (stackSequence != null) {
			ImagePlus out = new ImagePlus("sequence-"+psf.getName(), stackSequence);
			out.show();
			String pathStack = pathMain + "sequence-as-stack" + File.separator;
			new File(pathStack).mkdir();
			(new FileSaver(out)).saveAsTiffStack(pathStack + "sequence-as-stack-" + dataset + "-" + psf.getName() + ".tif");	
		}
	}
	
	private int getCores(int multihreading) {
		int cores = 0;
		if (multihreading == MTHREAD_ADAPTATIVE)
			cores = Runtime.getRuntime().availableProcessors();
		else if (multihreading == MTHREAD_1_CORE)
			cores = 1;
		else if (multihreading == MTHREAD_2_CORE)
			cores = 2;
		else if (multihreading == MTHREAD_4_CORE)
			cores = 4;
		else if (multihreading == MTHREAD_8_CORE)
			cores = 8;
		else if (multihreading == MTHREAD_16_CORE)
			cores = 16;
		return cores;
	}

	/**
	 * Generate one frame, can run in a separated thread.
	 */
	public class FrameGenerator implements Runnable {
		private int 	numberFrame;
		private String 	pathFrames;
		private boolean actions[];
		private boolean project;
		private boolean stats;
		private double 	fwhmNano;
		private PSFModule psf;
		private BufferedWriter buffer;
		private Fluorophores fluorophores;
		public Fluorophores fluorophoresFrame;
		public Fluorophores fluorophoresProcessed;
		
		public FrameGenerator(String pathFrames, PSFModule psf, BufferedWriter buffer, Fluorophores fluorophores, 
				int numberFrame, double fwhmNano, boolean actions[], boolean project, boolean stats,
				Fluorophores fluorophoresProcessed) {
			this.pathFrames =  pathFrames;
			this.fluorophores = fluorophores;
			this.numberFrame = numberFrame;
			this.actions = actions;
			this.project = project;
			this.stats = stats;
			this.psf = psf;
			this.fwhmNano = fwhmNano;
			this.buffer = buffer;
			this.fluorophoresProcessed = fluorophoresProcessed;
			fluorophoresFrame = new Fluorophores();
			for(Fluorophore fluo : fluorophores)
				if (viewportCamera.insideXY(fluo))
					fluorophoresFrame.add(fluo);
		}
		
		@Override
		public void run() {
	
			double chrono = System.currentTimeMillis();
			String filename = pathConvolution + Tools.format(numberFrame);
			int fovX = viewportCamera.getFoVXPixel() * upsamplingWorking * upsamplingConvolve;
			int fovY = viewportCamera.getFoVYPixel() * upsamplingWorking * upsamplingConvolve;					
			float imageConvolve[][] = new float[fovX][fovY];
			float imageWorking[][] = null;
			if (actions[0]) {
				//Chrono.reset();
				psf.convolve(fluorophoresFrame, imageConvolve, fwhmNano / upsamplingConvolve);
				//Debug Builder.create(imageConvolve).show("Conv-" + p + "-" + psf[p].getName());		
				imageWorking = downsampling.run(imageConvolve, upsamplingWorking);
				//Chrono.print("Convolve (" + numberFrame + ") fluos:" + fluorophores.size());
			}
			
			if (actions[1]) {
				Chrono.reset();
				ImagePlus imp = new ImagePlus(filename, new FloatProcessor(imageWorking));	
				(new FileSaver(imp)).saveAsTiff(filename + ".tif");
				Chrono.print("Store " + filename);
			}
		
			if (actions[2]) {
				Chrono.reset();
				ImagePlus imp = new Opener().openImage(filename + ".tif");
				imageWorking = ((FloatProcessor)imp.getProcessor()).getFloatArray();
				Chrono.print("Load " + filename);
			}
			
			if (actions[3] && imageWorking != null) {
				//if (autofluo != null)
				//	autofluo.add(imageWorking);
				
				float imageCam[][] = downsampling.run(imageWorking, upsamplingConvolve);
				if (noise != null)
					noise.add(imageCam, autofluo.backPoisson);
				camera.format(imageCam);
				if (stats) {
					for(Fluorophore fluo : fluorophores) {
						computeSNR(imageCam, fluo, fwhmNano);
					}

					for(Fluorophore fluo : fluorophores)
						computeClosest(fluo, fluorophores, fwhmNano);
					for(Fluorophore fluo : fluorophores) {
						try { buffer.write(fluo.vectorValuesAsString()); } catch(Exception ex) {};
					}
				}
				if (project)
					projection.projectAtCameraResolution(imageCam);
				
				ImagePlus imp = camera.storeFrame(pathFrames, imageCam, numberFrame);
				fluorophoresFrame.save(pathFluosFrame + File.separator + Tools.format(numberFrame) + ".csv");
				if (stackSequence == null)
					stackSequence = new ImageStack(imp.getWidth(), imp.getHeight());
				stackSequence.addSlice(""+numberFrame, imp.getProcessor());
			}
			
			if (project) {
				projection.projectAtWorkingResolution(imageWorking);
			}
			Verbose.prolix("" + psf.getName() + " f:" + numberFrame + " n:" + fluorophoresFrame.size() + " t:" + IJ.d2s((System.currentTimeMillis()-chrono)*1e-3) + "us");	
			fluorophoresProcessed.addAll(fluorophoresFrame);
		}
		
		public void computeClosest(Fluorophore fluo, Fluorophores fluos, double fwhm) {
			int id = 0;
			double min = 2 * fwhm;
			int count = 0;
			for(Fluorophore f : fluos) {
				double d = f.distance(fluo);
				if (d > 0.0000001) {
					if (d < min) {
						min = d;
						id = f.id;
					}
				}
				if (d < fwhm) {
					count++;
				}
			}
			fluo.closestDistance = min;
			fluo.closestID = id;
			fluo.closestCount = count;
		}
		
		public void computeSNR(float[][] image, Fluorophore fluo, double fwhmNano) {
			int nx = image.length;
			double pixelsize 	= viewportCamera.getPixelsize();
			int i = Tools.round(fluo.x / pixelsize);
			int j = Tools.round(fluo.y / pixelsize);
			int h = (int) Math.ceil(fwhmNano / pixelsize);
			int n = 2 * h;
			int m = 2 * n + 1;
			int u = m - h - 1;
			if (h == 0)
				return;

	
			double block[][] = new double[m][m];
			double test[][] = new double[m][m];
			for (int x = 0; x <m; x++)
			for (int y = 0; y <m; y++) {
				int ii = i - n + x;
				int jj = j - n + y;
				if (ii >= 0 && ii < nx)
				if (jj >= 0 && jj < nx)
					block[x][y] = image[ii][jj];
			}
			
			// Signal
			double meanSignal = 0.0;
			double meanBackground = 0.0;
			double maxSignal = 0;
			int countSignal = 0;
			int countBackground = 0;
			for (int x = 0; x <m; x++)
			for (int y = 0; y <m; y++) {
				if (x > h && x < u && y > h && y < u) {
					meanSignal += block[x][y];
					maxSignal = Math.max(block[x][y], maxSignal);
					countSignal++;
					test[x][y] = 100;
				}
				else {
					meanBackground += block[x][y];
					countBackground++;
					test[x][y] = -100;
				}
			}

			meanSignal = meanSignal / countSignal;
			meanBackground = meanBackground / countBackground;

			double noiseSignal = 0;
			double noiseBackground = 0;
			for (int x = 0; x <n; x++)
			for (int y = 0; y <n; y++) {
				if (x > h && x < u && y > h && y < u) 
					noiseSignal += (block[x][y]-meanSignal) * (block[x][y]-meanSignal);
				else
					noiseBackground += (block[x][y] - meanBackground) * (block[x][y] - meanBackground);
			}
			noiseSignal = Math.sqrt(noiseSignal / countSignal);
			noiseBackground = Math.sqrt(noiseBackground / countBackground);
			
			fluo.setSNR(meanBackground, noiseBackground, maxSignal, meanSignal, noiseSignal);
			
		}
		
		public void displaySNR(ImageWare image, Fluorophore fluo, double fwhmPixel, Overlay overlay, int factor, double pixelsize) {
			int i = Tools.round(fluo.x / pixelsize);
			int j = Tools.round(fluo.y / pixelsize);
			int h = (int) Math.ceil(fwhmPixel);
			int n = 2 * h + 1;
			if (overlay != null) {
				overlay.add(new Roi((i-h)*factor, (j-h)*factor, (2*h+1)*factor, (2*h+1)*factor));
				overlay.add(new Roi((i-n)*factor, (j-n)*factor, (2*n+1)*factor, (2*n+1)*factor));	
				overlay.add(new TextRoi(i*factor, j*factor-20, IJ.d2s(fluo.psnr,1)));					
			}
		}
		
		/*
		private void computeSNR(int numberFrame, Fluorophores fluos, float[][] array, double fwhmNano, boolean displayPSNR) {
			
			Overlay overlay = null;
			ImagePlus imp = null;
			int factorPSNR = 5;
			if(displayPSNR) {
				overlay = new Overlay();
				int n = array.length;
				imp = new ImagePlus("SNR " + numberFrame, new FloatProcessor(n*factorPSNR, n*factorPSNR));
				FloatProcessor fp = (FloatProcessor)imp.getProcessor();
				for(int x=0; x<n*factorPSNR; x++)
				for(int y=0; y<n*factorPSNR; y++)
					fp.putPixelValue(x, y, array[x/factorPSNR][y/factorPSNR]);
				imp.show();
			}

			
			ImageWare image = Builder.create(array);
			double pixelsize = viewportCamera.getPixelsize();
			double fwhmPixel = (float)(fwhmNano / pixelsize);
			int count = 0;
			for(Fluorophore fluo : fluos) {
				computeSNR(image, fluo, fwhmPixel, pixelsize);
				displaySNR(image, fluo, fwhmPixel, overlay, factorPSNR, pixelsize);
			}
			
			if (imp != null && overlay != null) {
				imp.setOverlay(overlay);
				imp.show();
			}
		}
		*/

	}
	

}
