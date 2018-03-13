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
package com.here.account.oauth2;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.here.account.auth.provider.ClientAuthorizationProviderChain;
import com.here.account.auth.provider.IdentityAuthorizationRequestProvider;
import com.here.account.http.HttpException;
import com.here.account.http.HttpProvider;
import com.here.account.http.HttpProvider.HttpRequest;
import com.here.account.http.HttpProvider.HttpResponse;
import com.here.account.identity.bo.IdentityTokenRequest;
import com.here.account.util.JacksonSerializer;

public class HereAccessTokenProviderIdentityRequestTest {

    
    
    private final int ONE_DAY_IN_SECONDS = 24*60*60;

    Map<String, Object> fileAccessTokenResponse;
    String accessToken;
    JacksonSerializer serializer;
    File identityFile;
    File podNameFile;
    ClientAuthorizationRequestProvider clientAuthorizationRequestProvider;
    
    protected void setUpIdentityFile() throws IOException {
        /*
             private String runAsId;
    private String namespace;
    private String runAsIdName;
    private String podName;

         */
        String userId = "HERE-123";
        String namespace = "my-namespace";
        String runAsIdName = "bbcde";
        identityFile = File.createTempFile(UUID.randomUUID().toString(), null);
        String identity = "{\"runAsId\":\"" + userId
                + "\",\"namespace\":\"" + namespace
                + "\",\"runAsIdName\":\"" + runAsIdName
                + "\"}";
        writeToFile(identityFile, identity);
    }
    
    private void writeToFile(File file, String s) throws IOException {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            outputStream.write(bytes);
            outputStream.flush();
        }
    }
    
    private void setUpPodNameFile() throws IOException {
        podNameFile = File.createTempFile(UUID.randomUUID().toString(), null);
        writeToFile(podNameFile, "pod-abc-123");
    }
    
    @Before
    public void setUp() throws IOException {
        serializer = new JacksonSerializer();
        setUpIdentityFile();
        setUpPodNameFile();
        
        String tokenUrl = "http://example.com/token";

        fileAccessTokenResponse = new HashMap<String, Object>();
        accessToken = "h1.test.value";
        fileAccessTokenResponse.put("access_token", accessToken);
        Long expOneDay = (System.currentTimeMillis() / 1000) + ONE_DAY_IN_SECONDS;
        fileAccessTokenResponse.put("exp", expOneDay);
        
        clientAuthorizationRequestProvider = 
                new IdentityAuthorizationRequestProvider(
                        serializer,
                        null,
                        identityFile,
                        podNameFile,
                        tokenUrl);


    }
    
    @After
    public void tearDown() {
        delete(podNameFile);
        delete(identityFile);
    }
    
    private void delete(File file) {
        if (null != file) {
            file.delete();
        }
    }
    
    @Test(expected=NullPointerException.class)
    public void test_no_exp() throws IOException, HttpException {
        exp = null;
        test_HereAccessTokenProvider_Identity();
    }
    
    @Test(expected=NullPointerException.class)
    public void test_null_exp_providerChain() throws IOException, HttpException {
        clientAuthorizationRequestProvider = new ClientAuthorizationProviderChain(clientAuthorizationRequestProvider);

        test_no_exp();
    }
    
    Integer exp = 60;

    
    @Test
    public void test_HereAccessTokenProvider_Identity() throws IOException, HttpException {
        AccessTokenRequest accessTokenRequest = clientAuthorizationRequestProvider.getNewAccessTokenRequest();
        assertTrue("accessTokenRequest wasn't of correct type "+accessTokenRequest, 
                accessTokenRequest.getClass().isAssignableFrom(IdentityTokenRequest.class));
        
        HttpProvider httpProvider = mock(HttpProvider.class);

        HttpResponse httpResponse = mock(HttpResponse.class);
        Map<String, Object> responseMap = new HashMap<String, Object>();
        responseMap.put("access_token", accessToken);
        if (null != exp) {
            responseMap.put("expires_in", exp);
        }
        String responseBody = serializer.objectToJson(responseMap);
        byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);
        Mockito.when(httpResponse.getResponseBody()).thenReturn(new ByteArrayInputStream(responseBytes));
        Mockito.when(httpResponse.getStatusCode()).thenReturn(200);
        Mockito.when(httpResponse.getContentLength()).thenReturn((long) responseBytes.length);
        Mockito.when(httpProvider.execute(Mockito.any(HttpRequest.class))).thenReturn(httpResponse);
        
        HereAccessTokenProvider tokenProvider = HereAccessTokenProvider.builder()
                .setHttpProvider(httpProvider)
                .setClientAuthorizationRequestProvider(clientAuthorizationRequestProvider).build();
        String actualAccessToken = tokenProvider.getAccessToken();
        assertTrue("expected accessToken " + accessToken + ", actual accessToken " + actualAccessToken, 
                accessToken.equals(actualAccessToken));
    }
    

}
