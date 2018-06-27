
package io.jenkins.plugins.nirmata.action;

public enum ActionType {
    UPDATE_CAT_APP("Update App in Catalog"),
    UPDATE_ENV_APP("Update App in Environment"),
    DELETE_ENV_APP("Delete App in Environment"),
    DEPLOY_ENV_APP("Deploy App in Environment");

    private String _action;

    ActionType(String action) {
        this._action = action;
    }

    public String getAction() {
        return this._action;
    }

    @Override
    public String toString() {
        return this._action;
    }

    public static ActionType fromString(String action) {
        for (ActionType type : ActionType.values()) {
            if (type._action.equals(action)) {
                return type;
            }
        }

        return null;
    }
}
