
package io.jenkins.plugins.nirmata.model;

public class Status {

    private int _statusCode;
    private String _message;

    public Status(int statusCode, String message) {
        this._statusCode = statusCode;
        this._message = message;
    }

    public String getMessage() {
        return _message;
    }

    public void setMessage(String message) {
        this._message = message;
    }

    public int getStatusCode() {
        return _statusCode;
    }

    public void setStatusCode(int statusCode) {
        this._statusCode = statusCode;
    }

}
