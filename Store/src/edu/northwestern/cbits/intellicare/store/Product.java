package edu.northwestern.cbits.intellicare.store;


public class Product {
	public String name;
	public String description;
	public String icon;
	public String attachesOnAvatarAtLocation;
	public double buyPrice;
	
	public Product() {}
	public Product(String n, String d, String i, String a, double b) {
		name=n; description=d;icon=i;attachesOnAvatarAtLocation=a;buyPrice=b;
	}
}