package edu.northwestern.cbits.intellicare.mantra;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import edu.northwestern.cbits.intellicare.mantra.activities.SharedUrlActivity;

/**** Async tasks *****/


class GetImageListAndSizesTaskBackgroundReturn {
	protected String url;
//	protected Map<String, Integer> imageUrlsAndSizes;
	public Map<String, Integer> imagesToDownload;
	public GetImageListAndSizesTaskBackgroundReturn(String u, Map<String, Integer> m) {
		url = u;
//		imageUrlsAndSizes = m;
		imagesToDownload = m;
	}
}

/**
 * Fetches the set of image URLs from webpage at a specified URL.
 * @author mohrlab
 *
 */
public class GetImageListAndSizesTask extends AsyncTask<String, Void, GetImageListAndSizesTaskBackgroundReturn> {
	private static final String CN = "GetImageListAndSizesTask";
	public Activity activity;

	private final ProgressBar progress; 
		
//	public GetImageListAndSizesTask(SharedUrlActivity sua, ProgressBar p) {
	public GetImageListAndSizesTask(Activity sua, ProgressBar p) {
//		public GetImageListAndSizesTask(ProgressBar p) {
		activity = sua;
		progress = p;
	}
		

	@Override
	protected GetImageListAndSizesTaskBackgroundReturn doInBackground(String... arg0) {
		try {
			String url = arg0[0];
			Log.d(CN + ".doInBackground", "entered for url = " + url);
			try {
				// get the set of image URLs, then get their file sizes
				long startTime = System.currentTimeMillis();
				Set<String> imageList = ImageExtractor.getImageList(url, false);
				long imageListTime = System.currentTimeMillis();
				Map<String,Integer> imageUrlsAndSizes = ImageExtractor.getRemoteContentLength(imageList);
				long endTime = System.currentTimeMillis();
				Log.d(CN + ".doInBackground", 
						"exiting; ELAPSED TIME (ms) = " + ((double)endTime - startTime) + 
						", getImageList (ms) = " + ((double)(imageListTime - startTime)) + 
						", getRemoteContentLength (ms) = " + ((double)(endTime - imageListTime))
						);
				
				// heuristically determine the set of images to download 
				Map<String,Integer> imagesToDownload = new HashMap<String, Integer>();
				for(String key : imageUrlsAndSizes.keySet()) {
					int sz = imageUrlsAndSizes.get(key);
					Log.d(CN + ".onPostExecute", "size = " + sz + " for image " + key);
					if(SharedUrlActivity.shouldDownloadImage(sz)) {
						imagesToDownload.put(key, sz);
					}
				}
				
				// process the set of images to download
//				GetImageListAndSizesTaskBackgroundReturn ret = new GetImageListAndSizesTaskBackgroundReturn(url, imageUrlsAndSizes);
				GetImageListAndSizesTaskBackgroundReturn ret = new GetImageListAndSizesTaskBackgroundReturn(url, imagesToDownload);
				return ret;
			}
			catch(SocketTimeoutException e) { displayNetworkExceptionMessage(url, e); }
			catch(UnknownHostException e) { displayNetworkExceptionMessage(url, e); }
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	@Override
	protected void onProgressUpdate(Void... values) {
		super.onProgressUpdate(values);
		for(int i=0;i<values.length;i++) {
			Log.d(CN+".onProgressUpdate", values[i].toString());
		}
//		progress.incrementProgressBy(1);
//		currentProgressBarValue++;
	}

	@Override
	protected void onPostExecute(GetImageListAndSizesTaskBackgroundReturn backgroundRet) {
		// select the set of images to download, using some heuristic function
//		Map<String, Integer> imagesToDownload = new HashMap<String, Integer>();
//		for(String key : backgroundRet.imageUrlsAndSizes.keySet()) {
//			int sz = backgroundRet.imageUrlsAndSizes.get(key);
//			Log.d(CN + ".onPostExecute", "size = " + sz + " for image " + key);
//			if(SharedUrlActivity.shouldDownloadImage(sz)) {
//				imagesToDownload.put(key, sz);
//			}
//		}

		// fetch the selected images from their URLs and save to the temp folder
		new GetImagesTask().execute(
				new GetImagesTaskParams(backgroundRet.imagesToDownload, activity, progress)
			);
	}

	/**
	 * @param url
	 * @param e 
	 */
	private void displayNetworkExceptionMessage(String url, IOException e) {
		e.printStackTrace();
		AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this.activity);
		dlgAlert.setMessage("Network error. Are you connected to the Internet? Error message for URL (" + url + "): " + e.getMessage());
		dlgAlert.setPositiveButton("OK",
			    new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) {
			          //dismiss the dialog  
			        }
			    });
	}
	
}