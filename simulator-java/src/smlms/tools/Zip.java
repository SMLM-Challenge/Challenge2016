package smlms.tools;

import ij.IJ;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {
	

	static public void zipFolder(String dir, String zipFileName) {
	    File dirObj = new File(dir);
	    try {
	    	ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
	    	IJ.log("Creating : " + zipFileName);
	    	addDir(dirObj, out);
	    	out.close();
	    }
	    catch(Exception ex) {
	    	  IJ.log(ex.toString());	    	
	    }
	}

	static private void addDir(File dirObj, ZipOutputStream out) {
	    File[] files = dirObj.listFiles();
	    byte[] tmpBuf = new byte[1024];

	    for (int i = 0; i < files.length; i++) {
	      try {
	    	  FileInputStream in = new FileInputStream(files[i].getAbsolutePath());
	    	  out.putNextEntry(new ZipEntry(files[i].getName()));
	    	  int len;
	    	  while ((len = in.read(tmpBuf)) > 0) {
	    		  out.write(tmpBuf, 0, len);
	    	  }
	    	  out.closeEntry();
	    	  in.close();
	      }
	      catch(Exception ex) {
	    	  IJ.log(ex.toString());
	      }
	    }
	  }
/*
	public static void zipFolder(String folder, String zipfile) {
		try {
			File inFolder = new File(folder);
			File outFolder = new File(zipfile);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outFolder)));
			BufferedInputStream in = null;
			byte[] data  = new byte[1000];
			String files[] = inFolder.list();
			for (int i=0; i<files.length; i++) {
					in = new BufferedInputStream(new FileInputStream(inFolder.getPath() + "/" + files[i]), 1000);  
				out.putNextEntry(new ZipEntry(files[i])); 
				int count;
				while((count = in.read(data,0,1000)) != -1) {
					out.write(data, 0, count);
				}
				out.closeEntry();
			}
			out.flush();
			out.close();
		}
		catch(Exception e) {
			IJ.error("" + e);
		} 
	}
	*/
} 