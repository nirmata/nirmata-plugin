
package io.jenkins.plugins.nirmata.util;

import java.util.Arrays;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jenkins.plugins.nirmata.model.HTTPInfo;

public abstract class HttpClient {

    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);
    private static final String CHARSET = "UTF-8";

    private HttpClient() {

    }

    public static HTTPInfo doGet(String uri, String contentType, String authorization) {
        HTTPInfo httpInfo = new HTTPInfo();
        CloseableHttpResponse result = null;

        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpUriRequest request = RequestBuilder.get()
                .setUri(uri)
                .setHeader(HttpHeaders.CONTENT_TYPE, contentType)
                .setHeader(HttpHeaders.AUTHORIZATION, authorization)
                .build();
            result = client.execute(request);

            String jsonResponse = EntityUtils.toString(result.getEntity(), CHARSET);
            EntityUtils.consume(result.getEntity());
            result.close();

            httpInfo.setMethod(request.getMethod());
            httpInfo.setUri(request.getURI().toString());
            httpInfo.setHeaders(Arrays.asList(request.getAllHeaders()));
            httpInfo.setPayload(jsonResponse);
        } catch (Exception e) {
            logger.error("Exception: {}", e);
            httpInfo.setMessage(e.getMessage());
        }

        if (result != null) {
            httpInfo.setStatusCode(result.getStatusLine().getStatusCode());
            httpInfo.setMessage(result.getStatusLine().getReasonPhrase());
        }

        return httpInfo;
    }

    public static HTTPInfo doPost(String uri, String contentType, String authorization, StringEntity entity) {
        HTTPInfo httpInfo = new HTTPInfo();
        CloseableHttpResponse result = null;

        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpUriRequest request = RequestBuilder.post()
                .setUri(uri)
                .setEntity(entity)
                .setHeader(HttpHeaders.CONTENT_TYPE, contentType)
                .setHeader(HttpHeaders.AUTHORIZATION, authorization)
                .build();
            result = client.execute(request);

            String jsonResponse = EntityUtils.toString(result.getEntity(), CHARSET);
            EntityUtils.consume(result.getEntity());
            result.close();

            httpInfo.setMethod(request.getMethod());
            httpInfo.setEntity(entity.toString());
            httpInfo.setUri(request.getURI().toString());
            httpInfo.setHeaders(Arrays.asList(request.getAllHeaders()));
            httpInfo.setPayload(jsonResponse);
        } catch (Exception e) {
            logger.error("Exception: {}", e);
            httpInfo.setMessage(e.getMessage());
        }

        if (result != null) {
            httpInfo.setStatusCode(result.getStatusLine().getStatusCode());
            httpInfo.setMessage(result.getStatusLine().getReasonPhrase());
        }

        return httpInfo;
    }

    public static HTTPInfo doDelete(String uri, String contentType, String authorization) {
        HTTPInfo httpInfo = new HTTPInfo();
        CloseableHttpResponse result = null;

        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpUriRequest request = RequestBuilder.delete()
                .setUri(uri)
                .setHeader(HttpHeaders.CONTENT_TYPE, contentType)
                .setHeader(HttpHeaders.AUTHORIZATION, authorization)
                .build();
            result = client.execute(request);

            String jsonResponse = EntityUtils.toString(result.getEntity(), CHARSET);
            EntityUtils.consume(result.getEntity());
            result.close();

            httpInfo.setMethod(request.getMethod());
            httpInfo.setUri(request.getURI().toString());
            httpInfo.setHeaders(Arrays.asList(request.getAllHeaders()));
            httpInfo.setPayload(jsonResponse);
        } catch (Exception e) {
            logger.error("Exception: {}", e);
            httpInfo.setMessage(e.getMessage());
        }

        if (result != null) {
            httpInfo.setStatusCode(result.getStatusLine().getStatusCode());
            httpInfo.setMessage(result.getStatusLine().getReasonPhrase());
        }

        return httpInfo;
    }
}
