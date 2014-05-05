package edu.northwestern.cbits.intellicare.socialforce;

public class ContactRecord implements Comparable<ContactRecord>
{
	public int count = 1;
	public String number = null;
	public String name = null;
	public int level = -1;
	public String key = null;

	public int compareTo(ContactRecord other) 
	{
		if (this.count > other.count)
			return -1;
		else if (this.count < other.count)
			return 1;
				
		if (this.name != null)
			return this.name.compareTo(other.name);
		
		return this.number.compareTo(other.number);
	}
}