
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

    private final String actionType;
    private final String endpoint;
    private final String apikey;
    private final String environment;
    private final String application;
    private final String catalog;
    private final String runname;
    private final String deleteapp;
    private final String timeout;
    private final String directories;
    private final boolean includescheck;
    private final String includes;
    private final boolean excludescheck;
    private final String excludes;

    public String getActionType() {
        return actionType;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getApikey() {
        return apikey;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getApplication() {
        return application;
    }

    public String getCatalog() {
        return catalog;
    }

    public String getRunname() {
        return runname;
    }

    public String getDeleteapp() {
        return deleteapp;
    }

    public String getTimeout() {
        return timeout;
    }

    public String getDirectories() {
        return directories;
    }

    public boolean isIncludescheck() {
        return includescheck;
    }

    public String getIncludes() {
        return includes;
    }

    public boolean isExcludescheck() {
        return excludescheck;
    }

    public String getExcludes() {
        return excludes;
    }

    @DataBoundConstructor
    public NirmataBuilder(String actionType, String endpoint, String apikey, String environment, String application,
        String catalog, String runname, String deleteapp, String timeout, String directories, boolean includescheck,
        String includes, boolean excludescheck, String excludes) {
        this.actionType = actionType;
        this.endpoint = endpoint;
        this.apikey = apikey;
        this.environment = environment;
        this.application = application;
        this.catalog = catalog;
        this.runname = runname;
        this.deleteapp = deleteapp;
        this.timeout = timeout;
        this.directories = directories;
        this.includescheck = includescheck;
        this.includes = !includescheck ? null : includes;
        this.excludescheck = excludescheck;
        this.excludes = !excludescheck ? null : excludes;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener)
        throws InterruptedException, IOException {

        NirmataCredentials credentials = new NirmataCredentials();
        Optional<StringCredentials> credential = credentials.getCredential(apikey);
        NirmataClient client = new NirmataClient(endpoint, credential.get().getSecret().getPlainText());

        Action action = new Action(client, listener);

        if (ActionType.UPDATE_ENV_APP.getAction().equals(actionType)) {

            String appendedDirectoryPath = FileOperations.appendBasePath(workspace.getRemote(), directories);
            action.buildStep(ActionType.UPDATE_ENV_APP, environment, application, appendedDirectoryPath,
                !includescheck ? null : includes, !excludescheck ? null : excludes);
        } else if (ActionType.UPDATE_CAT_APP.getAction().equals(actionType)) {

            String appendedDirectoryPath = FileOperations.appendBasePath(workspace.getRemote(), directories);
            action.buildStep(ActionType.UPDATE_CAT_APP, catalog, appendedDirectoryPath,
                !includescheck ? null : includes, !excludescheck ? null : excludes);
        } else if (ActionType.DEPLOY_ENV_APP.getAction().equals(actionType)) {

            action.buildStep(ActionType.DEPLOY_ENV_APP, environment, catalog, runname);
        } else if (ActionType.DELETE_ENV_APP.getAction().equals(actionType)) {

            action.buildStep(ActionType.DELETE_ENV_APP, environment, deleteapp);
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

                return (status != null && status.getStatusCode() != HttpServletResponse.SC_ACCEPTED)
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

            if (status.getStatusCode() == HttpServletResponse.SC_ACCEPTED) {
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

                if (status.getStatusCode() == HttpServletResponse.SC_ACCEPTED) {
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

            if (status.getStatusCode() == HttpServletResponse.SC_ACCEPTED) {
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

                if (status.getStatusCode() == HttpServletResponse.SC_ACCEPTED) {
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