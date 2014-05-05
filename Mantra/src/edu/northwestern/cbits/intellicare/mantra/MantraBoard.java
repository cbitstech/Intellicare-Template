package edu.northwestern.cbits.intellicare.mantra;

public class MantraBoard {

	private long mId;
	private String mMantra;

	public MantraBoard() {
		mId = -1;
		mMantra = "";
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}

	public String getMantra() {
		return mMantra;
	}

	public void setMantra(String mantra) {
		mMantra = mantra;
	}
}
