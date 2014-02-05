/**
 * 
 */
package edu.northwestern.cbits.intellicare.mantra.tests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;

import edu.northwestern.cbits.intellicare.mantra.ImageExtractor;
import junit.framework.TestCase;

/**
 * @author mohrlab
 *
 */
public class ImageExtractorTest extends TestCase {

	/**
	 * @param name
	 */
	public ImageExtractorTest(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}


	
	/***** test methods *****/

	public void testGetImageIsNotNullForAnonymousSite() {
		// * input params *
		String testMethodName = ImageExtractor.methodName();
		String   baseUrl = "http://cbits.northwestern.edu/images/"
				,fileName = "cbitsfulllogo.png"
				,url = baseUrl + fileName;
		String 	outputFolder = "C:\\temp\\Intellicare\\",
				outputFileName = fileName.replaceAll("\\.(\\w+)$", "_" + testMethodName + ".$1");
		
		// * execute *
		try {
			// * test *
			ImageExtractor.log(testMethodName, "Fetching image from: " + url);
			byte[] image = ImageExtractor.getImage(url);
			assertNotNull(image);
			// let's write the image so we can easily view it: http://stackoverflow.com/questions/12465586/how-can-i-download-an-image-using-jsoup
			ImageExtractor.saveImage(image, outputFolder + outputFileName);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException during HTTP(S) call.");
		}
	}

	
	public void testGetImageListIsNotNullOrEmpty() throws Exception {
		// * input params *
		String testMethodName = ImageExtractor.methodName();
		String   baseUrl = "http://cbits.northwestern.edu/"
				,fileName = ""
				,url = baseUrl + fileName;
		String 	outputFolder = "C:\\temp\\Intellicare\\",
				outputFileName = fileName.replaceAll("\\.(\\w+)$", "_" + testMethodName + ".$1");
			
		// * execute *
		try {
			// * test *
			ImageExtractor.log(testMethodName, "Fetching page from: " + url);
			Set<String> imageUrlList = ImageExtractor.getImageList(url, false);
			assertNotNull(imageUrlList);
			ImageExtractor.log(testMethodName, "list = " + imageUrlList);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException during HTTP(S) call.");
		}
	}

	
	public void testListAndDownloadImages() throws Exception {
		// * input params *
		String testMethodName = ImageExtractor.methodName();
		String   baseUrl = "http://cbits.northwestern.edu/"
				,fileName = ""
				,url = baseUrl + fileName;
		String 	outputFolder = "C:\\temp\\Intellicare\\";
		
		// * execute *
		try {
			// * test *
			ImageExtractor.log(testMethodName, "Fetching page from: " + url);
			Set<String> imageUrlList = ImageExtractor.getImageList(url, false);
			ImageExtractor.log(testMethodName, "list = " + imageUrlList);
			ImageExtractor.downloadAndSaveImages(outputFolder, imageUrlList);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException during HTTP(S) call.");
		}
	}
	
	public void testListAndDownloadImagesFromMultipleUrls() throws Exception {
		String testMethodName = ImageExtractor.methodName();

		ArrayList<String> urls = getUrlTestSet();
		String 	baseOutputFolder = "C:\\temp\\Intellicare\\";

		// * execute *
		try {
			// * test *
			for(String u : urls) {
				ImageExtractor.log(testMethodName, "Fetching page from: " + u);
				try {
					Set<String> imageUrlList = ImageExtractor.getImageList(u, false);
					ImageExtractor.log(testMethodName, "  imageUrlList = " + imageUrlList);
					String outputFolder = baseOutputFolder + ImageExtractor.extractHostName(u) + "\\";
					ImageExtractor.log(testMethodName, "  outputFolder = " + outputFolder);
					ImageExtractor.downloadAndSaveImages(outputFolder, imageUrlList);
				} catch (HttpStatusException e) {
					e.printStackTrace();
					ImageExtractor.log(testMethodName, "HttpStatusException during HTTP(S) call: " + e.getMessage());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception during HTTP(S) call: " + e.getMessage());
		}
	}

	public void testListAndGetFileSizesFromMultipleUrls() throws Exception {
		String testMethodName = ImageExtractor.methodName();

		ArrayList<String> urls = getUrlTestSet();
		String 	baseOutputFolder = "C:\\temp\\Intellicare\\";

		// * execute *
		try {
			// * test *
			System.out.println("\"size\",\"url\"");
			for(String u : urls) {
				ImageExtractor.log(testMethodName, "Fetching page from: " + u);
				try {
					// get the set of image URLs
					Set<String> imageUrlList = ImageExtractor.getImageList(u, false);
					ImageExtractor.log(testMethodName, "  imageUrlList = " + imageUrlList);
					
					// get the sizes for each file specified by each image URL
					Map<String, Integer> hostImagesLength = ImageExtractor.getRemoteContentLength(imageUrlList);
					for(String url : hostImagesLength.keySet()) {
						System.out.println(hostImagesLength.get(url) + ",\"" + url + "\"");
					}
				} catch (HttpStatusException e) {
					e.printStackTrace();
					ImageExtractor.log(testMethodName, "HttpStatusException during HTTP(S) call: " + e.getMessage());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception during HTTP(S) call: " + e.getMessage());
		}
	}

	public void testGetRemoteContentLength() throws Exception {
		String testMethodName = ImageExtractor.methodName();
		String   baseUrl = "http://cbits.northwestern.edu/images/"
				,fileName = "cbitsfulllogo.png"
				,url = baseUrl + fileName;
		int length = ImageExtractor.getRemoteContentLength(url);
		assertTrue("greater at least zero?", length >= 0);
		ImageExtractor.log(testMethodName, "length (in bytes) = " + length);
	}
	
	
	
	/***** setup/teardown and utility methods *****/
	/**
	 * @return
	 */
	private ArrayList<String> getUrlTestSet() {
		ArrayList<String> urls = new ArrayList<String>();
		urls.add("http://www.flickr.com/explore");
		urls.add("http://www.flickr.com/map");
		urls.add("http://www.flickr.com/galleries");
		urls.add("https://www.google.com/search?safe=active&site=&tbm=isch&source=hp&biw=1547&bih=927&q=kittens&oq=kittens&gs_l=img.3..0l10.1111.1821.0.1901.7.7.0.0.0.0.111.483.6j1.7.0....0...1ac.1.34.img..1.6.394.tsrj5JwqEPo");
		urls.add("http://icanhas.cheezburger.com/lolcats");
		urls.add("http://pixabay.com/en/");
		urls.add("http://www.publicdomainpictures.net/");
		urls.add("http://addicted2success.com/quotes/images-56-inspirational-picture-quotes-that-will-motivate-your-mind/");
		urls.add("http://www.pinterest.com/justcoachit/inspiring-pics-quotes/");
		urls.add("http://mashable.com/2012/12/08/inspiring-photos-2012/");
		return urls;
	}
	
	// enables getting the currently-executing method name.
	// src: http://stackoverflow.com/questions/442747/getting-the-name-of-the-current-executing-method/8592871#8592871
    private static final int CLIENT_CODE_STACK_INDEX;
    static {
        // Finds out the index of "this code" in the returned stack trace - funny but it differs in JDK 1.5 and 1.6
        int i = 0;
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            i++;
            if (ste.getClassName().equals(ImageExtractor.class.getName())) {
                break;
            }
        }
        CLIENT_CODE_STACK_INDEX = i;
    }
    public static String methodName() {
        return Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX].getMethodName();
    }
}
