package smlms.simulation;

import ij.ImagePlus;
import ij.io.FileSaver;
import imageware.Builder;
import imageware.ImageWare;
import smlms.tools.Tools;

public class CameraModule {

	public static String[] names = new String[] {"TIFF 8-bits", "TIFF 16-bits", "TIFF 32-bits", "JPEG"};
	public static String[] quantizationNames = new String[] 
			{"No > real value", "8-bit", "10-bit", "12-bit", "14-bit", "16-bit", "20-bit", "24-bit"};
	
	public static int quantizationValue[] = {0, 8, 10, 12, 14, 16, 20, 24};
	
	private String		quantization 	= "No > real";
	private double		saturation 		= 10000;
	private String		format 			= CameraModule.names[0];
	
	
	public CameraModule(String quantization, double saturation, String format) {
		this.quantization 	= quantization;
		this.saturation 	= saturation;
		this.format 		= format;
	}

	public void format(float[][] frame) {
		int n = frame.length;
		int m = frame[0].length;
		
		int index = 0;
		for(int i=0; i<quantizationValue.length; i++)
			if (quantization.equals(quantizationNames[i])) 
				index = i;
	
		int level = quantizationValue[index];
		if (level != 0)
			saturation = Math.min(saturation, Math.pow(2, level));
		
		for(int x=0;x<n;x++)
		for(int y=0;y<m;y++) {
			double dn = Math.min(saturation, Math.max(0, frame[x][y]));
			if (level == 0)
				frame[x][y] = (float)dn;
			else
				frame[x][y] = (int)Math.floor(dn);
		}
	}
	
	public double getBaseline() {
		if (quantization.equals("No > real value")) {
			return -10000000;
		}
		return 0.0;
	}
	
	public double getSaturation() {
		if (quantization.equals("No > real value")) {
			return 10000000;
		}
		int index = 0;
		for(int i=0; i<quantizationValue.length; i++)
			if (quantization.equals(quantizationNames[i])) 
				index = i;
		return Math.pow(2, quantizationValue[index])-1;
	}
	
	public ImagePlus storeFrame(String path, float[][] camera, int number) {
		
		ImagePlus imp;
		ImageWare im32 = Builder.create(camera);
		if (format.equals(names[0]))
			imp = new ImagePlus(""+number, im32.convert(ImageWare.BYTE).buildImageStack());
		else if (format.equals(names[1]))
			imp = new ImagePlus(""+number, im32.convert(ImageWare.SHORT).buildImageStack());
		else
			imp = new ImagePlus(""+number, im32.buildImageStack());
		
		String filename = path + Tools.format(number);
		if (format.equals(names[3]))
			(new FileSaver(imp)).saveAsJpeg(filename + ".jpg");
		else
			(new FileSaver(imp)).saveAsTiff(filename + ".tif");	
		return imp;
	}
	
	public String getFormat() {
		return format;
	}
	/*
	public void report(PrintStream out) {	
		out.print("<h2>Camera</h2>");
		out.print("<table cellpadding=5>");			
		out.print("<tr><td></td><td>Gain</td><td>" + gain + "</td><td></td></tr>");
		out.print("<tr><td></td><td>Baseline</td><td>" + baseline + "</td><td></td></tr>");
		out.print("<tr><td></td><td>Saturation</td><td>" + saturation + "</td><td></td></tr>");
		out.print("<tr><td></td><td>Quantization</td><td>" + quantization + "</td><td>bits</td></tr>");
		out.print("<tr><td></td><td>File Format</td><td>" + format + "</td><td></td></tr>");
		out.print("</table>");
	}*/
}
