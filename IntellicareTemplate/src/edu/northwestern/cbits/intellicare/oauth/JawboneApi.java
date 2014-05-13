package edu.northwestern.cbits.intellicare.oauth;

import java.net.URLEncoder;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.model.OAuthConfig;

public class JawboneApi extends DefaultApi20 
{
	public static final String CONSUMER_KEY = "fd6P6eL5d9g";
	public static final String CONSUMER_SECRET = "1a7357e886f0b42c4f2fc611ec91f25454e4c67a";

	public String getAccessTokenEndpoint() 
	{
		return "https://jawbone.com/auth/oauth2/token";
	}

	@SuppressWarnings("deprecation")
	public String getAuthorizationUrl(OAuthConfig arg0) 
	{
		return "https://jawbone.com/auth/oauth2/auth?response_type=code&client_id=" +  JawboneApi.CONSUMER_KEY + "&scope=move_read%20basic_read&redirect_uri=" + URLEncoder.encode("https://tech.cbits.northwestern.edu/oauth/jawbone");
	}
}
