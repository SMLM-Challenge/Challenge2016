package smlms.sample;

import smlms.tools.Point3D;

public abstract class Item {

	private String name = "";

	public Item(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public abstract String getInfo();
	public abstract String save();
	public abstract boolean contains(Point3D p);
	public abstract void init(int tolerance);
	
}
