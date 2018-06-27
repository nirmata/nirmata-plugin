
package io.jenkins.plugins.nirmata.model;

import java.util.List;

public class Response {

    private Status _status;
    private List<Model> _model;

    public Status getStatus() {
        return _status;
    }

    public void setStatus(Status status) {
        this._status = status;
    }

    public List<Model> getModel() {
        return _model;
    }

    public void setModel(List<Model> model) {
        this._model = model;
    }

}
