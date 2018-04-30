package smlms.file;

import ij.IJ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import smlms.tools.Tools;

public class Description {

	private HashMap<Integer, Fields>	fields	         = new HashMap<Integer, Fields>();
	public LinearTransformAxis	     axisX	             = new LinearTransformAxis();
	public LinearTransformAxis	     axisY	             = new LinearTransformAxis();
	public LinearTransformAxis	     axisZ	             = new LinearTransformAxis();
	public LinearTransformAxis	     axisT	             = new LinearTransformAxis();
	public int	                     numberOfHeadingRows	= 0;
	private String	                 description	     = "";

	public static void main(String args[]) {
		Description desc1 = new Description();
		System.out.println("Desc1 \n" + desc1.toString());
		Description desc2 = new Description("# sigmax Xnm Ypix ? * Z frame 3 ; X 100 ; T 0.5 0.3  ");
		System.out.println("Desc2 \n" + desc2.toString());
		Description desc3 = new Description("/Users/sage/Desktop/des.txt");
		System.out.println("Desc3 \n" + desc3.toString());
		Description desc4 = new Description("activations");
		System.out.println("Desc4 \n" + desc4.toString());
	}

	public static String getDescriptionPath() {
		String s = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "smlms-data" + File.separator + "descriptions" + File.separator;
		new File(s).mkdir();
		return s;
	}

	public Description() {
		fields.put(1, Fields.ID);
		fields.put(2, Fields.X);
		fields.put(3, Fields.Y);
		fields.put(4, Fields.Z);
		fields.put(5, Fields.FRAME);
		fields.put(6, Fields.PHOTONS);
	}

	// Type | Field_sorted_by_columns | ScaleX Y Z T | ShiftX Y Z T
	// Type | ID X Y Z * * FRAME | 100 100 1 1 | 0.5 0.5 0.5 0
	public Description(String line) {
		if (line == null)
			return;
		line = line.trim();

		String items[] = line.split("#");
		IJ.log("DECOMPOSE " + line);
		if (items.length == 3) {
			line = items[1];
			IJ.log("IN FILE " + line);
		}
		else {
			File file = new File(getDescriptionPath() + line);
			if (file.exists()) {
				try {
					BufferedReader buffer = new BufferedReader(new FileReader(getDescriptionPath() + line));
					line = buffer.readLine();
					buffer.close();
				}
				catch (Exception ex) {
					line = "Error in reading " + getDescriptionPath() + line;
				}
			}
		}

		line = line.toLowerCase();
		description = line.trim();
		String features[] = description.split(";");
		for (String feature : features) {
			String feat = feature.trim().toLowerCase();
			if (feat.startsWith("#")) {
				createFields(feat);
				numberOfHeadingRows = (int) Tools.extractDouble(feat);
			}
			if (feat.startsWith("x"))
				axisX = createAxis(feat);
			if (feat.startsWith("y"))
				axisY = createAxis(feat);
			if (feat.startsWith("z"))
				axisZ = createAxis(feat);
			if (feat.startsWith("t"))
				axisT = createAxis(feat);
		}

		for (int i = 0; i < fields.size(); i++)
			IJ.log("Create Column " + i + " > Fields " + fields.get(i));
		IJ.log(" AXIS X " + axisX.a + " " + axisX.b);
	}

	public void addField(String add) {
		String sf = "# ";
		Iterator<Integer> iterator = fields.keySet().iterator();
		while (iterator.hasNext()) {
			Integer mentry = iterator.next();
			sf += fields.get(mentry) + " ";
		}
		createFields(sf + add);
	}

	public String getDecription() {
		return description;
	}

	public String createDescriptionLine() {
		String sf = "# ";
		Iterator<Integer> iterator = fields.keySet().iterator();
		while (iterator.hasNext()) {
			Integer mentry = iterator.next();
			sf += fields.get(mentry) + " ";
		}

		if (numberOfHeadingRows > 0)
			sf += " " + numberOfHeadingRows + "; ";
		else
			sf += "; ";

		String sx = axisX.b == 0.0 ? (axisX.a == 1.0 ? "" : "X " + axisX.a + "; ") : "X " + axisX.a + " " + axisX.b + "; ";
		String sy = axisY.b == 0.0 ? (axisY.a == 1.0 ? "" : "Y " + axisY.a + "; ") : "Y " + axisY.a + " " + axisY.b + "; ";
		String sz = axisZ.b == 0.0 ? (axisZ.a == 1.0 ? "" : "Z " + axisZ.a + "; ") : "Z " + axisZ.a + " " + axisZ.b + "; ";
		String st = axisT.b == 0.0 ? (axisT.a == 1.0 ? "" : "T " + axisT.a + "; ") : "T " + axisT.a + " " + axisT.b + "; ";

		description = sf + sx + sy + sz + st;
		return description;
	}

	public static String[] getRegisteredDescription() {
		String path = getDescriptionPath();
		File dir = new File(path);
		dir.mkdir();
		String list[] = dir.list();
		ArrayList<String> listValid = new ArrayList<String>();
		for (int i = 0; i < list.length; i++) {
			if (!list[i].startsWith(".")) {
				listValid.add(list[i]);
			}
		}
		String[] listFinal = new String[listValid.size()];
		for (int i = 0; i < listFinal.length; i++) {
			listFinal[i] = listValid.get(i);
		}
		return listFinal;
	}

	public HashMap<Integer, Fields> getFields() {
		return fields;
	}

	public boolean[] getActiveFields() {
		boolean activeFields[] = new boolean[Fluorophore.vectorSize()];
		for (int i = 0; i < activeFields.length; i++)
			activeFields[i] = false;
		String names[] = Fluorophore.vectorNames();
		Iterator<Integer> iterator = fields.keySet().iterator();
		while (iterator.hasNext()) {
			Integer mentry = iterator.next();
			for (int i = 0; i < names.length; i++) {
				if (names[i].toLowerCase().equals(fields.get(mentry).name().toLowerCase()))
					activeFields[i] = true;
			}
		}
		return activeFields;
	}

	public String toString() {
		String s = "Fields: ";
		Iterator<Integer> iterator = fields.keySet().iterator();
		while (iterator.hasNext()) {
			Integer mentry = iterator.next();
			s += " " + mentry.toString() + "=" + fields.get(mentry);
		}
		s += " HeadingRows=" + numberOfHeadingRows + ";\n";
		s += "AxisX: " + axisX.a + " * x + " + axisX.b + "; ";
		s += "AxisY: " + axisY.a + " * y + " + axisY.b + "; ";
		s += "AxisZ: " + axisZ.a + " * z + " + axisZ.b + "; ";
		s += "AxisT: " + axisT.a + " * t + " + axisT.b + ";\n";
		return s;
	}

	private void createFields(String text) {
		String words[] = text.trim().split("[,:; \t]");
		Fields[] types = Fields.values();
		fields.clear();
		for (int i = 0; i < words.length; i++) {
			String word = words[i].trim().toLowerCase();
			for (int j = 0; j < types.length; j++) {
				if (word.startsWith(types[j].getFieldDescription())) {
					int in = fields.size() + 1;
					fields.put(in, types[j]);
				}
			}
		}
	}

	private LinearTransformAxis createAxis(String text) {
		LinearTransformAxis axis = new LinearTransformAxis();
		ArrayList<Double> doubles = Tools.extractDoubles(text);
		axis.a = doubles.size() >= 1 ? doubles.get(0) : 1.0;
		axis.b = doubles.size() >= 2 ? doubles.get(1) : 0.0;
		return axis;
	}

	private String getExtension(File f) {
		String ext = "";
		String s = f.getName();
		int i = s.lastIndexOf('.');
		if (i > 0 && i < s.length() - 1)
			ext = s.substring(i + 1).toLowerCase();
		return ext;
	}
}
