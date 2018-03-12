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
package com.here.account.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import com.here.account.http.HttpException;
import com.here.account.http.HttpProvider;
import com.here.account.oauth2.AccessTokenException;
import com.here.account.oauth2.ErrorResponse;
import com.here.account.oauth2.RequestExecutionException;
import com.here.account.oauth2.ResponseParsingException;
import com.here.account.util.Serializer;

/**
 * A Service that represents a Resource Server, in OAuth2-speak.
 * It is expected that a wrapper class invokes methods on an instance 
 * of this class, so that simple Java create(), read(), update(), 
 * and delete() methods can be written with POJOs.
 * 
 * @author kmccrack
 */
public class Service {

    public static class Builder {
        private HttpProvider httpProvider;
        private Serializer serializer;
        private HttpProvider.HttpRequestAuthorizer clientAuthorizer;

        private Builder() {

        }

        public Builder withHttpProvider(HttpProvider httpProvider) {
            this.httpProvider = httpProvider;
            return this;
        }

        public Builder withSerializer(Serializer serializer) {
            this.serializer = serializer;
            return this;
        }

        public Builder withClientAuthorizer(HttpProvider.HttpRequestAuthorizer clientAuthorizer) {
            this.clientAuthorizer = clientAuthorizer;
            return this;
        }

        public Service build() {
            return new Service(httpProvider, serializer, clientAuthorizer);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final HttpProvider httpProvider;
    private final Serializer serializer;
    private final HttpProvider.HttpRequestAuthorizer clientAuthorizer;

    private Service(HttpProvider httpProvider, Serializer serializer,
                    HttpProvider.HttpRequestAuthorizer clientAuthorizer) {
        this.httpProvider = httpProvider;
        this.serializer = serializer;
        this.clientAuthorizer = clientAuthorizer;
    }

    /**
     * Gets the Response from the requested HTTP request.
     *
     * @param method the HTTP method
     * @param url the HTTP request URL
     * @param request the request object of type R, or null if no request object
     * @param responseClass the response object class, for deserialization
     * @param <R> the Request parameterized type
     * @param <T> the Response parameterized type
     * @return the Response of type T
     * @throws AccessTokenException
     * @throws RequestExecutionException
     * @throws ResponseParsingException
     */
    public <R, T> T getResponse(
            String method,
            String url,
            R request,
            Class<T> responseClass)
            throws AccessTokenException, RequestExecutionException, ResponseParsingException {

        HttpProvider.HttpRequest httpRequest;
        if (null == request) {
            httpRequest = httpProvider.getRequest(
                    clientAuthorizer, method, url, (String) null);
        } else {
            // HttpConstants.ContentTypes.JSON == requestContentType
            String jsonBody = serializer.objectToJson(request);
            httpRequest = httpProvider.getRequest(
                        clientAuthorizer, method, url, jsonBody);
        }

        // blocking
        HttpProvider.HttpResponse apacheResponse = null;
        InputStream jsonInputStream = null;

        try {
            apacheResponse = httpProvider.execute(httpRequest);
            jsonInputStream = apacheResponse.getResponseBody();
        } catch (IOException | HttpException e) {
            throw new RequestExecutionException(e);
        }

        int statusCode = apacheResponse.getStatusCode();
        try {
            if (200 == statusCode || 201 == statusCode || 204 == statusCode) {
                try {
                    return serializer.jsonToPojo(jsonInputStream,
                            responseClass);
                } catch (Exception e) {
                    throw new ResponseParsingException(e);
                }
            } else {
                ErrorResponse errorResponse;
                try {
                    // parse the error response
                    errorResponse = serializer.jsonToPojo(jsonInputStream, ErrorResponse.class);
                } catch (Exception e) {
                    // if there is trouble parsing the error
                    throw new ResponseParsingException(e);
                }
                throw new AccessTokenException(statusCode, errorResponse);
            }
        } finally {
            if (null != jsonInputStream) {
                try {
                    jsonInputStream.close();
                } catch (IOException ioe) {
                    throw new UncheckedIOException(ioe);
                }

            }
        }
    }


}
