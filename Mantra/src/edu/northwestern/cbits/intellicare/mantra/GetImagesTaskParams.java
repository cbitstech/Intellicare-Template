package edu.northwestern.cbits.intellicare.mantra;

import java.util.Map;

import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;

import edu.northwestern.cbits.intellicare.mantra.activities.SharedUrlActivity;

/**
 * DTO containing image-fetching async task params.
 * @author mohrlab
 *
 */
public class GetImagesTaskParams {
	Map<String,Integer> imagesToDownload;
	Activity activity;
	ProgressBar progress;
	View progressBarView;

	public GetImagesTaskParams(Map<String, Integer> d, Activity activity2, ProgressBar p, View pbv) {
		imagesToDownload = d;
		activity = activity2;
		progress = p;
		progressBarView = pbv;
	}
}