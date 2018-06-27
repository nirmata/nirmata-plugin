
package io.jenkins.plugins.nirmata.action;

import java.util.List;

import com.google.common.base.Strings;

import hudson.AbortException;
import hudson.model.TaskListener;
import io.jenkins.plugins.nirmata.model.HTTPInfo;
import io.jenkins.plugins.nirmata.model.Model;
import io.jenkins.plugins.nirmata.util.FileOperations;
import io.jenkins.plugins.nirmata.util.LocalRepo;
import io.jenkins.plugins.nirmata.util.NirmataClient;

public final class Action {

    private final NirmataClient _client;
    private final TaskListener _listener;

    public Action(NirmataClient client, TaskListener listener) {
        this._client = client;
        this._listener = listener;
    }

    public NirmataClient getClient() {
        return _client;
    }

    public TaskListener getListener() {
        return _listener;
    }

    public void buildStep(ActionType type, String... vargs) throws AbortException {
        if (type.equals(ActionType.UPDATE_CAT_APP) && vargs.length == 4) {

            update(vargs[0], vargs[1], vargs[2], vargs[3]);
        } else if (type.equals(ActionType.UPDATE_ENV_APP) && vargs.length == 5) {

            update(vargs[0], vargs[1], vargs[2], vargs[3], vargs[4]);
        } else if (type.equals(ActionType.DEPLOY_ENV_APP) && vargs.length == 3) {

            deploy(vargs[0], vargs[1], vargs[2]);
        } else if (type.equals(ActionType.DELETE_ENV_APP) && vargs.length == 2) {

            delete(vargs[0], vargs[1]);
        } else {

            throw new AbortException("Unknown action, " + type.toString());
        }
    }

    private void printActionInfo(String... vargs) {
        _listener.getLogger().println();
        for (String varg : vargs) {
            _listener.getLogger().println(varg);
        }
        _listener.getLogger().println();
    }

    private void update(String catalog, String directories, String includes, String excludes) throws AbortException {
        printActionInfo("Action: " + ActionType.UPDATE_CAT_APP.toString(),
            "Catalog: " + catalog,
            "Directories: " + directories,
            "Includes: " + includes,
            "Excludes: " + excludes);

        String applicationId = null;
        List<Model> catalogApplications = _client.getAppsFromCatalog().getModel();

        if (catalogApplications != null && !catalogApplications.isEmpty()) {
            for (Model e : catalogApplications) {
                if (e.getName().equals(catalog)) {
                    applicationId = e.getId();
                }
            }
        } else {
            _listener.getLogger().println("ERROR: Catalog applications list is empty");
        }

        if (!Strings.isNullOrEmpty(applicationId)) {
            List<String> listOfDirectories = FileOperations.getList(directories);
            List<String> listOfFiles = LocalRepo.getFilesInDirectory(listOfDirectories, includes, excludes);
            String yamlStr = FileOperations.appendFiles(listOfFiles);

            HTTPInfo result = _client.updateAppsInCatalog(applicationId, yamlStr);
            printActionInfo(result.toString());
        } else {
            throw new AbortException(
                String.format("Unable to update application in catalog, {%s}. ApplicationId is null",
                    catalog));
        }
    }

    private void update(String environment, String application, String directories, String includes,
        String excludes) throws AbortException {
        printActionInfo("Action: " + ActionType.UPDATE_ENV_APP.toString(),
            "Environment: " + environment,
            "Application: " + application,
            "Directories: " + directories,
            "Includes: " + includes,
            "Excludes: " + excludes);

        String environmentId = null;
        List<Model> environments = _client.getEnvironments().getModel();

        if (environments != null && !environments.isEmpty()) {
            for (Model e : environments) {
                if (e.getName().equals(environment)) {
                    environmentId = e.getId();
                }
            }
        } else {
            _listener.getLogger().println("ERROR: Environments list is empty");
        }

        if (Strings.isNullOrEmpty(environmentId)) {
            throw new AbortException(
                String.format("Unable to update application, {%s}. EnvironmentId is null", application));
        }

        String applicationId = null;
        List<Model> applications = _client.getAppsFromEnvironment(environmentId).getModel();

        if (applications != null && !applications.isEmpty()) {
            for (Model e : applications) {
                if (e.getName().equals(application)) {
                    applicationId = e.getId();
                }
            }
        } else {
            _listener.getLogger().println("ERROR: Applications list is empty");
        }

        if (!Strings.isNullOrEmpty(applicationId)) {
            List<String> listOfDirectories = FileOperations.getList(directories);
            List<String> listOfFiles = LocalRepo.getFilesInDirectory(listOfDirectories, includes, excludes);
            String yamlStr = FileOperations.appendFiles(listOfFiles);

            HTTPInfo result = _client.updateAppsInEnvironment(applicationId, yamlStr);
            printActionInfo(result.toString());
        } else {
            throw new AbortException(
                String.format("Unable to update application, {%s}. ApplicationId is null", application));
        }
    }

    private void deploy(String environment, String catalog, String application) throws AbortException {
        printActionInfo("Action: " + ActionType.DEPLOY_ENV_APP.toString(),
            "Environment: " + environment,
            "Catalog: " + catalog,
            "Application: " + application);

        String applicationId = null;
        List<Model> catalogApplications = _client.getAppsFromCatalog().getModel();

        if (catalogApplications != null && !catalogApplications.isEmpty()) {
            for (Model e : catalogApplications) {
                if (e.getName().equals(catalog)) {
                    applicationId = e.getId();
                }
            }
        } else {
            _listener.getLogger().println("ERROR: Applications list is empty");
        }

        if (!Strings.isNullOrEmpty(applicationId)) {
            HTTPInfo result = _client.deployAppsInEnvironment(applicationId, environment, application);
            printActionInfo(result.toString());
        } else {
            throw new AbortException(
                String.format("Unable to depoly application in environment, {%s}. ApplicationId is null",
                    environment));
        }
    }

    private void delete(String environment, String application) throws AbortException {
        printActionInfo("Action: " + ActionType.DELETE_ENV_APP.toString(),
            "Environment: " + environment,
            "Application: " + application);

        String environmentId = null;
        List<Model> environments = _client.getEnvironments().getModel();

        if (environments != null && !environments.isEmpty()) {
            for (Model e : environments) {
                if (e.getName().equals(environment)) {
                    environmentId = e.getId();
                }
            }
        } else {
            _listener.getLogger().println("ERROR: Environments list is empty");
        }

        if (Strings.isNullOrEmpty(environmentId)) {
            throw new AbortException(
                String.format("Unable to delete application, {%s}. EnvironmentId is null", application));
        }

        String applicationId = null;
        List<Model> applications = _client.getAppsFromEnvironment(environmentId).getModel();

        if (applications != null && !applications.isEmpty()) {
            for (Model e : applications) {
                if (e.getName().equals(application)) {
                    applicationId = e.getId();
                }
            }
        } else {
            _listener.getLogger().println("ERROR: Applications list is empty");
        }

        if (!Strings.isNullOrEmpty(applicationId)) {
            HTTPInfo result = _client.deleteAppsFromEnvironment(applicationId);
            printActionInfo(result.toString());
        } else {
            throw new AbortException(
                String.format("Unable to delete application, {%s}. ApplicationId is null", application));
        }
    }
}
