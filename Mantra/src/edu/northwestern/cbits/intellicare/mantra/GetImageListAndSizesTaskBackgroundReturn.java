package edu.northwestern.cbits.intellicare.mantra;

import java.util.Map;

/**** Async tasks *****/


public class GetImageListAndSizesTaskBackgroundReturn {
	protected String url;
	public Map<String, Integer> imagesToDownload;
	public GetImageListAndSizesTaskBackgroundReturn(String u, Map<String, Integer> m) {
		url = u;
		imagesToDownload = m;
	}
}