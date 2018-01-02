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

import com.here.account.oauth2.ClientCredentialsProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author kmccrack
 */
public class ClientAuthorizationProviderChain implements ClientAuthorizationProvider {

    private static final Logger LOG = Logger.getLogger(ClientAuthorizationProviderChain.class.getName());

    private boolean reuseMostRecentProvider = true;
    private ClientAuthorizationProvider mostRecentProvider = null;
    private List<ClientAuthorizationProvider> clientAuthorizationProviders;

    public ClientAuthorizationProviderChain(ClientAuthorizationProvider... clientAuthorizationProviders) {
        this.clientAuthorizationProviders = new ArrayList<ClientAuthorizationProvider>();
        for (ClientAuthorizationProvider clientAuthorizationProvider : clientAuthorizationProviders) {
            this.clientAuthorizationProviders.add(clientAuthorizationProvider);
        }
    }

    public ClientAuthorizationProviderChain(List<ClientAuthorizationProvider> clientAuthorizationProviders) {
        this.clientAuthorizationProviders = new ArrayList<ClientAuthorizationProvider>(clientAuthorizationProviders);
    }

    public static ClientAuthorizationProviderChain DEFAULT_CLIENT_CREDENTIALS_PROVIDER_CHAIN = getDefaultClientCredentialsProviderChain();

    private static ClientAuthorizationProviderChain getDefaultClientCredentialsProviderChain() {
        ClientAuthorizationProvider systemProvider = new FromSystemProperties();
        ClientAuthorizationProvider iniFileProvider = new FromDefaultHereCredentialsIniFile();
        ClientAuthorizationProvider propertiesFileProvider = new FromDefaultHereCredentialsPropertiesFile();
        return new ClientAuthorizationProviderChain(
                systemProvider,
                iniFileProvider,
                propertiesFileProvider
                );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientCredentialsProvider getClientCredentialsProvider() {
        if (reuseMostRecentProvider && mostRecentProvider != null) {
            return mostRecentProvider.getClientCredentialsProvider();
        }

        for (ClientAuthorizationProvider factory : clientAuthorizationProviders) {
            try {
                ClientCredentialsProvider credentials = factory.getClientCredentialsProvider();

                if (null != credentials.getTokenEndpointUrl()
                    && null != credentials.getClientAuthorizer()) {
                    LOG.info("Loading credentials from " + factory.toString());

                    mostRecentProvider = factory;
                    return credentials;
                }
            } catch (Exception e) {
                // Ignore any exceptions and move onto the next provider
                LOG.warning("Unable to load credentials from " + factory.toString() +
                        ": " + e.getMessage());
            }
        }

        throw new RuntimeException("Unable to load credentials from chain");
    }
}
