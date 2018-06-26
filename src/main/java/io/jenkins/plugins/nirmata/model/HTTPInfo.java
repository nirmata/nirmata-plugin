
package io.jenkins.plugins.nirmata.model;

import static java.util.Optional.ofNullable;

import java.util.List;

import org.apache.http.Header;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HTTPInfo {

    private String uri;
    private String method;
    private int statusCode;
    private String message;
    private String entity;
    private String payload;
    private List<Header> headers;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int httpStatusCode) {
        this.statusCode = httpStatusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String httpMessage) {
        this.message = httpMessage;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @JsonIgnore
    public List<Header> getHeaders() {
        return headers;
    }

    @JsonProperty
    public void setHeaders(List<Header> hdrs) {
        headers = hdrs;
    }

    @Override
    public String toString() {
        return String.format(
            "Uri: %s,\nMethod: %s,\nStatusCode: %s,\nMessage: %s,\nEntity: %s,\nResult: %s", uri, method, statusCode,
            message, ofNullable(entity).orElse("<not applicable>"), payload);
    }
}
