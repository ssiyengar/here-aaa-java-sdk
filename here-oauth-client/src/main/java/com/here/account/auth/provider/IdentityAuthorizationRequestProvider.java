/*
 * Copyright (c) 2018 HERE Europe B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.here.account.auth.provider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;


import com.here.account.auth.OAuth1ClientCredentialsProvider;
import com.here.account.auth.OAuth2Authorizer;
import com.here.account.http.HttpConstants.ContentTypes;
import com.here.account.http.HttpProvider.HttpRequestAuthorizer;
import com.here.account.identity.bo.IdentityTokenRequest;
import com.here.account.oauth2.AccessTokenRequest;
import com.here.account.oauth2.ClientAuthorizationRequestProvider;
import com.here.account.util.JacksonSerializer;
import com.here.account.util.Serializer;

public class IdentityAuthorizationRequestProvider implements ClientAuthorizationRequestProvider {

    /**
     * The HERE Access Token URL.
     */
    private static final String IDENTITY_SERVICE_TOKEN_ENDPOINT_URL = 
            "file:///var/run/secrets/identity/access-token";
            //"http://identity.here-olp-identity-service-sit.svc.cluster.local:8080/token";
            //"http://192.168.99.100:32744/token";
            //"http://127.0.0.1:8080/token";
    
    /**
     * The Kubernetes Service Access Token file.
     */
    private static final String ACCESS_TOKEN_FILE = "/var/run/secrets/kubernetes.io/serviceaccount/token";

    /**
     * The Identity Service token request file.
     */
    private static final String IDENTITY_TOKEN_REQUEST_FILE = "/var/run/identity/identity.json";
    
    private static final String RUN_AS_ID = "runAsId";
    private static final String NAMESPACE = "namespace";
    private static final String NAME = "name";
    
    private final Serializer serializer;
    private final String tokenUrl;
    
    public IdentityAuthorizationRequestProvider() {
        this(new JacksonSerializer(), IDENTITY_SERVICE_TOKEN_ENDPOINT_URL);
    }
    
    public IdentityAuthorizationRequestProvider(Serializer serializer, String tokenUrl) {
        this.serializer = serializer;
        this.tokenUrl = tokenUrl;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getTokenEndpointUrl() {
        return tokenUrl;
        
    }
    
    protected File getIdentityServiceAccessTokenFile() {
        return new File(ACCESS_TOKEN_FILE);
    }
    
    protected File getIdentityTokenRequestFile() {
        return new File(IDENTITY_TOKEN_REQUEST_FILE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpRequestAuthorizer getClientAuthorizer() {
        AuthorizerAndRequest delegate = getAuthorizerAndRequest();
        return delegate.getAuthorizer();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public AccessTokenRequest getNewAccessTokenRequest() {
        return getAuthorizerAndRequest().getRequest();
    }
    
    protected AuthorizerAndRequest getAuthorizerAndRequest() {
        return new AuthorizerAndRequest(
                getAuthorizer(),
                getRequest());
    }
    
    protected OAuth2Authorizer getAuthorizer() {
        String identityServiceAccessToken = readFullyToString(this.getIdentityServiceAccessTokenFile());
        if (identityServiceAccessToken.length() == 0) {
            throw new RequestProviderException(getClass() + ": zero-length service access token");
        }
        return new OAuth2Authorizer(identityServiceAccessToken);
    }
    
    private class AuthorizerAndRequest {
        private final OAuth2Authorizer authorizer;
        private final IdentityTokenRequest request;
        
        private AuthorizerAndRequest(OAuth2Authorizer authorizer,
                IdentityTokenRequest request) {
            this.authorizer = authorizer;
            this.request = request;
        }

        public OAuth2Authorizer getAuthorizer() {
            return authorizer;
        }

        public IdentityTokenRequest getRequest() {
            return request;
        }
        
    }
    
    private static int MAX_ACCESS_TOKEN_BYTES = 4096;

    protected String readFullyToString(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            byte[] buf = new byte[MAX_ACCESS_TOKEN_BYTES];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int numRead;
            int totalRead = 0;
            while (totalRead < MAX_ACCESS_TOKEN_BYTES && (numRead = inputStream.read(buf)) > 0) {
                totalRead += numRead;
                out.write(buf, 0, numRead);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new RequestProviderException(getClass() + ": trouble reading file " + e, e);
        }
    }
    
    protected Properties getIdentityRequestProperties() throws IOException {
        File file = this.getIdentityTokenRequestFile();
        return OAuth1ClientCredentialsProvider.getPropertiesFromFile(file);
    }

    protected IdentityTokenRequest getRequest() {
        File file = this.getIdentityTokenRequestFile();
        
        try (InputStream inputStream = new FileInputStream(file)) {
            Map<String, Object> identityRequestMap = serializer.jsonToMap(inputStream);
            IdentityTokenRequest identityTokenRequest = new IdentityTokenRequest();
            identityTokenRequest
                .setRunAsId((String) identityRequestMap.get(RUN_AS_ID))
                .setNamespace((String) identityRequestMap.get(NAMESPACE))
                .setName((String) identityRequestMap.get(NAME));
            return identityTokenRequest;
        } catch (IOException e) {
            throw new RequestProviderException(getClass() + ": trouble reading identity token request file " + e, e);
            
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContentTypes getRequestContentType() {
        return ContentTypes.JSON;
    }

}
