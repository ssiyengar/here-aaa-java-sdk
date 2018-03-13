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
package com.here.account.client;

import static org.junit.Assert.assertTrue;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.junit.Assert;
import org.junit.Test;

public class ClientTest {

    @Test
    public void test_nullSafeCloseThrowingUnchecked_null() {
        Client.nullSafeCloseThrowingUnchecked(null);
    }

    @Test
    public void test_nullSafeCloseThrowingUnchecked_noException() {
        Closeable closeable = new Closeable() {

            @Override
            public void close() throws IOException {
                // no exceptions thrown
            }
            
        };
        Client.nullSafeCloseThrowingUnchecked(closeable);
    }
    
    @Test
    public void test_nullSafeCloseThrowingUnchecked_withException() {
        final String message = "test I/O trouble!";
        Closeable closeable = new Closeable() {

            @Override
            public void close() throws IOException {
                throw new IOException(message);
            }
            
        };
        try {
            Client.nullSafeCloseThrowingUnchecked(closeable);
            Assert.fail("should have thrown UncheckedIOException");
        } catch (UncheckedIOException unchecked) {
            IOException ioe = unchecked.getCause();
            assertTrue("ioe was null", null != ioe);
            String actualMessage = ioe.getMessage();
            assertTrue("message was expected "+message+", actual "+actualMessage, 
                    message.equals(actualMessage));
        }
    }


}
