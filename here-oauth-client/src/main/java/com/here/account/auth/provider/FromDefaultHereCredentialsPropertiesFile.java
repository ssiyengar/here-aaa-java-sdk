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

    private static final String USER_DOT_HOME = "user.home";
    private static final String DOT_HERE_SUBDIR = ".here";
    private static final String CREDENTIALS_DOT_PROPERTIES_FILENAME = "credentials.properties";
    private static final String DEFAULT_CREDENTIALS_FILE_PATH = "~" + File.separatorChar + DOT_HERE_SUBDIR
            + File.separatorChar + CREDENTIALS_DOT_PROPERTIES_FILENAME;

    public FromDefaultHereCredentialsPropertiesFile() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientCredentialsProvider getClientAuthorization() {
        File file = getDefaultHereCredentialsFile();
        try {
            Properties properties = OAuth1ClientCredentialsProvider.getPropertiesFromFile(file);
            return FromSystemProperties.getClientCredentialsProviderWithDefaultTokenEndpointUrl(properties);
        } catch (IOException e) {
            throw new RuntimeException("trouble FromFile " + e, e);
        }
    }

    protected static File getDefaultHereCredentialsFile() {
        String userDotHome = System.getProperty(USER_DOT_HOME);
        if (userDotHome != null && userDotHome.length() > 0) {
            File dir = new File(userDotHome, DOT_HERE_SUBDIR);
            File file = new File(dir, CREDENTIALS_DOT_PROPERTIES_FILENAME);
            if (isFileExistsAndCanRead(file)) {
                return file;
            }
        }
        return null;
    }

    protected static boolean isFileExistsAndCanRead(File file) {
        return file.isFile() && file.exists() && file.canRead();
    }

}
