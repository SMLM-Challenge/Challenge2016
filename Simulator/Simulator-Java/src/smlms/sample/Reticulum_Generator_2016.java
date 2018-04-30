package smlms.sample;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.StackWindow;
import ij.process.FloatProcessor;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import smlms.tools.NormalizedVariable;
import smlms.tools.Point3D;
import smlms.tools.PsRandom;

public class Reticulum_Generator_2016 {

	private PsRandom rand = new PsRandom(123);
	
	private int 	margin = 0;
	private int 	nx = 640 + margin + margin; 	// 320 * 20 = 6400nm
	private int 	ny = 640 + margin + margin; 	// 320 * 20 = 6400nm
	private int 	nz = 150; 						// 25 * 20 = 1500nm
	private double 	pixelsize = 10;

	private double 	jitterExtremityAngleRadian = 0; //0.02;
	private int 	nbExtremity = 12;
	private double 	radiusTube = 150;
	private double 	thickTube = 6;

	private String outfile = "/Users/sage/Desktop/SampleER0.txt";
	
	public static void main(String args[]) {
		new Reticulum_Generator_2016();
	}

	public Reticulum_Generator_2016() {
		System.out.println("Endoplasmic Reticulum");
	
		ImagePlus imp = new ImagePlus("background", new FloatProcessor(640, 640));
		imp.show();

		Point3D extremities[] = createExtremity();
		
		ArrayList<ArrayList<Point3D>> lists = build();
	
		IJ.log("Number of tubes: " + lists.size());
		new MTCanvas(imp, lists, nz,  extremities);
		
		Sample sample = new Sample("name", (int)((nx-2*margin)*pixelsize), (int)((ny-2*margin)*pixelsize), (int)((nz)*pixelsize));
		for(int i=0; i<lists.size(); i++) {
			NormalizedVariable radius = new NormalizedVariable(radiusTube);
			NormalizedVariable thickness = new NormalizedVariable(thickTube);
			ArrayList<Point3D> nodes = new ArrayList<Point3D>();
			ArrayList<Point3D> list = lists.get(i);
			for(int k=0; k<list.size(); k++) {
				Point3D p = list.get(k);
				nodes.add(new Point3D((p.x-margin)*pixelsize, (p.y-margin)*pixelsize, p.z*pixelsize));
			}
			Tube tube = new Tube("tube " + i, nodes, radius, thickness, 1);
			IJ.log(" Radius 0.0: " + radius.get(0.0));
			IJ.log(" Radius 0.5: " + radius.get(0.5));
			IJ.log(" Radius 1.0: " + radius.get(1.0));
			sample.add(tube);
		}
		sample.save(outfile);
	}

	private ArrayList<ArrayList<Point3D>> build() {

		ArrayList<Point3D> my1 = new ArrayList<Point3D>();
		my1.add(new Point3D(	-20	,	266.5	,	700	));
		my1.add(new Point3D(	44	,	268.5	,	147	));
		my1.add(new Point3D(	74	,	262.5	,	148	));
		my1.add(new Point3D(	104	,	260.5	,	149	));
		my1.add(new Point3D(	128	,	266.5	,	150	));
		my1.add(new Point3D(	144	,	287.5	,	151	));
		my1.add(new Point3D(	164	,	298.5	,	152	));
		my1.add(new Point3D(	190	,	302.5	,	153	));
		my1.add(new Point3D(	216	,	313.5	,	154	));
		my1.add(new Point3D(	246	,	327.5	,	155	));
		my1.add(new Point3D(	271	,	329.5	,	156	));
		my1.add(new Point3D(	299	,	325.5	,	157	));
		my1.add(new Point3D(	320	,	305.5	,	158	));
		my1.add(new Point3D(	343	,	290.5	,	159	));
		my1.add(new Point3D(	367	,	286.5	,	160	));
		my1.add(new Point3D(	379	,	259.5	,	161	));
		my1.add(new Point3D(	378	,	229.5	,	162	));
		my1.add(new Point3D(	385	,	205.5	,	163	));
		my1.add(new Point3D(	404	,	195.5	,	164	));
		my1.add(new Point3D(	445	,	202.5	,	165	));
		my1.add(new Point3D(	469	,	224.5	,	166	));
		my1.add(new Point3D(	490	,	242.5	,	167	));
		my1.add(new Point3D(	509	,	268.5	,	168	));
		my1.add(new Point3D(	533	,	272.5	,	169	));
		my1.add(new Point3D(	564	,	275.5	,	170	));
		my1.add(new Point3D(	581	,	254.5	,	171	));
		my1.add(new Point3D(	603	,	225.5	,	172	));
		my1.add(new Point3D(	618	,	200.5	,	173	));
		my1.add(new Point3D(	650	,	187.5	,	174	));
								
		ArrayList<Point3D> my2 = new ArrayList<Point3D>();
		my2.add(new Point3D(	35	,	401.5	,	176	));
		my2.add(new Point3D(	73	,	409.5	,	177	));
		my2.add(new Point3D(	97	,	403.5	,	178	));
		my2.add(new Point3D(	120	,	383.5	,	179	));
		my2.add(new Point3D(	138	,	363.5	,	180	));
		my2.add(new Point3D(	151	,	327.5	,	181	));
		my2.add(new Point3D(	170	,	292.5	,	182	));
		my2.add(new Point3D(	181	,	258.5	,	183	));
		my2.add(new Point3D(	195	,	215.5	,	184	));
		my2.add(new Point3D(	209	,	178.5	,	185	));
		my2.add(new Point3D(	221	,	150.5	,	186	));
		my2.add(new Point3D(	247	,	134.5	,	187	));
		my2.add(new Point3D(	280	,	129.5	,	188	));
		my2.add(new Point3D(	317	,	136.5	,	189	));
		my2.add(new Point3D(	357	,	140.5	,	190	));
		my2.add(new Point3D(	392	,	157.5	,	191	));
		my2.add(new Point3D(	413	,	185.5	,	192	));
		my2.add(new Point3D(	432	,	221.5	,	193	));
		my2.add(new Point3D(	423	,	261.5	,	194	));
								
		ArrayList<Point3D> my3 = new ArrayList<Point3D>();
		my3.add(new Point3D(	149	,	124.5	,	196	));
		my3.add(new Point3D(	186	,	155.5	,	197	));
		my3.add(new Point3D(	202	,	166.5	,	198	));
		my3.add(new Point3D(	225	,	188.5	,	199	));
		my3.add(new Point3D(	239	,	220.5	,	200	));
		my3.add(new Point3D(	247	,	247.5	,	201	));
		my3.add(new Point3D(	238	,	270.5	,	202	));
		my3.add(new Point3D(	211	,	285.5	,	203	));
		my3.add(new Point3D(	190	,	281.5	,	204	));
		my3.add(new Point3D(	169	,	269.5	,	205	));
		my3.add(new Point3D(	146	,	252.5	,	206	));
		my3.add(new Point3D(	125	,	254.5	,	207	));
		my3.add(new Point3D(	101	,	270.5	,	208	));
		my3.add(new Point3D(	80	,	299.5	,	209	));
		my3.add(new Point3D(	57	,	338.5	,	210	));
		my3.add(new Point3D(	55	,	373.5	,	211	));
		my3.add(new Point3D(	55	,	417.5	,	212	));
		my3.add(new Point3D(	59	,	454.5	,	213	));
		my3.add(new Point3D(	64	,	487.5	,	214	));
		my3.add(new Point3D(	73	,	518.5	,	215	));
								
		ArrayList<Point3D> my4 = new ArrayList<Point3D>();
		my4.add(new Point3D(	142	,	-20	,	217	));
		my4.add(new Point3D(	148	,	31.5	,	218	));
		my4.add(new Point3D(	176	,	54.5	,	219	));
		my4.add(new Point3D(	207	,	85.5	,	220	));
		my4.add(new Point3D(	239	,	104.5	,	221	));
		my4.add(new Point3D(	261	,	122.5	,	222	));
		my4.add(new Point3D(	279	,	153.5	,	223	));
		my4.add(new Point3D(	296	,	184.5	,	224	));
		my4.add(new Point3D(	307	,	217.5	,	225	));
		my4.add(new Point3D(	325	,	233.5	,	226	));
		my4.add(new Point3D(	343	,	258.5	,	227	));
		my4.add(new Point3D(	334	,	292.5	,	228	));
		my4.add(new Point3D(	347	,	335.5	,	229	));
		my4.add(new Point3D(	364	,	365.5	,	230	));
		my4.add(new Point3D(	384	,	389.5	,	231	));
		my4.add(new Point3D(	416	,	404.5	,	232	));
		my4.add(new Point3D(	459	,	407.5	,	233	));
		my4.add(new Point3D(	495	,	409.5	,	234	));
		my4.add(new Point3D(	534	,	408.5	,	235	));
		my4.add(new Point3D(	564	,	406.5	,	236	));
		my4.add(new Point3D(	601	,	413.5	,	237	));
		my4.add(new Point3D(	650	,	432.5	,	238	));
								
		ArrayList<Point3D> my5 = new ArrayList<Point3D>();
		my5.add(new Point3D(	562	,	650	,	241	));
		my5.add(new Point3D(	547	,	605.5	,	242	));
		my5.add(new Point3D(	545	,	569.5	,	243	));
		my5.add(new Point3D(	538	,	524.5	,	244	));
		my5.add(new Point3D(	534	,	485.5	,	245	));
		my5.add(new Point3D(	528	,	449.5	,	246	));
		my5.add(new Point3D(	520	,	413.5	,	247	));
		my5.add(new Point3D(	498	,	381.5	,	248	));
		my5.add(new Point3D(	480	,	346.5	,	249	));
		my5.add(new Point3D(	445	,	330.5	,	250	));
		my5.add(new Point3D(	425	,	313.5	,	251	));
		my5.add(new Point3D(	419	,	287.5	,	252	));
		my5.add(new Point3D(	410	,	261.5	,	253	));
		my5.add(new Point3D(	395	,	250.5	,	254	));
		my5.add(new Point3D(	367	,	240.5	,	255	));
		my5.add(new Point3D(	355	,	242.5	,	256	));
								
		ArrayList<Point3D> my6= new ArrayList<Point3D>();
		my6.add(new Point3D(	-20	,	327.5	,	258	));
		my6.add(new Point3D(	28	,	322.5	,	259	));
		my6.add(new Point3D(	56	,	322.5	,	260	));
		my6.add(new Point3D(	97	,	325.5	,	261	));
		my6.add(new Point3D(	131	,	330.5	,	262	));
		my6.add(new Point3D(	156	,	338.5	,	263	));
		my6.add(new Point3D(	182	,	352.5	,	264	));
		my6.add(new Point3D(	204	,	376.5	,	265	));
		my6.add(new Point3D(	218	,	402.5	,	266	));
		my6.add(new Point3D(	250	,	414.5	,	267	));
		my6.add(new Point3D(	276	,	424.5	,	268	));
		my6.add(new Point3D(	297	,	425.5	,	269	));
		my6.add(new Point3D(	330	,	419.5	,	270	));
		my6.add(new Point3D(	351	,	412.5	,	271	));
		my6.add(new Point3D(	373	,	390.5	,	272	));
		my6.add(new Point3D(	392	,	371.5	,	273	));
		my6.add(new Point3D(	412	,	338.5	,	274	));
		my6.add(new Point3D(	431	,	313.5	,	275	));
		my6.add(new Point3D(	453	,	291.5	,	276	));
		my6.add(new Point3D(	478	,	258.5	,	277	));
		my6.add(new Point3D(	491	,	233.5	,	278	));
		my6.add(new Point3D(	507	,	209.5	,	279	));
		my6.add(new Point3D(	519	,	195.5	,	280	));
		my6.add(new Point3D(	540	,	181.5	,	281	));
		
		ArrayList<ArrayList<Point3D>> lists = new ArrayList<ArrayList<Point3D>>();
		lists.add(my1);
		lists.add(my2);
		lists.add(my3);
		lists.add(my4);
		lists.add(my5);
		lists.add(my6);
		
		for ( ArrayList<Point3D> list : lists)
		for (Point3D p : list) {
			double t = p.x; p.x = p.y; p.y = t; 
		}
		
		IJ.log(" \n");
		int er = 0;
		NormalizedVariable[] z = new NormalizedVariable[6];
		double n[] = new double[6];
		// 1
		n[er] = lists.get(er).size();
		z[er] = new NormalizedVariable(68);
		z[er].addLinear(00, -32);
		z[er].addCosine(3, 2.5, 0.3);
		z[er].addUniformRandom(0, 3);
		for (int k=0; k<n[er]; k++) { // 1
			lists.get(er).get(k).z = z[er].get(k/(n[er]-1));
			IJ.log(" ER" + (er+1) + " " + lists.get(er).get(k).z);
		}

		IJ.log(" \n");
		er++;
		// 2
		n[er] = lists.get(er).size();
		z[er] = new NormalizedVariable(0);
		z[er].addLinear(170, -50);
		z[er].addCosine(5, 0.3, 1.57);
		z[er].addUniformRandom(0, 3);
		for (int k=0; k<n[er]; k++) { // 1
			lists.get(er).get(k).z = z[er].get(k/(n[er]-1));
			IJ.log(" ER" + (er+1) + " " + lists.get(er).get(k).z);
		}

		IJ.log(" \n");
		er++;
		// 3
		n[er] = lists.get(er).size();
		z[er] = new NormalizedVariable(18);
		z[er].addCosine(19, 1.1, 1.57);
		z[er].addUniformRandom(0, 3);
		for (int k=0; k<n[er]; k++) { // 1
			lists.get(er).get(k).z = z[er].get(k/(n[er]-1));
			IJ.log(" ER" + (er+1) + " " + lists.get(er).get(k).z);
		}
		
		IJ.log(" \n");
		er++;
		// 4
		n[er] = lists.get(er).size();
		z[er] = new NormalizedVariable(0);
		z[er].addLinear(95, 50);
		z[er].addCosine(2, 2.1, 1.57);
		z[er].addUniformRandom(0, 3);
		for (int k=0; k<n[er]; k++) { // 1
			lists.get(er).get(k).z = z[er].get(k/(n[er]-1));
			IJ.log(" ER" + (er+1) + " " + lists.get(er).get(k).z);
		}
		
		IJ.log(" \n");
		er++;
		// 4
		n[er] = lists.get(er).size();
		z[er] = new NormalizedVariable(0);
		z[er].addLinear(90, 160);
		z[er].addCosine(2, 3.1, 1.57);
		z[er].addUniformRandom(0, 3);
		for (int k=0; k<n[er]; k++) { // 1
			lists.get(er).get(k).z = z[er].get(k/(n[er]-1));
			IJ.log(" ER" + (er+1) + " " + lists.get(er).get(k).z);
		}
	
		IJ.log(" \n");
		er++;
		// 6
		n[er] = lists.get(er).size();
		z[er] = new NormalizedVariable(0);
		z[er].addLinear(100, -1);
		z[er].addCosine(10, 2.1, 1.57);
		z[er].addUniformRandom(0, 3);
		for (int k=0; k<n[er]; k++) { // 1
			lists.get(er).get(k).z = z[er].get(k/(n[er]-1));
			IJ.log(" ER" + (er+1) + " " + lists.get(er).get(k).z);
		}


		return lists;
	}
	
	

	private Point3D[] createExtremity() {
		int xc = nx / 2;
		int yc = ny / 2;
		Point3D extremities[] = new Point3D[nbExtremity];
		for (int a = 0; a < nbExtremity; a++) {
			double r = a * 2 * Math.PI / nbExtremity + rand.nextDouble(-jitterExtremityAngleRadian, jitterExtremityAngleRadian);
			double xp = xc + nx * Math.cos(r)* 0.49;
			double yp = yc + ny * Math.sin(r) * 0.49;
			double zp = nz * 0.5 + nz*rand.nextDouble(-0.4, 0.4);
			extremities[a] = new Point3D(xp, yp, zp);
			IJ.log("Extremity " + extremities[a].x + " " + extremities[a].y + " " + extremities[a].z);
		}
		return extremities;
	}
	
	
	public class MTCanvas extends ImageCanvas {
		private ArrayList<ArrayList<Point3D>> tubes;
		private int nz;
		private Point3D extremities[];
		
		public MTCanvas(ImagePlus imp, ArrayList<ArrayList<Point3D>> tubes, int nz, Point3D extremities[]) {
			super(imp);
			this.tubes = tubes;
			this.extremities = extremities;
			this.nz = nz;
			if (imp.getStackSize() > 1)
				imp.setWindow(new StackWindow(imp, this));
			else
				imp.setWindow(new ImageWindow(imp, this));
		}
	
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			for(Point3D extremity : extremities) {
				g.setColor(Color.WHITE);
				g.fillOval(screenXD(extremity.x)-3, screenYD(extremity.y)-3, 7, 7);
				
			}
			
			for(ArrayList<Point3D> tube : tubes) {
				Color c = Color.getHSBColor((float)(tube.get(0).z/nz), 1, 1);
				g.setColor(c);
				g.fillOval(screenXD(tube.get(0).x)-3, screenYD(tube.get(0).y)-3, 7, 7);
				
				for(int p=1; p<tube.size(); p++) {
					Point3D prev = tube.get(p-1);
					Point3D curr = tube.get(p);
					g.drawLine(screenXD(prev.x), screenYD(prev.y), screenXD(curr.x), screenYD(curr.y));
				}
			}
		}
	}
}
