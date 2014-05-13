package edu.northwestern.cbits.intellicare.oauth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import edu.northwestern.cbits.ic_template.R;
import edu.northwestern.cbits.intellicare.logging.LogManager;

public class OAuthWebActivity extends ActionBarActivity
{
	protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.getWindow().requestFeature(Window.FEATURE_PROGRESS);

        this.setContentView(R.layout.layout_web_activity);
    }

	@SuppressLint({ "SetJavaScriptEnabled", "DefaultLocale" })
	protected void onResume()
	{
		super.onResume();
		
        WebView webView = (WebView) this.findViewById(R.id.webview);
        
        Uri uri = this.getIntent().getData();
        
        if (uri != null && uri.getScheme() != null && (uri.getScheme().toLowerCase(Locale.ENGLISH).startsWith("http") || uri.getScheme().toLowerCase(Locale.ENGLISH).startsWith("https")))
        {
        	final OAuthWebActivity me = this;
        	
        	WebSettings settings = webView.getSettings();
        	
        	settings.setJavaScriptEnabled(true);
        	settings.setBuiltInZoomControls(true);

        	webView.setWebChromeClient(new WebChromeClient() 
        	{
        		public void onProgressChanged(WebView view, int progress) 
        		{
        			me.setProgress(progress * 1000);
        		}
        		
        		public void onCloseWindow (WebView window)
        		{
        			me.finish();
        		}
        		
        		public void onReceivedTitle (WebView view, String title)
        		{
        			me.getSupportActionBar().setTitle(title);
        		}
        	});
        	
        	webView.setWebViewClient(new WebViewClient() 
        	{
        		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) 
        		{
        			Toast.makeText(me, description, Toast.LENGTH_LONG).show();
        		}
        		
    			public boolean shouldOverrideUrlLoading (final WebView view, final String url)
        		{
        			boolean oauth = false;
        			
        			if (url.toLowerCase(Locale.getDefault()).startsWith("http://purple.robot.com/oauth"))
        				oauth = true;
        			else if (url.toLowerCase(Locale.getDefault()).startsWith("https://purple.robot.com/oauth"))
        				oauth = true;
        			else if (url.toLowerCase(Locale.getDefault()).startsWith("http://tech.cbits.northwestern.edu/oauth/github?code="))
        			{
        				Runnable r = new Runnable()
        				{
							public void run() 
							{
		        				Uri u = Uri.parse(url);
		        				
		        				final String code = u.getQueryParameter("code");
		        				
		        				final String newUrl = "https://github.com/login/oauth/access_token";

								try 
								{
									AndroidHttpClient androidClient = AndroidHttpClient.newInstance("Intellicare", me);

							        HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

									SchemeRegistry registry = new SchemeRegistry();
									registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

									SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
									registry.register(new Scheme("https", socketFactory, 443));

									HttpParams params = androidClient.getParams();
									HttpConnectionParams.setConnectionTimeout(params, 180000);
									HttpConnectionParams.setSoTimeout(params, 180000);

									SingleClientConnManager mgr = new SingleClientConnManager(params, registry);
									HttpClient httpClient = new DefaultHttpClient(mgr, params);

									HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
									
									HttpPost httpPost = new HttpPost(newUrl);

									List<NameValuePair> pairs = new ArrayList<NameValuePair>();
									pairs.add(new BasicNameValuePair("client_id", GitHubApi.CONSUMER_KEY));
									pairs.add(new BasicNameValuePair("client_secret", GitHubApi.CONSUMER_SECRET));
									pairs.add(new BasicNameValuePair("code", code));
									HttpEntity entity = new UrlEncodedFormEntity(pairs, HTTP.US_ASCII);

									httpPost.setEntity(entity);

									HttpResponse response = httpClient.execute(httpPost);

									HttpEntity httpEntity = response.getEntity();

									String result = EntityUtils.toString(httpEntity);
									
									androidClient.close();
									
									String redirectUri = "http://tech.cbits.northwestern.edu/oauth/github?" + result;
									
			        				Intent intent = new Intent(me, OAuthActivity.class);
			        				intent.setData(Uri.parse(redirectUri));
			        				intent.putExtras(new Bundle());
			        				
			        				me.startActivity(intent);
			        				
			        				me.finish();
								} 
								catch (ParseException e) 
								{
									e.printStackTrace();
								}
								catch (IOException e) 
								{
									e.printStackTrace();
								}
							}
        				};
        				
        				Thread t = new Thread(r);
        				t.start();
        				
        				return true;
        			}
        			else if (url.toLowerCase(Locale.getDefault()).startsWith("https://tech.cbits.northwestern.edu/oauth/jawbone?code="))
        			{
        				Runnable r = new Runnable()
        				{
							public void run() 
							{
		        				Uri u = Uri.parse(url);
		        				
		        				final String code = u.getQueryParameter("code");
		        				
		        				final String newUrl = "https://jawbone.com/auth/oauth2/token";

								try 
								{
									AndroidHttpClient androidClient = AndroidHttpClient.newInstance("Intellicare", me);

							        HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

									SchemeRegistry registry = new SchemeRegistry();
									registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

									SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory();
									registry.register(new Scheme("https", socketFactory, 443));

									HttpParams params = androidClient.getParams();
									HttpConnectionParams.setConnectionTimeout(params, 180000);
									HttpConnectionParams.setSoTimeout(params, 180000);

									SingleClientConnManager mgr = new SingleClientConnManager(params, registry);
									HttpClient httpClient = new DefaultHttpClient(mgr, params);

									HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
									
									HttpPost httpPost = new HttpPost(newUrl);

									List<NameValuePair> pairs = new ArrayList<NameValuePair>();
									pairs.add(new BasicNameValuePair("client_id", JawboneApi.CONSUMER_KEY));
									pairs.add(new BasicNameValuePair("client_secret", JawboneApi.CONSUMER_SECRET));
									pairs.add(new BasicNameValuePair("grant_type", "authorization_code"));
									pairs.add(new BasicNameValuePair("code", code));
									HttpEntity entity = new UrlEncodedFormEntity(pairs, HTTP.US_ASCII);

									httpPost.setEntity(entity);

									HttpResponse response = httpClient.execute(httpPost);

									HttpEntity httpEntity = response.getEntity();

									String result = EntityUtils.toString(httpEntity);
									
									String accessToken = "";
									
									try 
									{
										JSONObject resultObj = new JSONObject(result);
										
										accessToken = resultObj.getString("access_token");
									} 
									catch (JSONException e) 
									{
										LogManager.getInstance(me).logException(e);
									}
									
									androidClient.close();
									
									String redirectUri = "http://tech.cbits.northwestern.edu/oauth/jawbone?code=" + accessToken;
									
			        				Intent intent = new Intent(me, OAuthActivity.class);
			        				intent.setData(Uri.parse(redirectUri));
			        				intent.putExtras(new Bundle());
			        				
			        				me.startActivity(intent);
			        				
			        				me.finish();
								} 
								catch (ParseException e) 
								{
									e.printStackTrace();
								}
								catch (IOException e) 
								{
									e.printStackTrace();
								}
							}
        				};
        				
        				Thread t = new Thread(r);
        				t.start();
        				
        				return true;
						
        			}
        			else if (url.toLowerCase(Locale.getDefault()).startsWith("http://tech.cbits.northwestern.edu/oauth"))
        				oauth = true;
        			else if (url.toLowerCase(Locale.getDefault()).startsWith("https://tech.cbits.northwestern.edu/oauth"))
        				oauth = true;
        			else if (url.toLowerCase(Locale.getDefault()).startsWith("http://pr-oauth/oauth"))
        				oauth = true;
        			
        			if (oauth)
        			{
        				Intent intent = new Intent(me, OAuthActivity.class);
        				intent.setData(Uri.parse(url));
        				intent.putExtras(new Bundle());
        				
        				me.startActivity(intent);
        				
        				me.finish();
        			}
        			
        			return oauth;
        		}
        	});

            webView.loadUrl(uri.toString());
        }
        else
			Toast.makeText(this, R.string.error_missing_uri, Toast.LENGTH_LONG).show();
	}
	
	public void onBackPressed ()
	{
        WebView webView = (WebView) this.findViewById(R.id.webview);
        
        if (webView.canGoBack())
        	webView.goBack();
        else
        	super.onBackPressed();
	}
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
        MenuInflater inflater = this.getMenuInflater();
        inflater.inflate(R.menu.menu_oauth_web_activity, menu);

        return true;
	}

	public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.menu_close)
        	this.finish();

    	return true;
    }
}
