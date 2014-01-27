package edu.northwestern.cbits.intellicare.store;


public class Product {
	public String name;
	public String description;
	public String icon;
	public String attachesOnAvatarAtLocation;
	public double buyPrice;
	public int userOwnsThis;
	
	public Product() {}
	public Product(String n, String d, String i, String a, double b, int o) {
		name=n; description=d;icon=i;attachesOnAvatarAtLocation=a;buyPrice=b;userOwnsThis=o;
	}
	
	public String toString() {
		return 
			"name = " + name + 
			"; description = " + description + 
			"; icon = " + icon + 
			"; attachesOnAvatarAtLocation = " + attachesOnAvatarAtLocation + 
			"; buyPrice = " + String.valueOf(buyPrice) +
			"; userOwnsThis = " + userOwnsThis;
	}
}