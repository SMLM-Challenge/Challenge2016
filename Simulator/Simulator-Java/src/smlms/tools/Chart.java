package smlms.tools;

import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ColorProcessor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Chart {

	private XYSeriesCollection data;
	private XYLineAndShapeRenderer render;
	private int count = 0;
	private String absisseName;
	private String valueName;
	private String title;
	private double[] abscisseValue;

	public int nbins;
	public double bin;
	private String legend = "";

	public Chart(String title, String valueName, double abscisseValue[]) {
		this.title = title;
		this.valueName = valueName;
		this.abscisseValue = abscisseValue;
		data = new XYSeriesCollection();
		render = new XYLineAndShapeRenderer();
	}

	public void setLegend(String legend) {
		this.legend = legend;
	}

	public String getLegend() {
		return legend;
	}

	public String getTitle() {
		return title;
	}

	public void setBinningInformation(int nbins, double bin) {
		this.nbins = nbins;
		this.bin = bin;
	}

	public void add(String name, double[] function) {
		XYSeries plot = new XYSeries(name);
		for (int i = 0; i < Math.min(function.length, abscisseValue.length); i++)
			plot.add(abscisseValue[i], function[i]);
		count++;
		data.addSeries(plot);
	}

	private JFreeChart plot() {
		if (count == 1) {
			render.setSeriesPaint(0, Color.black);
			render.setSeriesShape(0, new Rectangle(new Dimension(0, 0)));
		} 
		else if (count == 2) {
			render.setSeriesPaint(0, new Color(255, 10, 10, 200));
			render.setSeriesPaint(1, new Color(10, 255, 10, 200));
			render.setSeriesShape(0, new Rectangle(new Dimension(0, 0)));
			render.setSeriesShape(1, new Rectangle(new Dimension(0, 0)));
			Stroke dashedStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] { 10.0f, 6.0f }, 0.0f);
			render.setSeriesOutlineStroke(0, dashedStroke);
		} 
		else if (count < 9) {
			Stroke dashedStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] { 10.0f, 6.0f }, 0.0f);
			render.setSeriesOutlineStroke(0, dashedStroke);
			for (int i = 0; i < count; i++) {
				Color color = Color.getHSBColor((float) (i) / count, 0.9f, 0.9f);
				Color tc = new Color(color.getGreen(), color.getBlue(), color.getRed(), 150);
				render.setSeriesPaint(i, tc);
				render.setSeriesShape(i, new Rectangle(new Dimension(0, 0)));
			}
		} 
		else {
			for (int i = 0; i < count; i++) {
				float h = (float) ((int) (i / 3) * 3) / count;
				Color color = Color.getHSBColor(h, 0.2f + (i % 4) * 0.2f, 0.1f + (i % 3) * 0.3f);
				Stroke stroke = new BasicStroke(1.0f + (i % 2) * 1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[] { 2.0f, 2.0f }, (i % 3) * 10f);
				render.setSeriesStroke(i, stroke);
				Color tc = new Color(color.getGreen(), color.getBlue(), color.getRed(), 100);
				render.setSeriesPaint(i, tc);
				render.setSeriesShape(i, new Rectangle(new Dimension(0, 0)));
			}
		}
	
		JFreeChart chart = ChartFactory.createXYLineChart(title, "", "", data, PlotOrientation.VERTICAL, true, true, true);
		XYPlot plot = chart.getXYPlot();
		plot.setRangeGridlinePaint(new Color(140, 140, 140));
		plot.setDomainGridlinePaint(new Color(140, 140, 140));

		plot.setRenderer(render);
		plot.setBackgroundPaint(new Color(248, 248, 248));

		ValueAxis range = new NumberAxis();
		range.setAutoRange(true);
		range.setLabel(valueName);
		plot.setRangeAxis(range);

		chart.getLegend().setFrame(new BlockBorder(new Color(220, 220, 220)));
		return chart;
	}

	public void show(String name, int width, int height) {
		JFreeChart chart = plot();
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setSize(width, height);
		chartPanel.setBackground(Color.RED);
		JFrame frame = new JFrame(name);
		frame.add(chartPanel);
		frame.pack();
		frame.setVisible(true);
	}

	public ColorProcessor createImage(int width, int height) {
		JFreeChart chart = plot();
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = img.createGraphics();
		chart.draw(g2, new Rectangle2D.Double(0, 0, width, height));
		g2.dispose();
		ImagePlus imp = new ImagePlus("", img);
		return (ColorProcessor) imp.getProcessor();
	}

	public void savePNG(String filename, int width, int height) {
		ImagePlus imp = new ImagePlus("", createImage(width, height));
		(new FileSaver(imp)).saveAsPng(filename);
	}
/*
	public void savePDF(int width, int height, String filename) {
		JFreeChart chart = plot();
		try {
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
			Document document = new Document(PageSize.A4, 36, 36, 154, 54);
			document.addAuthor("Daniel Sage");
			// document.setPageSize(PageSize.A4);
			PdfWriter writer = PdfWriter.getInstance(document, out);
			document.open();
			document.setPageSize(PageSize.A4);
			PdfContentByte contentByte = writer.getDirectContent();
			PdfTemplate template = contentByte.createTemplate(width, height);
			Graphics2D g = template.createGraphics(width, height, new DefaultFontMapper());
			Rectangle2D rectangle2d = new Rectangle2D.Double(0, 0, width, height);
			chart.draw(g, rectangle2d);
			g.dispose();
			contentByte.addTemplate(template, 1f, 0, 0, 1f, 0, 0);
			document.close();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
*/
	public void saveCVS(String filename, boolean saveHeader, boolean transpose) {
		File file = new File(filename);
		IJ.log("Save into " + filename);
		if (data == null) {
			IJ.log("No data to store " + filename);
			return;
		}
		try {
			BufferedWriter buffer = new BufferedWriter(new FileWriter(file));
			String s = "";
			int n = data.getSeriesCount();
			int m = data.getSeries(0).getItemCount();
			if (transpose) {
				for (int j = 0; j < m; j++) {
					s = "";
					for (int i = 0; i < n; i++)
						s += data.getSeries(i).getY(j).doubleValue() + ",";
					buffer.write(s + "\n");
				}
			} 
			else {
				for (int i = 0; i < n; i++) {
					XYSeries series = data.getSeries(i);
					m = series.getItemCount();
					IJ.log(" >>>>>>>>>>>>>> series " + i + " " + n);
					s = "";
					for (int j = 0; j < m - 1; j++)
						s += series.getY(j).doubleValue() + ",";
					s += series.getY(m - 1).doubleValue();
					buffer.write(s + "\n");
				}
			}
			buffer.close();
		} catch (IOException ex) {
			System.out.println("" + ex);
		}
	}

}
