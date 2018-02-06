package com.here.account.auth.provider;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.here.account.auth.OAuth1ClientCredentialsProvider;

public class FromSystemPropertiesTest {

    private static final String TEST_TOKEN_ENDPOINT_URL_PROPERTY = "here.token.endpoint.url";
    private static final String TEST_DEFAULT_TOKEN_ENDPOINT_URL = "https://account.api.here.com/oauth2/token";
    FromSystemProperties fromSystemProperties;
    
    String expectedTokenEndpointUrl = "expectedTokenEndpointUrl";
    String expectedAccessKeyId = "expectedAccessKeyId";
    String expectedAccessKeySecret = "accessKeySecret";
    
    String tokenEndpointUrl;
    String accessKeyId;
    String accessKeySecret;
    
    @Before
    public void setUp() {
        tokenEndpointUrl = System.getProperty(OAuth1ClientCredentialsProvider.FromProperties.TOKEN_ENDPOINT_URL_PROPERTY);
        accessKeyId = System.getProperty(OAuth1ClientCredentialsProvider.FromProperties.ACCESS_KEY_ID_PROPERTY);
        accessKeySecret = System.getProperty(OAuth1ClientCredentialsProvider.FromProperties.ACCESS_KEY_SECRET_PROPERTY);

        System.setProperty(OAuth1ClientCredentialsProvider.FromProperties.TOKEN_ENDPOINT_URL_PROPERTY, expectedTokenEndpointUrl);
        System.setProperty(OAuth1ClientCredentialsProvider.FromProperties.ACCESS_KEY_ID_PROPERTY, expectedAccessKeyId);
        System.setProperty(OAuth1ClientCredentialsProvider.FromProperties.ACCESS_KEY_SECRET_PROPERTY, expectedAccessKeySecret);
    }
    
    @After
    public void tearDown() {
        // there's no way to undo a System.setProperty(a, b) if a was previously null
        // the best we can do is a ""
        if (null == tokenEndpointUrl) {
            tokenEndpointUrl = "";
        }
        if (null == accessKeyId) {
            accessKeyId = "";
        }
        if (null == accessKeySecret) {
            accessKeySecret = "";
        }
        
        System.setProperty(OAuth1ClientCredentialsProvider.FromProperties.TOKEN_ENDPOINT_URL_PROPERTY, tokenEndpointUrl);
        System.setProperty(OAuth1ClientCredentialsProvider.FromProperties.ACCESS_KEY_ID_PROPERTY, accessKeyId);
        System.setProperty(OAuth1ClientCredentialsProvider.FromProperties.ACCESS_KEY_SECRET_PROPERTY, accessKeySecret);

    }
    
    @Test
    public void test_default_system_tokenEndpointUrl() {
        fromSystemProperties = new FromSystemProperties();
        String actualTokenEndpointUrl = fromSystemProperties.getTokenEndpointUrl();
        String expectedTokenEndpointUrl = System.getProperty(TEST_TOKEN_ENDPOINT_URL_PROPERTY, TEST_DEFAULT_TOKEN_ENDPOINT_URL);
        assertTrue("tokenEndpointUrl expected "+expectedTokenEndpointUrl+", actual "+actualTokenEndpointUrl, 
                (null == expectedTokenEndpointUrl && null == actualTokenEndpointUrl) 
                || (null != expectedTokenEndpointUrl && expectedTokenEndpointUrl.equals(actualTokenEndpointUrl)));
    }

}
