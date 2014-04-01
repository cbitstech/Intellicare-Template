package edu.northwestern.cbits.intellicare.oauth;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;

public class FitbitApi extends DefaultApi10a 
{
	public static final String CONSUMER_KEY = "942cc901ff16414a81a599668a1987d6";
	public static final String CONSUMER_SECRET = "8182965179ef4494ba6294ff77602b3c";

	public FitbitApi()
	{
		super();
	}
	
	public String getAccessTokenEndpoint() 
	{
		return "https://api.fitbit.com/oauth/access_token";
	}

	public String getAuthorizationUrl(Token token) 
	{
		return "https://www.fitbit.com/oauth/authenticate?oauth_token=" + token.getToken();
	}

	public String getRequestTokenEndpoint() 
	{
		return "https://api.fitbit.com/oauth/request_token";
	}
}
