package edu.northwestern.cbits.intellicare.mantra;

import java.util.Map;

import edu.northwestern.cbits.intellicare.mantra.activities.SharedUrlActivity;

public class GetImagesTaskParams {
	Map<String,Integer> imagesToDownload;
	SharedUrlActivity activity;
	public GetImagesTaskParams(Map<String, Integer> d, SharedUrlActivity a) { imagesToDownload = d; activity = a; }
}