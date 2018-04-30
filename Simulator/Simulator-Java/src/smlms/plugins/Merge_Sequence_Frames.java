package smlms.plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.io.Opener;
import imageware.Builder;
import imageware.ImageWare;

import java.io.File;

import smlms.tools.Zip;

public class Merge_Sequence_Frames {

	public static String  dataset = "MT2.N2";
	
	public static void main(String[] arg) {
		Merge_Sequence_Frames merge = new Merge_Sequence_Frames();
		String path1 	= "/Users/sage/Desktop/activation-may/" + dataset +"/BP-000/";
		String path2 	= "/Users/sage/Desktop/activation-may/" + dataset +"/BP-500/";
		String pathout 	= "/Users/sage/Desktop/activation-may/" + dataset +"/BP-Exp/sequence-MT2.N1-BP-Exp/";
		merge.run(path1, path2, pathout);
	}
	
	
	public void run(String path1, String path2, String pathout) {
		String list1[] = new File(path1 + "/sequence").list();
		String list2[] = new File(path2 + "/sequence").list();
		new File(pathout).mkdir();
		new File(pathout + "/sequence").mkdir();
		Opener opener = new Opener();
		IJ.log("Path1 "  + path1);
		IJ.log("Path2 "  + path2);
		IJ.log("Number of files " + list1.length + " in "  + path1);
		IJ.log("Number of files " + list2.length + " in "  + path2);
	
		for(int i=0; i<Math.min(list1.length, list2.length); i++) {
			IJ.log(" " + list1[i]);
			ImagePlus imp1 = opener.openImage(path1 + "/sequence/" + list1[i]);
			ImagePlus imp2 = opener.openImage(path2 + "/sequence/" + list2[i]);
			ImageWare image1 = Builder.wrap(imp1);
			ImageWare image2 = Builder.wrap(imp2);
			int nx1 = image1.getSizeX();
			int nx2 = image1.getSizeX();
			int ny1 = image2.getSizeX();
			ImageWare image = Builder.create(nx1+nx2, ny1, 1, image1.getType());
			image.putXY(0,  0,  0, image1);
			image.putXY(nx1,  0,  0, image2);
			ImagePlus imp = new ImagePlus("", image.buildImageStack());
			(new FileSaver(imp)).saveAsTiff(pathout + "/sequence/" + File.separator + list1[i]);	
	}
	
	Zip.zipFolder(pathout + "/sequence/", pathout + "sequence-" + dataset + "-BP-Exp-as-list.zip");
	}
}
