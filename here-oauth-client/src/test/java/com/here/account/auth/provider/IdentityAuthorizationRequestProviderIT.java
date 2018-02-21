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

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

import com.here.account.oauth2.HereAccessTokenProvider;


@Ignore
public class IdentityAuthorizationRequestProviderIT {
    
    private IdentityAuthorizationRequestProvider identityAuthorizationRequestProvider = new IdentityAuthorizationRequestProvider();

    @Test
    public void test_identity_authorization_alwaysNew() {
        HereAccessTokenProvider hereAccessTokenProvider = 
                HereAccessTokenProvider.builder()
                .setClientAuthorizationRequestProvider(identityAuthorizationRequestProvider)
                .setAlwaysRequestNewToken(true)
                .build();
        String accessToken = hereAccessTokenProvider.getAccessToken();
        assertTrue("accessToken was null", null != accessToken);
        assertTrue("accessToken was blank", accessToken.trim().length() > 0);
        
    }
    
    @Test
    public void test_identity_authorization() {
        HereAccessTokenProvider hereAccessTokenProvider = 
                HereAccessTokenProvider.builder()
                .setClientAuthorizationRequestProvider(identityAuthorizationRequestProvider)
                .build();
        String accessToken = hereAccessTokenProvider.getAccessToken();
        assertTrue("accessToken was null", null != accessToken);
        assertTrue("accessToken was blank", accessToken.trim().length() > 0);
    }

}
