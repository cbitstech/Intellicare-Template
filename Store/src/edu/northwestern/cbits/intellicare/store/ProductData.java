package edu.northwestern.cbits.intellicare.store;

public class ProductData {
	
	public static Product[] getAllProducts() {
		return new Product[]{
			new Product("Alpha","A hat","","headTop",22),
			new Product("Beta","B shoulder pad","","shouldersBoth",45),
			new Product("Charlie","C armor","","torsoFront",300),
			new Product("Delta","D pistol","","thighRight",450),
			new Product("Epsilon","E boots","","feet",32)
		};
	}
}
