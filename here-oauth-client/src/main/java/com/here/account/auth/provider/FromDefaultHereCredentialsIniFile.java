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

import com.here.account.auth.OAuth1ClientCredentialsProvider;
import com.here.account.http.HttpProvider.HttpRequestAuthorizer;
import com.here.account.oauth2.ClientCredentialsProvider;
import com.here.account.util.OAuthConstants;
import org.ini4j.Ini;

import java.io.*;
import java.util.Properties;

/**
 * @author kmccrack
 */
public class FromDefaultHereCredentialsIniFile implements ClientCredentialsProvider {

    private static final String CREDENTIALS_DOT_INI_FILENAME = "credentials.ini";

    private final File file;
    
    private final String sectionName;

    public FromDefaultHereCredentialsIniFile() {
        this(getDefaultHereCredentialsIniFile(), FromDefaultHereCredentialsIniStream.DEFAULT_INI_SECTION_NAME);
    }

    public FromDefaultHereCredentialsIniFile(File file, String sectionName) {
        this.file = file;
        this.sectionName = sectionName;
    }

    protected ClientCredentialsProvider getClientCredentialsProvider() {
        try (InputStream inputStream = new FileInputStream(file)) {
            Properties properties = FromDefaultHereCredentialsIniStream
                    .getPropertiesFromIni(inputStream, sectionName);
            return FromSystemProperties.getClientCredentialsProviderWithDefaultTokenEndpointUrl(properties);
        } catch (IOException e) {
            throw new RuntimeException("trouble FromFile " + e, e);
        }
    }

    protected static File getDefaultHereCredentialsIniFile() {
        return DefaultHereConfigFiles.getDefaultHereConfigFile(CREDENTIALS_DOT_INI_FILENAME);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTokenEndpointUrl() {
        return getClientCredentialsProvider().getTokenEndpointUrl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpRequestAuthorizer getClientAuthorizer() {
        return getClientCredentialsProvider().getClientAuthorizer();
    }

}
