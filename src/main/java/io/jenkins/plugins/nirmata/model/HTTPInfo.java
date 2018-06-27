
package io.jenkins.plugins.nirmata.model;

import static java.util.Optional.ofNullable;

import java.util.List;

import org.apache.http.Header;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HTTPInfo {

    private String _uri;
    private String _method;
    private int _statusCode;
    private String _message;
    private String _entity;
    private String _payload;
    private List<Header> _headers;

    public String getUri() {
        return _uri;
    }

    public void setUri(String uri) {
        this._uri = uri;
    }

    public String getMethod() {
        return _method;
    }

    public void setMethod(String method) {
        this._method = method;
    }

    public int getStatusCode() {
        return _statusCode;
    }

    public void setStatusCode(int httpStatusCode) {
        this._statusCode = httpStatusCode;
    }

    public String getMessage() {
        return _message;
    }

    public void setMessage(String httpMessage) {
        this._message = httpMessage;
    }

    public String getEntity() {
        return _entity;
    }

    public void setEntity(String entity) {
        this._entity = entity;
    }

    public String getPayload() {
        return _payload;
    }

    public void setPayload(String payload) {
        this._payload = payload;
    }

    @JsonIgnore
    public List<Header> getHeaders() {
        return _headers;
    }

    @JsonProperty
    public void setHeaders(List<Header> hdrs) {
        _headers = hdrs;
    }

    @Override
    public String toString() {
        return String.format(
            "Uri: %s,\nMethod: %s,\nStatusCode: %s,\nMessage: %s,\nEntity: %s,\nResult: %s", _uri, _method, _statusCode,
            _message, ofNullable(_entity).orElse("<not applicable>"), _payload);
    }
}
