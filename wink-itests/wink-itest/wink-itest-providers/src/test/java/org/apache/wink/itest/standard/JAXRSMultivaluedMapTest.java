/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.wink.itest.standard;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import junit.framework.TestCase;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.wink.test.integration.ServerEnvironmentInfo;

public class JAXRSMultivaluedMapTest extends TestCase {

    public String getBaseURI() {
        if (ServerEnvironmentInfo.isRestFilterUsed()) {
            return ServerEnvironmentInfo.getBaseURI();
        }
        return ServerEnvironmentInfo.getBaseURI() + "/standard";
    }

    /**
     * Tests posting to a MultivaluedMap with application/x-www-form-urlencoded
     * request Content-Type.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testPostMultivaluedMap() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod = new PostMethod(getBaseURI() + "/providers/standard/multivaluedmap");
        postMethod.setRequestEntity(new StringRequestEntity("tuv=wxyz&abcd=",
                                                            "application/x-www-form-urlencoded",
                                                            "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(200, postMethod.getStatusCode());
            InputStream is = postMethod.getResponseBodyAsStream();

            InputStreamReader isr = new InputStreamReader(is);
            char[] buffer = new char[1];
            int read = 0;
            int offset = 0;
            while ((read = isr.read(buffer, offset, buffer.length - offset)) != -1) {
                offset += read;
                if (offset >= buffer.length) {
                    buffer = ArrayUtils.copyOf(buffer, buffer.length * 2);
                }
            }
            char[] carr = ArrayUtils.copyOf(buffer, offset);

            int checkEOF = is.read();
            assertEquals(-1, checkEOF);
            String str = new String(carr);

            assertTrue(str, "abcd=&tuv=wxyz".equals(str) || "tuv=wxyz&abcd=".equals(str));
            assertEquals("application/x-www-form-urlencoded", postMethod
                .getResponseHeader("Content-Type").getValue());
            Header contentLengthHeader = postMethod.getResponseHeader("Content-Length");
            if (contentLengthHeader != null) {
                // some of the containers can be "smarter" and set the
                // content-length for us if the payload is small
                assertEquals("14", contentLengthHeader.getValue());
            }
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests posting to a MultivaluedMap with a request Content-Type that is not
     * application/x-www-form-urlencoded.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testPostMultivaluedMapNotFormURLEncoded() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod = new PostMethod(getBaseURI() + "/providers/standard/multivaluedmap");
        postMethod
            .setRequestEntity(new StringRequestEntity("tuv=wxyz&abcd=", "text/plain", "UTF-8"));
        try {
            client.executeMethod(postMethod);

            assertEquals(415, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests posting to a MultivaluedMap with a request Accept type that is not
     * application/x-www-form-urlencoded.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testPostMultivaluedMapAcceptNotFormURLEncoded() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(getBaseURI() + "/providers/standard/multivaluedmap/noproduces");
        postMethod.setRequestEntity(new StringRequestEntity("tuv=wxyz&abcd=",
                                                            "application/x-www-form-urlencoded",
                                                            "UTF-8"));
        postMethod.addRequestHeader("Accept", "not/expected");
        try {
            client.executeMethod(postMethod);

            assertEquals(500, postMethod.getStatusCode());
        } finally {
            postMethod.releaseConnection();
        }
    }

    /**
     * Tests putting and then getting a /multivaluedmap.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testPutReader() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PutMethod putMethod = new PutMethod(getBaseURI() + "/providers/standard/multivaluedmap");
        putMethod.setRequestEntity(new StringRequestEntity("username=user1&password=user1password",
                                                           "application/x-www-form-urlencoded",
                                                           "UTF-8"));
        try {
            client.executeMethod(putMethod);
            assertEquals(204, putMethod.getStatusCode());
        } finally {
            putMethod.releaseConnection();
        }

        GetMethod getMethod = new GetMethod(getBaseURI() + "/providers/standard/multivaluedmap");
        getMethod.addRequestHeader("Accept", "application/x-www-form-urlencoded");
        try {
            client.executeMethod(getMethod);
            assertEquals(200, getMethod.getStatusCode());
            InputStream is = getMethod.getResponseBodyAsStream();

            InputStreamReader isr = new InputStreamReader(is);
            char[] buffer = new char[1];
            int read = 0;
            int offset = 0;
            while ((read = isr.read(buffer, offset, buffer.length - offset)) != -1) {
                offset += read;
                if (offset >= buffer.length) {
                    buffer = ArrayUtils.copyOf(buffer, buffer.length * 2);
                }
            }
            char[] carr = ArrayUtils.copyOf(buffer, offset);

            int checkEOF = is.read();
            assertEquals(-1, checkEOF);
            String str = new String(carr);

            assertTrue(str,
                       "username=user1&password=user1password".equals(str) || "password=user1password&username=user1"
                           .equals(str));
            assertEquals("application/x-www-form-urlencoded", getMethod
                .getResponseHeader("Content-Type").getValue());
            Header contentLengthHeader = getMethod.getResponseHeader("Content-Length");
            if (contentLengthHeader != null) {
                // some of the containers can be "smarter" and set the
                // content-length for us if the payload is small
                assertEquals("37", contentLengthHeader.getValue());
            }
        } finally {
            getMethod.releaseConnection();
        }
    }

    /**
     * Tests posting an empty request entity to a MultivaluedMap.
     * 
     * @throws HttpException
     * @throws IOException
     */
    public void testSendingNoRequestEntityMultivaluedMap() throws HttpException, IOException {
        HttpClient client = new HttpClient();

        PostMethod postMethod =
            new PostMethod(getBaseURI() + "/providers/standard/multivaluedmap/empty");
        postMethod.addRequestHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED);
        try {
            client.executeMethod(postMethod);
            assertEquals(200, postMethod.getStatusCode());
            assertEquals("expected", postMethod.getResponseBodyAsString());
        } finally {
            postMethod.releaseConnection();
        }
    }

    //
    // /**
    // * Tests a resource method invoked with a MultivaluedMap&lt;String,
    // * Object&gt; as a parameter. This should fail with a 415 since the reader
    // * has no way to necessarily wrap it to the type.
    // *
    // * @throws HttpException
    // * @throws IOException
    // */
    // public void testMultivaluedMapImplementation() throws HttpException,
    // IOException {
    // HttpClient client = new HttpClient();
    //
    // PostMethod postMethod = new PostMethod(getBaseURI()
    // + "/providers/standard/multivaluedmap/subclasses/shouldfail");
    // byte[] barr = new byte[1000];
    // Random r = new Random();
    // r.nextBytes(barr);
    // postMethod.setRequestEntity(new InputStreamRequestEntity(new
    // ByteArrayInputStream(barr),
    // "any/type"));
    // try {
    // client.executeMethod(postMethod);
    // assertEquals(415, postMethod.getStatusCode());
    // } finally {
    // postMethod.releaseConnection();
    // }
    //
    // postMethod = new PostMethod(getBaseURI()
    // + "/providers/standard/multivaluedmap/subclasses/shouldfail");
    // postMethod.setRequestEntity(new InputStreamRequestEntity(new
    // ByteArrayInputStream(barr),
    // "application/x-www-form-urlencoded"));
    // try {
    // client.executeMethod(postMethod);
    // assertEquals(415, postMethod.getStatusCode());
    // System.out.println(postMethod.getResponseBodyAsString());
    // } finally {
    // postMethod.releaseConnection();
    // }
    // }

}
