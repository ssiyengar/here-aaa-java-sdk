package com.here.account.auth.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

import org.ini4j.Ini;

import com.here.account.auth.OAuth1ClientCredentialsProvider;
import com.here.account.http.HttpProvider.HttpRequestAuthorizer;
import com.here.account.oauth2.ClientCredentialsProvider;
import com.here.account.util.OAuthConstants;

public class FromDefaultHereCredentialsIniStream implements ClientCredentialsProvider {
    
    private final String sectionName;
    private final ClientCredentialsProvider delegate;
    
    public FromDefaultHereCredentialsIniStream(InputStream is) {
        this(is, DEFAULT_INI_SECTION_NAME);
    }
        
    public FromDefaultHereCredentialsIniStream(InputStream is, String sectionName) {
        this.sectionName = sectionName;
        this.delegate = getClientCredentialsProvider(is);
    }

    protected ClientCredentialsProvider getClientCredentialsProvider(InputStream is) {
        try {
            Properties properties = getPropertiesFromIni(is, sectionName);
            return FromSystemProperties.getClientCredentialsProviderWithDefaultTokenEndpointUrl(properties);
        } catch (IOException e) {
            throw new RuntimeException("trouble FromFile " + e, e);
        }
    }

    static final String DEFAULT_INI_SECTION_NAME = "default";
    
    static Properties getPropertiesFromIni(InputStream inputStream, String sectionName) throws IOException {
        Ini ini = new Ini();
        try (Reader reader = new InputStreamReader(inputStream, OAuthConstants.UTF_8_CHARSET)) {
            ini.load(reader);
            Ini.Section section = ini.get(sectionName);
            Properties properties = new Properties();
            properties.put(OAuth1ClientCredentialsProvider.FromProperties.TOKEN_ENDPOINT_URL_PROPERTY,
                    section.get(OAuth1ClientCredentialsProvider.FromProperties.TOKEN_ENDPOINT_URL_PROPERTY));
            properties.put(OAuth1ClientCredentialsProvider.FromProperties.ACCESS_KEY_ID_PROPERTY,
                    section.get(OAuth1ClientCredentialsProvider.FromProperties.ACCESS_KEY_ID_PROPERTY));
            properties.put(OAuth1ClientCredentialsProvider.FromProperties.ACCESS_KEY_SECRET_PROPERTY,
                    section.get(OAuth1ClientCredentialsProvider.FromProperties.ACCESS_KEY_SECRET_PROPERTY));
            return properties;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTokenEndpointUrl() {
        return delegate.getTokenEndpointUrl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpRequestAuthorizer getClientAuthorizer() {
        return delegate.getClientAuthorizer();
    }

}
