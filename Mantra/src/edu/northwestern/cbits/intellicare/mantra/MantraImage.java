package edu.northwestern.cbits.intellicare.mantra;

public class MantraImage {

	private long mId;
	private long mFocusBoardId;
	private String mPath;
	private String mCaption;

	public MantraImage() {
		mId = -1;
	}

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}

	public long getFocusBoardId() {
		return mFocusBoardId;
	}

	public void setFocusBoardId(long focusBoardId) {
		mFocusBoardId = focusBoardId;
	}

	public String getPath() {
		return mPath;
	}

	public void setPath(String path) {
		mPath = path;
	}
	
	public String getCaption() {
		return mCaption;
	}
	
	public void setCaption(String c) {
		mCaption = c;
	}
}
