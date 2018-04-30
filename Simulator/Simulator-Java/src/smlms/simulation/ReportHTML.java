package smlms.simulation;

import ij.IJ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class ReportHTML extends PrintStream {

	public ReportHTML(String path, String filename) throws FileNotFoundException {
		super(new FileOutputStream(path+File.separator+filename));
	}

	public void printTitle(String name) {
		print("<p class=\"report_title\" style=\"clear:both\">" + name + "</p>\n");
	}
	
	public void printSection(String name) {
		print("<p class=\"report_section\" style=\"clear:both\">" + name + "</p>\n");
	}

	public void printValue(String value) {
		print("<td class=\"report_value\">" + value + "</td>");
	}

	public void printTH(String name) {
		print("<td class=\"report_header\">" + name + "</td>\n");
	}

	public void printTD(String name) {
		print("<td valign=\"top\" class=\"report_param\">" + name + "</td>\n");
	}

	public void printTD(String name, boolean left) {
		if (left)
			print("<td valign=\"top\" class=\"report_param_lvert\">" + name + "</td>\n");
		else
			print("<td valign=\"top\" class=\"report_param_rvert\">" + name + "</td>\n");
	}

	public void printHeader(String name, int span) {
		print("<tr><td colspan=\"" + span + "\" class=\"report_header\">" + name + "</td></tr>\n");
	}

	public void printParam(String name, double value, int digit, String unit) {
		print("<tr><td class=\"report_param\">" + name + "</td><td class=\"report_value\">" + IJ.d2s(value,digit) + "</td><td class=\"report_unit\">" + unit + "</td></tr>\n");
	}

	public void printParam(String name, String value, String unit) {
		print("<tr><td class=\"report_param\">" + name + "</td><td class=\"report_value\">" + value + "</td><td class=\"report_unit\">" + unit + "</td></tr>\n");
	}

	public void printField(String name, double nx, double ny, int digit, String unit) {
		print("<tr><td class=\"report_param\">" + name + "</td><td class=\"report_value\">" + IJ.d2s(nx,digit) + "x" + IJ.d2s(ny,digit) + "</td><td class=\"report_unit\">" + unit + "</td></tr>\n");
	}

	public void printFile(String filename) {
		if (!(new File(filename)).exists())
			return;
		try {
			String content = "";
			BufferedReader file = new BufferedReader(new FileReader(filename));
			String line;
			while((line = file.readLine()) != null) {
				content += line + "\n";
			}	
			file.close();
			print(content);
		}
		catch(IOException ex) {
			IJ.error("printFile: " + ex);
		}
	}
}
