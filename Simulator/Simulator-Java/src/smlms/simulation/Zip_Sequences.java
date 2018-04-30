package smlms.simulation;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.io.Opener;

import java.io.File;

import smlms.plugins.Merge_Sequence_Frames;
import smlms.tools.Zip;

public class Zip_Sequences {

	public static String	path	= "/Users/dsage/Desktop/beads/beads6/";
	public static String[]	psfs	= new String[] { "2D-Exp", "AS-Exp", "DH-Exp", "BP-Exp" };

	public static void main(String args[]) {
		new Zip_Sequences();
	}

	public Zip_Sequences() {
		Merge_Sequence_Frames merge = new Merge_Sequence_Frames();
		merge.run(path+"BP000-Exp", path+"BP500-Exp", path + "BP-Exp");
		for (int i = 0; i < psfs.length; i++) {
			String p = path + psfs[i] + File.separator + "sequence/";
			Zip.zipFolder(p, path + "stack-beads-100nm-" + psfs[i] + "-100x100x10-as-list.zip");

			String[] list = new File(p).list();
			ImageStack stack = null;
			Opener opener = new Opener();
			for (int j = 0; j < list.length; j++) {
				IJ.log(p + list[j]);
				ImagePlus imp = opener.openImage(p + list[j]);
				if (imp != null) {
					if (stack == null) stack = new ImageStack(imp.getWidth(), imp.getHeight());
					stack.addSlice("", imp.getProcessor());
				}
			}
			String filename = "stack-beads-100nm-" + psfs[i] + "-100x100x10-as-stack";
			ImagePlus out = new ImagePlus(filename, stack);
			String folder = path + psfs[i] + "-stack/";
			new File(folder).mkdir();
			FileSaver saver = new FileSaver(out);
			saver.saveAsTiffStack(folder + filename + ".tif");
			Zip.zipFolder(folder, path + filename + ".zip");

		}

	}
}
