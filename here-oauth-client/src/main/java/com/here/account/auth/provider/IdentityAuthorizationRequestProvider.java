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
import java.util.Objects;

import com.here.account.auth.NoAuthorizer;
import com.here.account.auth.OAuth2Authorizer;
import com.here.account.http.HttpConstants.ContentTypes;
import com.here.account.http.HttpProvider.HttpRequestAuthorizer;
import com.here.account.identity.bo.IdentityTokenRequest;
import com.here.account.oauth2.AccessTokenRequest;
import com.here.account.oauth2.ClientAuthorizationRequestProvider;
import com.here.account.util.JacksonSerializer;
import com.here.account.util.ReadUtil;
import com.here.account.util.Serializer;

public class IdentityAuthorizationRequestProvider implements ClientAuthorizationRequestProvider {

    /**
     * The HERE Access Token URL.
     */
    private static final String IDENTITY_SERVICE_TOKEN_ENDPOINT_URL = 
            "http://identity.here-olp-identity-service-sit.svc.cluster.local:8080/token";
    
    /**
     * The Kubernetes Service Account Token file.
     */
    private static final String SERVICE_ACCOUNT_TOKEN_FILE_PATH = "/var/run/secrets/kubernetes.io/serviceaccount/token";

    /**
     * The Identity Service token request file.
     */
    private static final String IDENTITY_TOKEN_REQUEST_FILE_PATH = "/var/run/identity/identity.json";
    
    /**
     * The Pod Name file.
     */
    private static final String POD_NAME_FILE_PATH = "/var/run/podinfo/name";
    
    private static final String RUN_AS_ID = "runAsId";
    private static final String NAMESPACE = "namespace";
    private static final String RUN_AS_ID_NAME = "runAsIdName";
    
    private final Serializer serializer;
    private final File serviceAccountTokenFile;
    private final File identityFile;
    private final File podNameFile;
    private final String tokenUrl;
    
    public IdentityAuthorizationRequestProvider() {
        this(new JacksonSerializer(), 
                new File(SERVICE_ACCOUNT_TOKEN_FILE_PATH),
                new File(IDENTITY_TOKEN_REQUEST_FILE_PATH),
                new File(POD_NAME_FILE_PATH),
                IDENTITY_SERVICE_TOKEN_ENDPOINT_URL);
    }
    
    public IdentityAuthorizationRequestProvider(Serializer serializer, 
            File serviceAccountTokenFile,
            File identityFile,
            File podNameFile,
            String tokenUrl) {
        // serviceAccessTokenFile can be null
        Objects.requireNonNull(serializer, "serializer is required");
        Objects.requireNonNull(identityFile, "identityFile is required");
        Objects.requireNonNull(podNameFile, "podNameFile is required");
        Objects.requireNonNull(tokenUrl, "tokenUrl is required");

        this.serializer = serializer;
        this.serviceAccountTokenFile = serviceAccountTokenFile;
        this.identityFile = identityFile;
        this.podNameFile = podNameFile;
        this.tokenUrl = tokenUrl;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getTokenEndpointUrl() {
        return tokenUrl;
        
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
    
    protected HttpRequestAuthorizer getAuthorizer() {
        if (null == serviceAccountTokenFile) {
            return new NoAuthorizer();
        }
        String identityServiceAccessToken = readFullyToString(this.serviceAccountTokenFile);
        if (identityServiceAccessToken.length() == 0) {
            throw new RequestProviderException(getClass() + ": zero-length service access token");
        }
        return new OAuth2Authorizer(identityServiceAccessToken);
    }
    
    private class AuthorizerAndRequest {
        private final HttpRequestAuthorizer authorizer;
        private final IdentityTokenRequest request;
        
        private AuthorizerAndRequest(HttpRequestAuthorizer authorizer,
                IdentityTokenRequest request) {
            this.authorizer = authorizer;
            this.request = request;
        }

        public HttpRequestAuthorizer getAuthorizer() {
            return authorizer;
        }

        public IdentityTokenRequest getRequest() {
            return request;
        }
        
    }
    
    protected String readFullyToString(File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            byte[] bytes = ReadUtil.readUpTo16KBytes(inputStream);
            return new String(bytes, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            throw new RequestProviderException(getClass() + ": trouble reading file " + e, e);
        }
    }
    
    protected IdentityTokenRequest getRequest() {
        File identityFile = this.identityFile;
        File podNameFile = this.podNameFile;

        String podName = readFullyToString(podNameFile);

        try (InputStream identityInputStream = new FileInputStream(identityFile)) {
            Map<String, Object> identityMap = serializer.jsonToMap(identityInputStream);
            IdentityTokenRequest identityTokenRequest = new IdentityTokenRequest();
            identityTokenRequest
                .withRunAsId((String) identityMap.get(RUN_AS_ID))
                .withNamespace((String) identityMap.get(NAMESPACE))
                .withRunAsIdName((String) identityMap.get(RUN_AS_ID_NAME))
                .withPodName(podName);
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
