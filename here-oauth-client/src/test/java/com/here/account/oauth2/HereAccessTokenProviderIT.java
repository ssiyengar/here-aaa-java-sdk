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
 */package com.here.account.oauth2;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;

import com.here.account.auth.OAuth1ClientCredentialsProvider;
import com.here.account.auth.provider.FromDefaultHereCredentialsIniFile;
import com.here.account.auth.provider.FromDefaultHereCredentialsIniStream;
import com.here.account.auth.provider.FromDefaultHereCredentialsPropertiesFileExposer;

/**
 * Created by kmccrack on 1/2/18.
 */
public class HereAccessTokenProviderIT {

    @Test
    public void test_builder_basic() throws IOException {
        try (
                HereAccessTokenProvider accessTokens = HereAccessTokenProvider.builder().build()
        ) {
            String accessToken = accessTokens.getAccessToken();
            assertTrue("accessToken was null", null != accessToken);
        }
    }
    
    @Ignore // we don't yet return credentials.ini files from our APIs
    @Test
    public void test_builder_ini() throws IOException {

        try (
                HereAccessTokenProvider accessTokens = HereAccessTokenProvider.builder()
                .setOAuth1CredentialsProvider(new FromDefaultHereCredentialsIniFile())
                .build()
        ) {
            String accessToken = accessTokens.getAccessToken();
            assertTrue("accessToken was null", null != accessToken);
        }
    }
    
    @Test
    public void test_builder_iniStream() throws IOException {

        try (
                InputStream inputStream = getTestIniFromPropertiesFile();
                HereAccessTokenProvider accessTokens = HereAccessTokenProvider.builder()
                .setOAuth1CredentialsProvider(new FromDefaultHereCredentialsIniStream(inputStream))
                .build()
        ) {
            String accessToken = accessTokens.getAccessToken();
            assertTrue("accessToken was null", null != accessToken);
        }
    }

    /**
     * Using the file ~/.here/credentials.properties, create some fake credentials.ini 
     * content by prepending the default section in memory, returning an InputStream to it.
     * 
     * @return
     * @throws IOException
     */
    protected InputStream getTestIniFromPropertiesFile() throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write("[default]\n".getBytes(StandardCharsets.UTF_8));
            File file = FromDefaultHereCredentialsPropertiesFileExposer.getDefaultHereCredentialsFile();
            Properties properties = OAuth1ClientCredentialsProvider.getPropertiesFromFile(file);
            for (Entry<Object, Object> property : properties.entrySet()) {
                Object name = property.getKey();
                Object value = property.getValue();
                String line = name + "=" + value + "\n";
                outputStream.write(line.getBytes(StandardCharsets.UTF_8));
            }
            outputStream.flush();
            byte[] bytes = outputStream.toByteArray();
            return new ByteArrayInputStream(bytes);
        }
    }

}
