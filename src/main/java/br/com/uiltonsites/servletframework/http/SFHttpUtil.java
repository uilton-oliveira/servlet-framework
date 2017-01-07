/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package br.com.uiltonsites.servletframework.http;

import br.com.uiltonsites.servletframework.abstracts.SFLifeCycle;
import br.com.uiltonsites.servletframework.utility.SFLogger;
import br.com.uiltonsites.servletframework.utility.exceptions.SFParameterException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author Uilton Oliveira <uilton.dev@gmail.com>
 */
public class SFHttpUtil extends SFLifeCycle {
    public static final String TYPE_APPLICATION_XML = "application/xml";
    public static final String TYPE_APPLICATION_JSON = "application/json";
    public static final String TYPE_MULTIPART_FORM_DATA = "multipart/form-data";
    public static final String TYPE_APPLICATION_URLENCODED = "application/x-www-form-urlencoded";
    public static final String TYPE_TEXT_HTML = "text/html";
    
    private String encoding = "UTF-8";
    private int lastStatusCode;
    
    //public static final int MAX_CONNECTION = 600;
    public static final int MAX_CONNECTION = 600;
    public static final int TIMEOUT = 60000; // 1 min
    private static PoolingHttpClientConnectionManager cm;
    private static final RequestConfig requestConfig;
    public static CloseableHttpClient httpclient; 
    
    static {
        requestConfig = RequestConfig.custom()
        .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
        .setConnectTimeout(TIMEOUT)
        .setConnectionRequestTimeout(TIMEOUT)
        .setSocketTimeout(TIMEOUT)
        .build();
    }

    public SFHttpUtil(SFLogger logger) {
        super(logger);
    }

    public SFHttpUtil() {
    }

    public static CloseableHttpClient newHttpClient() {
        if (cm == null) {
            cm = new PoolingHttpClientConnectionManager();
            cm.setMaxTotal(MAX_CONNECTION);
            cm.setDefaultMaxPerRoute(MAX_CONNECTION);
        }
        if (httpclient == null) {
            httpclient = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(new DefaultHttpRequestRetryHandler())
                .build();
        }
        return httpclient;
    }
    
    public static CloseableHttpClient getHttpClientInstance() {
        return newHttpClient();
    }
    
    public static void releaseConnections() {
        if (httpclient != null) {
            try {
                httpclient.close();
                httpclient = null;
            } catch (IOException ex) {
            }
        }
        if (cm != null) {
            cm.shutdown();
            cm.close();
            cm = null;
        }
    }
    
    private SFHttpResult parseRawResponse(BufferedReader in) throws IOException {
        SFHttpResult hr = new SFHttpResult();
        String line;
        StringBuilder response = new StringBuilder();
        boolean isResponse = false;
        boolean isFirst = true;
        
        while ((line = in.readLine()) != null) {
            if (isFirst) {
                isFirst = false;
                String[] status = line.split(" ");
                hr.statusCode = Integer.parseInt(status[1].trim());
                lastStatusCode = hr.statusCode;
                hr.reasonPhrase = status[2].trim();
            } else if (isResponse == false && line.trim().isEmpty()) {
                isResponse = true;
            } else if (isResponse) {
                response.append(line);
                response.append("\n");
            }
        }
        hr.output = response.toString();
        return hr;
    }
    
    public SFHttpResult sendRawData(String url, int port, String data) {
        SFHttpResult hr = new SFHttpResult();
        logger.info("Sending Raw Data to: " + url + " | Port: " + port);
        Socket socket = null;
        
        OutputStreamWriter osw;
        BufferedReader in;
        try {
            socket = new Socket(url, port);
            osw =new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8")); 
            
            osw.write(data, 0, data.length());
            osw.flush();
            
            hr = parseRawResponse(in);
            
        } catch (IOException ex) {
            logger.warn("Failed to send POST data to: " + url);
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                } else {
                    logger.warn("Failed to close socket, socket==null");
                }
            } catch (IOException ex) {
                logger.warn("Failed to send POST data to: " + url, ex);
            }
        }
        return hr;
    }
    
    public SFHttpResult sendRawData(String url, String data) {
        return sendRawData(url, 80, data);
    }
    
    private void closeResource(CloseableHttpClient httpClient, CloseableHttpResponse response) {
//        if (httpClient != null) {
//            try {
//                httpClient.close();
//            } catch (IOException ex) {
//                logger.error("failed to close httpClient");
//            }
//        }
        if (response != null) {
            try {
                response.close();
            } catch (IOException ex) {
                logger.error("failed to close response");
            }
        }
    }
    
    public SFHttpResult sendPost(String url, HttpEntity httpEntity, String contentType) {
        
        return sendPost(url, httpEntity, contentType, null);
        
    }
    
    public SFHttpResult sendPost(String url, HttpEntity httpEntity, String contentType, List<Header> headers) {
        SFHttpResult result = new SFHttpResult();
        
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        
        try {
            httpClient = newHttpClient();
            HttpPost request = new HttpPost(url);
            request.setHeader("Content-Type", contentType);
            
            if (headers != null && !headers.isEmpty()) {
                for (Header header : headers) {
                    request.setHeader(header);
                }
            }
            
            request.setEntity(httpEntity);
            
            response = httpClient.execute(request);
            result.output = EntityUtils.toString(response.getEntity(), encoding);
            result.statusCode = response.getStatusLine().getStatusCode();
            lastStatusCode = result.statusCode;
            result.reasonPhrase = response.getStatusLine().getReasonPhrase();
            
        } catch (UnsupportedEncodingException ex) {
            logger.warn("Failed to send POST data to: " + url, ex);
        } catch (IOException | ParseException ex) {
            logger.warn("Failed to send POST data to: " + url, ex);
        } catch (Exception ex) {
            logger.error("Failed to send POST data to: " + url, ex);
        } finally {
            closeResource(httpClient, response);
        }
        return result;
    }
    
    public SFHttpResult sendPost(String url, String data, String contentType) {
        return sendPost(url, data, contentType, null);
    }
    
    public SFHttpResult sendPost(String url, String data, String contentType, List<Header> headers) {
        logger.trace("Sending POST to: " + url);
        logger.trace("### Content of POST ###");
        logger.trace("\n"+data);
        try {
            return sendPost(url, new StringEntity(data), contentType, headers);
        } catch (UnsupportedEncodingException ex) {
            logger.warn("Failed to send POST data to: " + url, ex);
            return null;
        }
    }
    
    public SFHttpResult sendPost(String url, Map<String, String> data, String contentType) {
        return sendPost(url, data, contentType, null);
    }
    public SFHttpResult sendPost(String url, Map<String, String> data, String contentType, List<Header> headers) {
        logger.trace("Sending POST to: " + url);
        logger.trace("### Content of POST ###");
        
        StringBuilder kv = new StringBuilder("&");
        
        try {
            
            ArrayList<NameValuePair> parameters = new ArrayList<>();
            
            for (Map.Entry<String, String> entry : data.entrySet()) {
                parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                kv.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            
            kv.setLength(kv.length()-1);
            logger.trace("\n"+kv.toString());
            
            return sendPost(url, new UrlEncodedFormEntity(parameters), contentType, headers);
        } catch (UnsupportedEncodingException ex) {
            logger.warn("Failed to send POST data to: " + url, ex);
            return null;
        }
    }
    
    public SFHttpResult sendGet(String url, Map<String, String> data, String contentType) throws SFParameterException {
        
        return sendGet(url, data, contentType, null);
        
    }
    
    public SFHttpResult sendGet(String url, Map<String, String> data, String contentType, List<Header> headers) throws SFParameterException {
        
        if(url == null || data == null ) {
            throw new SFParameterException("Error in sendGet parameters", logger);
        }
        
        StringBuilder strBuilder = new StringBuilder(url);
        strBuilder.append("?");
        
        boolean first = true;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (first) {
                first = false;
            } else {
                strBuilder.append("&");
            }
            strBuilder.append(entry.getKey());
            strBuilder.append("=");
            strBuilder.append(String.valueOf(entry.getValue()));
            
        }
        
        return sendGet(strBuilder.toString(), contentType, headers);
    }
    
    public SFHttpResult sendGet(String url, String contentType) {
        return sendGet(url, contentType, null);
    }
    
    public SFHttpResult sendGet(String url, String contentType, List<Header> headers) {
        logger.trace("Sending GET to: " + url);
        SFHttpResult result = new SFHttpResult();
        
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        
        try {
            httpClient = newHttpClient();
            HttpGet request = new HttpGet(url);
            request.setHeader("Content-Type", contentType);
            
            if (headers != null && !headers.isEmpty()) {
                for (Header header : headers) {
                    request.setHeader(header);
                }
            }
            
            response = httpClient.execute(request);
            
            result.output = EntityUtils.toString(response.getEntity(), encoding);
            result.statusCode = response.getStatusLine().getStatusCode();
            lastStatusCode = result.statusCode;
            result.reasonPhrase = response.getStatusLine().getReasonPhrase();
                        
        } catch (UnsupportedEncodingException ex) {
            logger.error("Failed to send GET data to: " + url, ex);
        } catch (IOException | ParseException ex) {
            logger.error("Failed to send GET data to: " + url, ex);
        } catch (Exception ex) {
            logger.error("Failed to send GET data to: " + url, ex);
        } finally {
            closeResource(httpClient, response);
        }
        return result;
    }
    
    public static String getRequestParamIsensitive(HttpServletRequest request, String parameter) {
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            if(entry.getKey().equalsIgnoreCase(parameter)) {
                return entry.getValue()[0];
            }
        }
        return null;
    }

    /**
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    
    /**
    * @return the lastStatusCode
    */
    public int getLastStatusCode() {
        return lastStatusCode;
    }

    @Override
    public void onDestroy() {
        releaseConnections();
    }

    @Override
    public void onCreate() {
    }
    
}
