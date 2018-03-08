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

import java.util.List;
import java.util.Map;

import com.here.account.http.HttpConstants.ContentTypes;
import com.here.account.http.HttpProvider;
import com.here.account.http.HttpProvider.HttpRequest;
import com.here.account.http.HttpProvider.HttpRequestAuthorizer;
import com.here.account.identity.bo.IdentityTokenRequest;
import com.here.account.oauth2.AccessTokenRequest;
import com.here.account.oauth2.ClientAuthorizationRequestProvider;

public class IdentityAuthorizationFileProvider implements ClientAuthorizationRequestProvider {

    /**
     * The HERE Access Token URL.
     */
    private static final String IDENTITY_SERVICE_TOKEN_ENDPOINT_URL = 
            "file:///var/run/secrets/identity/access-token";
    
    private final String tokenUrl;
    
    public IdentityAuthorizationFileProvider() {
        this(IDENTITY_SERVICE_TOKEN_ENDPOINT_URL);
    }
    
    public IdentityAuthorizationFileProvider(String tokenUrl) {
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
        return getAuthorizer();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public AccessTokenRequest getNewAccessTokenRequest() {
        return getRequest();
    }
    
    protected HttpProvider.HttpRequestAuthorizer getAuthorizer() {
        return new HttpProvider.HttpRequestAuthorizer() {

            @Override
            public void authorize(HttpRequest httpRequest, String method, String url,
                    Map<String, List<String>> formParams) {
                // nothing to do, because reading a File doesn't use an HttpRequest 
                // to be authorized
            }
            
        };
    }
    
    protected IdentityTokenRequest getRequest() {
        return new IdentityTokenRequest();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContentTypes getRequestContentType() {
        return ContentTypes.JSON;
    }

}
