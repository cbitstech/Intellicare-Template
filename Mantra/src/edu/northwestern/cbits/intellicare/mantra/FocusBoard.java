package edu.northwestern.cbits.intellicare.mantra;

public class FocusBoard {

	private long mId;
	private String mMantra;

	public FocusBoard() {
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
