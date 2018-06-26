
package io.jenkins.plugins.nirmata.model;

import java.util.List;

public class Response {

    private Status status;
    private List<Model> model;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<Model> getModel() {
        return model;
    }

    public void setModel(List<Model> model) {
        this.model = model;
    }

}
