
package io.jenkins.plugins.nirmata.action;

public enum ActionType {
    UPDATE_CAT_APP("Update App in Catalog"),
    UPDATE_ENV_APP("Update App in Environment"),
    DELETE_ENV_APP("Delete App in Environment"),
    DEPLOY_ENV_APP("Deploy App in Environment");

    private String action;

    ActionType(String action) {
        this.action = action;
    }

    public String getAction() {
        return this.action;
    }

    @Override
    public String toString() {
        return this.action;
    }

    public static ActionType fromString(String action) {
        for (ActionType type : ActionType.values()) {
            if (type.action.equals(action)) {
                return type;
            }
        }

        return null;
    }
}
