/*
 * Copyright (c) 2017 HERE Europe B.V.
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
import com.here.account.oauth2.ClientCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * A {@link ClientAuthorizationProvider} that pulls credential values from the
 * default "~/.here/credentials.properties" file.
 */
public class FromDefaultHereCredentialsPropertiesFile implements ClientAuthorizationProvider {

    private static final String CREDENTIALS_DOT_PROPERTIES_FILENAME = "credentials.properties";

    public FromDefaultHereCredentialsPropertiesFile() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientCredentialsProvider getClientCredentialsProvider() {
        File file = getDefaultHereCredentialsFile();
        try {
            Properties properties = OAuth1ClientCredentialsProvider.getPropertiesFromFile(file);
            return FromSystemProperties.getClientCredentialsProviderWithDefaultTokenEndpointUrl(properties);
        } catch (IOException e) {
            throw new RuntimeException("trouble FromFile " + e, e);
        }
    }

    static File getDefaultHereCredentialsFile() {
        return DefaultHereConfigFiles.getDefaultHereConfigFile(CREDENTIALS_DOT_PROPERTIES_FILENAME);
    }


}
