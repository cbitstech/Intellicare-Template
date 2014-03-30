package edu.northwestern.cbits.intellicare.oauth;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.model.OAuthConfig;

public class GitHubApi extends DefaultApi20 
{
	public static final String CONSUMER_KEY = "c523b93bbf14cea549a0";
	public static final String CONSUMER_SECRET = "f58db5af03d2ef3c18c81edca1a983ea148b2f47";

	public String getAccessTokenEndpoint() 
	{
		return "https://github.com/login/oauth/access_token";
	}

	public String getAuthorizationUrl(OAuthConfig arg0) 
	{
		return "https://github.com/login/oauth/authorize?client_id=" +  GitHubApi.CONSUMER_KEY + "&scope=repo:status";
	}

}
