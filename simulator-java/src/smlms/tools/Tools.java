//=========================================================================================
//
// Project: Localization Microscopy
//
// Author : Daniel Sage, Biomedical Imaging Group (BIG), http://bigwww.epfl.ch/sage/
//
// Organization: Ecole Polytechnique F�d�rale de Lausanne (EPFL), Lausanne, Switzerland
//
// Conditions of use: You'll be free to use this software for research purposes, but you 
// should not redistribute it without our consent. In addition, we expect you to include a
// citation or acknowledgment whenever you present or publish results that are based on it.
//
//=========================================================================================
package smlms.tools;

import ij.IJ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextField;


public class Tools {
	
	public static String getSMLMSPath() {
		String s = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "smlms-data" + File.separator;
		new File(s).mkdir();
		return s;
	}
	
	
	public static ArrayList<Double> extractDoubles(String text) {
		Pattern p = Pattern.compile("[-]?[0-9]*\\.?[0-9]+");
		Matcher m = p.matcher(text.trim());
		ArrayList<Double> doubles = new ArrayList<Double>();
		while (m.find()) {
			doubles.add(Double.parseDouble(m.group()));
		}
		return doubles;
	}
	
	public static double extractDouble(String text) {
		Pattern p = Pattern.compile("[-]?[0-9]*\\.?[0-9]+");
		Matcher m = p.matcher(text.trim());
		while (m.find()) {
			return Double.parseDouble(m.group());
		}
		return 0.0;
	}

	public static double extractDouble(JTextField txt) {
		return extractDouble(txt.getText().trim());
	}

	public static String[] concat(String[] list, String element) {
		String[] s = new String[list.length+1];
		for(int i=0; i<list.length; i++)
			s[i] = list[i];
		s[list.length] = element;
		return s;
	}
	
	public static String readFile(String filename) {
		try {
			StringBuffer fileData = new StringBuffer(1000);
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			}
			reader.close();
			return fileData.toString();
		}
		catch(Exception ex) {
			
		}
		return "";
	}

	public static void copyFile(String sourceFile, String destFile) {
		try {
			copyFile(new File(sourceFile), new File(destFile));
		}
		catch(IOException e) {}
		
	}

	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}
	
	public static int round(double a) {
		return (int)Math.round(a);
	}


	public static String format(int n) {
		String w = "" + n;
		if (n < 10)
			 w = "0000" + w;
		else if (n < 100)
			 w = "000" + w;
		else if (n < 1000)
			 w = "00" + w;
		else if (n < 10000)
			 w = "0" + w;
		return w;
	}

	public static String formatNanoCubetoString(double volnm) {
		if (volnm > 10000000000.0) {
			return IJ.d2s(volnm/1000000000.0, 1) + " um3";
		}
		if (volnm > 1000000000.0) {
			return IJ.d2s(volnm/1000000000.0, 2) + " um3";
		}
		if (volnm > 100000000.0) {
			return IJ.d2s(volnm/1000000000.0, 3) + " um3";
		}
		if (volnm > 1000000.0) {
			return IJ.d2s(volnm/1000000000.0, 4) + " um3";
		}
		return IJ.d2s(volnm, 0) + " nm3";
		
	}
}
