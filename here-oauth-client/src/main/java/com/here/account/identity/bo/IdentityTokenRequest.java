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
package com.here.account.identity.bo;

import com.here.account.oauth2.AccessTokenRequest;

public class IdentityTokenRequest extends AccessTokenRequest {
    
    public static final String IDENTITY_GRANT_TYPE = "identity";
    
    private String runAsId;
    private String namespace;
    private String name;
    
    public IdentityTokenRequest() {
        super(IDENTITY_GRANT_TYPE);
    }

    public String getRunAsId() {
        return runAsId;
    }

    public IdentityTokenRequest setRunAsId(String runAsId) {
        this.runAsId = runAsId;
        return this;
    }

    public String getNamespace() {
        return namespace;
    }

    public IdentityTokenRequest setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public String getName() {
        return name;
    }

    public IdentityTokenRequest setName(String name) {
        this.name = name;
        return this;
    }

    
}
