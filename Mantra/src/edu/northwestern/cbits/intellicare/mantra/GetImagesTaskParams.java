package edu.northwestern.cbits.intellicare.mantra;

import java.util.Map;

import edu.northwestern.cbits.intellicare.mantra.activities.ProgressActivity;

import android.app.Activity;
import android.view.View;
import android.widget.ProgressBar;


/**
 * DTO containing image-fetching async task params.
 * @author mohrlab
 *
 */
public class GetImagesTaskParams {
	Map<String,Integer> imagesToDownload;
	ProgressActivity activity;
	ProgressBar progress;
	View progressBarView;

	public GetImagesTaskParams(Map<String, Integer> d, ProgressActivity a, ProgressBar p, View pbv) {
		imagesToDownload = d;
		activity = a;
		progress = p;
		progressBarView = pbv;
	}
}