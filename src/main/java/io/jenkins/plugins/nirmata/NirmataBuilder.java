
package io.jenkins.plugins.nirmata;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.google.common.base.Strings;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ComboBoxModel;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import io.jenkins.plugins.nirmata.action.Action;
import io.jenkins.plugins.nirmata.action.ActionType;
import io.jenkins.plugins.nirmata.model.Model;
import io.jenkins.plugins.nirmata.model.Status;
import io.jenkins.plugins.nirmata.util.FileOperations;
import io.jenkins.plugins.nirmata.util.NirmataClient;
import io.jenkins.plugins.nirmata.util.NirmataCredentials;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;

public class NirmataBuilder extends Builder implements SimpleBuildStep {

    private static final Logger logger = LoggerFactory.getLogger(NirmataBuilder.class);

    private final String _actionType;
    private final String _endpoint;
    private final String _apikey;
    private final String _environment;
    private final String _application;
    private final String _catalog;
    private final String _runname;
    private final String _deleteapp;
    private final String _timeout;
    private final String _directories;
    private final boolean _includescheck;
    private final String _includes;
    private final boolean _excludescheck;
    private final String _excludes;

    public String getActionType() {
        return _actionType;
    }

    public String getEndpoint() {
        return _endpoint;
    }

    public String getApikey() {
        return _apikey;
    }

    public String getEnvironment() {
        return _environment;
    }

    public String getApplication() {
        return _application;
    }

    public String getCatalog() {
        return _catalog;
    }

    public String getRunname() {
        return _runname;
    }

    public String getDeleteapp() {
        return _deleteapp;
    }

    public String getTimeout() {
        return _timeout;
    }

    public String getDirectories() {
        return _directories;
    }

    public boolean isIncludescheck() {
        return _includescheck;
    }

    public String getIncludes() {
        return _includes;
    }

    public boolean isExcludescheck() {
        return _excludescheck;
    }

    public String getExcludes() {
        return _excludes;
    }

    @DataBoundConstructor
    public NirmataBuilder(String actionType, String endpoint, String apikey, String environment, String application,
        String catalog, String runname, String deleteapp, String timeout, String directories, boolean includescheck,
        String includes, boolean excludescheck, String excludes) {
        this._actionType = actionType;
        this._endpoint = endpoint;
        this._apikey = apikey;
        this._environment = environment;
        this._application = application;
        this._catalog = catalog;
        this._runname = runname;
        this._deleteapp = deleteapp;
        this._timeout = timeout;
        this._directories = directories;
        this._includescheck = includescheck;
        this._includes = !includescheck ? null : includes;
        this._excludescheck = excludescheck;
        this._excludes = !excludescheck ? null : excludes;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
        throws InterruptedException, IOException {

        NirmataCredentials credentials = new NirmataCredentials();
        Optional<StringCredentials> credential = credentials.getCredential(_apikey);
        NirmataClient client = new NirmataClient(_endpoint, credential.get().getSecret().getPlainText());

        Action action = new Action(client, listener);

        if (ActionType.UPDATE_ENV_APP.getAction().equals(_actionType)) {

            String appendedDirectoryPath = FileOperations.appendBasePath(workspace.getRemote(), _directories);
            action.buildStep(ActionType.UPDATE_ENV_APP, _environment, _application, appendedDirectoryPath,
                !_includescheck ? null : _includes, !_excludescheck ? null : _excludes);
        } else if (ActionType.UPDATE_CAT_APP.getAction().equals(_actionType)) {

            String appendedDirectoryPath = FileOperations.appendBasePath(workspace.getRemote(), _directories);
            action.buildStep(ActionType.UPDATE_CAT_APP, _catalog, appendedDirectoryPath,
                !_includescheck ? null : _includes, !_excludescheck ? null : _excludes);
        } else if (ActionType.DEPLOY_ENV_APP.getAction().equals(_actionType)) {

            action.buildStep(ActionType.DEPLOY_ENV_APP, _environment, _catalog, _runname);
        } else if (ActionType.DELETE_ENV_APP.getAction().equals(_actionType)) {

            action.buildStep(ActionType.DELETE_ENV_APP, _environment, _deleteapp);
        } else {

            throw new AbortException("Unknown action request!");
        }
    }

    @Symbol("nirmata")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private static final NirmataCredentials credentials = new NirmataCredentials();
        private static List<Model> environments, applications, catalogApplications;
        private static Status status;

        public FormValidation doCheckEndpoint(@QueryParameter String endpoint) {
            if (!Strings.isNullOrEmpty(endpoint)) {
                NirmataClient client = new NirmataClient(endpoint, null);
                status = client.getEnvironments().getStatus();

                return (status != null && status.getStatusCode() != HttpServletResponse.SC_UNAUTHORIZED)
                    ? FormValidation.error(String.format("%s (%s)", status.getMessage(), status.getStatusCode()))
                    : FormValidation.ok();
            } else {
                return FormValidation.warning("Endpoint is required");
            }
        }

        public FormValidation doCheckApikey(@QueryParameter String endpoint, @QueryParameter String apikey) {
            if (!Strings.isNullOrEmpty(apikey) && credentials.getCredential(apikey).isPresent()) {
                Optional<StringCredentials> credential = credentials.getCredential(apikey);
                NirmataClient client = new NirmataClient(endpoint, credential.get().getSecret().getPlainText());
                status = client.getEnvironments().getStatus();

                return (status != null && status.getStatusCode() != HttpServletResponse.SC_OK)
                    ? FormValidation.error(String.format("%s (%s)", status.getMessage(), status.getStatusCode()))
                    : FormValidation.ok();
            } else {
                return FormValidation.warning("API key is required");
            }
        }

        public FormValidation doCheckTimeout(@QueryParameter int timeout) {
            return timeout >= 0 ? FormValidation.ok() : FormValidation.error("Timeout cannot be less than 0");
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.NirmataBuilder_DescriptorImpl_DisplayName();
        }

        private int lastEditorId = 0;

        @JavaScriptMethod
        public synchronized String createEditorId() {

            return String.valueOf(lastEditorId++);

        }

        public ListBoxModel doFillApikeyItems() {
            if (!Jenkins.getInstanceOrNull().hasPermission(Jenkins.ADMINISTER)) {
                return new ListBoxModel();
            }

            List<StringCredentials> stringCredentials = credentials.getCredentials();
            return new StandardListBoxModel().includeEmptyValue().withAll(stringCredentials);
        }

        public ListBoxModel doFillEnvironmentItems(@QueryParameter String endpoint, @QueryParameter String apikey) {
            ListBoxModel models = new ListBoxModel();
            if (Strings.isNullOrEmpty(endpoint) || Strings.isNullOrEmpty(apikey)) {
                return models;
            }

            Optional<StringCredentials> credential = credentials.getCredential(apikey);
            NirmataClient client = new NirmataClient(endpoint, credential.get().getSecret().getPlainText());
            environments = client.getEnvironments().getModel();
            status = client.getEnvironments().getStatus();

            if (status.getStatusCode() == HttpServletResponse.SC_OK) {
                if (environments != null) {
                    for (Model model : environments) {
                        models.add(model.getName());
                    }
                } else {
                    models.add(new ListBoxModel.Option("--- No environments found ---", null, false));
                }
            }

            return models;
        }

        public ListBoxModel doFillApplicationItems(@QueryParameter String endpoint, @QueryParameter String apikey,
            @QueryParameter String environment) {
            String environmentId = null;

            ListBoxModel models = new ListBoxModel();
            if (Strings.isNullOrEmpty(endpoint) || Strings.isNullOrEmpty(apikey) || Strings.isNullOrEmpty(environment)
                || environments == null) {
                return models;
            }

            for (Model model : environments) {
                if (model.getName().equals(environment)) {
                    environmentId = model.getId();
                }
            }

            if (!Strings.isNullOrEmpty(environmentId)) {
                Optional<StringCredentials> credential = credentials.getCredential(apikey);
                NirmataClient client = new NirmataClient(endpoint, credential.get().getSecret().getPlainText());
                applications = client.getAppsFromEnvironment(environmentId).getModel();
                status = client.getAppsFromEnvironment(environmentId).getStatus();

                if (status.getStatusCode() == HttpServletResponse.SC_OK) {
                    if (!(applications == null || applications.isEmpty())) {
                        for (Model model : applications) {
                            models.add(model.getName());
                        }
                    } else {
                        models.add(new ListBoxModel.Option("--- No applications found ---", null, false));
                    }
                }
            }

            return models;
        }

        public ListBoxModel doFillCatalogItems(@QueryParameter String endpoint, @QueryParameter String apikey) {
            ListBoxModel models = new ListBoxModel();
            if (Strings.isNullOrEmpty(endpoint) || Strings.isNullOrEmpty(apikey)) {
                return models;
            }

            Optional<StringCredentials> credential = credentials.getCredential(apikey);
            NirmataClient client = new NirmataClient(endpoint, credential.get().getSecret().getPlainText());
            catalogApplications = client.getAppsFromCatalog().getModel();
            status = client.getAppsFromCatalog().getStatus();

            if (status.getStatusCode() == HttpServletResponse.SC_OK) {
                if (catalogApplications != null) {
                    for (Model model : catalogApplications) {
                        models.add(model.getName());
                    }
                } else {
                    models.add(new ListBoxModel.Option("--- No catalogs found ---", null, false));
                }
            }

            return models;
        }

        public ComboBoxModel doFillDeleteappItems(@QueryParameter String endpoint, @QueryParameter String apikey,
            @QueryParameter String environment) {
            String environmentId = null;

            ComboBoxModel models = new ComboBoxModel();
            if (Strings.isNullOrEmpty(endpoint) || Strings.isNullOrEmpty(apikey) || Strings.isNullOrEmpty(environment)
                || environments == null) {
                return models;
            }

            for (Model model : environments) {
                if (model.getName().equals(environment)) {
                    environmentId = model.getId();
                }
            }

            if (!Strings.isNullOrEmpty(environmentId)) {
                Optional<StringCredentials> credential = credentials.getCredential(apikey);
                NirmataClient client = new NirmataClient(endpoint, credential.get().getSecret().getPlainText());
                applications = client.getAppsFromEnvironment(environmentId).getModel();
                status = client.getAppsFromEnvironment(environmentId).getStatus();

                if (status.getStatusCode() == HttpServletResponse.SC_OK) {
                    if (!(applications == null || applications.isEmpty())) {
                        for (Model model : applications) {
                            models.add(model.getName());
                        }
                    }
                }
            }

            return models;
        }

    }

}