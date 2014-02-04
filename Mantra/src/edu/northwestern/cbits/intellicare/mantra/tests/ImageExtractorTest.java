/**
 * 
 */
package edu.northwestern.cbits.intellicare.mantra.tests;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

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
				,url = baseUrl + fileName;	// Yahoo logo
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
		String   baseUrl = "http://cbits.northwestern.edu/images/"
				,fileName = "cbitsfulllogo.png"
				,url = baseUrl + fileName;	// Yahoo logo
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
