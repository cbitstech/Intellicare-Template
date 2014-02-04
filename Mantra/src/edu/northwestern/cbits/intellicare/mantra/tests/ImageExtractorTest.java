/**
 * 
 */
package edu.northwestern.cbits.intellicare.mantra.tests;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection.Response;

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

	/**
	 * Test method for {@link edu.northwestern.cbits.intellicare.mantra.ImageExtractor#getImage(java.lang.String)}.
	 */
	public void testGetImageIsNotNullForAnonymousSite() {
		// * input params *
		String testMethodName = methodName();
		String   baseUrl = "http://cbits.northwestern.edu/images/"
				,fileName = "cbitsfulllogo.png"
				,url = baseUrl + fileName;
		String 	outputFolder = "C:\\temp\\Intellicare\\",
				outputFileName = fileName.replaceAll("\\.(\\w+)$", "_" + testMethodName + ".$1");
		
		// * execute *
		try {
			// * test *
			log(testMethodName, "Fetching image from: " + url);
			byte[] image = ImageExtractor.getImage(url);
			assertNotNull(image);
			// let's write the image so we can easily view it: http://stackoverflow.com/questions/12465586/how-can-i-download-an-image-using-jsoup
			saveImage(testMethodName, image, outputFolder + outputFileName);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException during HTTP(S) call.");
		}
	}

	public void testGetImageListIsNotNullOrEmpty() throws Exception {
		// * input params *
		String testMethodName = methodName();
		String   baseUrl = "http://cbits.northwestern.edu/"
				,fileName = ""
				,url = baseUrl + fileName;
		String 	outputFolder = "C:\\temp\\Intellicare\\",
				outputFileName = fileName.replaceAll("\\.(\\w+)$", "_" + testMethodName + ".$1");
			
		// * execute *
		try {
			// * test *
			log(testMethodName, "Fetching page from: " + url);
			ArrayList<String> imageUrlList = ImageExtractor.getImageList(url, false);
			assertNotNull(imageUrlList);
			log(testMethodName, "list = " + imageUrlList);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException during HTTP(S) call.");
		}
	}

	
	
//	.replaceAll("([\\w-_\\+\\='\"\\?,\\.%\\&:]+)\\.(\\w+)$", testMethodName + "_" + "$1" + ".$2");

	public void testListAndDownloadImages() throws Exception {
		// * input params *
		String testMethodName = methodName();
		String   baseUrl = "http://cbits.northwestern.edu/"
				,fileName = ""
				,url = baseUrl + fileName;
		String 	outputFolder = "C:\\temp\\Intellicare\\";
		
		// * execute *
		try {
			// * test *
			log(testMethodName, "Fetching page from: " + url);
			ArrayList<String> imageUrlList = ImageExtractor.getImageList(url, false);
			log(testMethodName, "list = " + imageUrlList);
			downloadAndSaveImages(outputFolder, imageUrlList);
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException during HTTP(S) call.");
		}
	}
	
	public void testListAndDownloadImagesFromMultipleUrls() throws Exception {
		String testMethodName = methodName();

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

		String 	baseOutputFolder = "C:\\temp\\Intellicare\\";

		// * execute *
		try {
			// * test *
			for(String u : urls) {
				log(testMethodName, "Fetching page from: " + u);
//				ArrayList<String> imageUrlList = ImageExtractor.getImageList(u, false);
				String outputFolder = baseOutputFolder + extractFQDN(u) + "\\";
				log(testMethodName, "outputFolder = " + outputFolder); // + " ; list = " + imageUrlList);
//				downloadAndSaveImages(outputFolder, imageUrlList);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("IOException during HTTP(S) call.");
		}
	}
	
	public static String extractFQDN(String url) {
		return url.replace("(?i)https{0,1}://(.*)", "$1"); //.replace("^(.*?)/.*$", "$1");
	}
	
	

	/***** test utility functions *****/
	
	private static void log(String fn, String msg) {
		System.out.println("[" + fn + "] " + msg);
	}
	
	private static void saveImage(String testMethodName, byte[] image, String fullFilePath) throws IOException {
		FileOutputStream out = (new FileOutputStream(new java.io.File(fullFilePath)));
		log(testMethodName, "Writing image to: " + fullFilePath);
        out.write(image);           // resultImageResponse.body() is where the image's contents are.
        out.close();
	}
	
	/**
	 * @param testMethodName
	 * @param outputFolder
	 * @param imageUrlList
	 */
	private void downloadAndSaveImages(String outputFolder, ArrayList<String> imageUrlList) {
		String testMethodName = methodName();
		
		// download and write the files
		for(String u : imageUrlList) {
			String outputFileName = u.replaceAll("^.*/(.*)\\??.*$", "$1");
			log(testMethodName, "outputFileName = " + outputFileName + "; u = " + u);
			try{
				saveImage(testMethodName, ImageExtractor.getImage(u), outputFolder + outputFileName);
			} catch(Exception e) {
				log(testMethodName, "ERROR: Couldn't write file at: " + outputFileName + " from URL (" + u + "). Reason: " + e.getMessage());
			}
		}
	}
	
	
	// enables getting the currently-executing method name.
	// src: http://stackoverflow.com/questions/442747/getting-the-name-of-the-current-executing-method/8592871#8592871
    private static final int CLIENT_CODE_STACK_INDEX;
    static {
        // Finds out the index of "this code" in the returned stack trace - funny but it differs in JDK 1.5 and 1.6
        int i = 0;
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            i++;
            if (ste.getClassName().equals(ImageExtractorTest.class.getName())) {
                break;
            }
        }
        CLIENT_CODE_STACK_INDEX = i;
    }
    public static String methodName() {
        return Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX].getMethodName();
    }
}
