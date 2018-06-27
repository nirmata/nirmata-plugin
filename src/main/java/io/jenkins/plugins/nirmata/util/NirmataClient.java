
package io.jenkins.plugins.nirmata.util;

import java.util.List;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import hudson.AbortException;
import io.jenkins.plugins.nirmata.model.HTTPInfo;
import io.jenkins.plugins.nirmata.model.Model;
import io.jenkins.plugins.nirmata.model.Response;
import io.jenkins.plugins.nirmata.model.Status;

public class NirmataClient {

    private static final Logger logger = LoggerFactory.getLogger(NirmataClient.class);
    private static final String GET_ENV_API = "/environments/api/Environment?fields=name,id";
    private static final String GET_APPS_FROM_ENV_API = "/environments/api/Environment/%s/applications?fields=name,id";
    private static final String GET_APPS_FROM_CAT_API = "/catalog/api/applications?fields=name,id";
    private static final String DELETE_APPS_FROM_CAT_API = "/environments/api/applications/%s";
    private static final String DEPLOY_APPS_FROM_CAT_API = "/catalog/api/applications/%s/run";
    private static final String UPDATE_APPS_FROM_ENV_API = "/environments/api/applications/%s/import";
    private static final String UPDATE_APPS_FROM_CAT_API = "/catalog/api/Application/%s/import";
    private static final String CONTENT_YAML_TYPE = "text/yaml";
    private static final String CONTENT_JSON_TYPE = "application/json";
    private static final String NIRMATA_STR = "NIRMATA-API ";

    private String _endpoint;
    private String _apiKey;

    @SuppressWarnings("unused")
    private NirmataClient() {

    }

    public NirmataClient(String endpoint, String apiKey) {
        _endpoint = endpoint;
        _apiKey = apiKey;
    }

    public Response getEnvironments() {
        Response response = null;

        try {
            String uri = String.format("https://%s%s", _endpoint, GET_ENV_API);

            HTTPInfo httpInfo = HttpClient.doGet(uri, CONTENT_YAML_TYPE, NIRMATA_STR + _apiKey);
            response = getResponse(httpInfo);
        } catch (Exception e) {
            logger.error("Error encountered while getting environments, {}", e);
        }

        return response;
    }

    public Response getResponse(HTTPInfo httpInfo) {
        Response response = new Response();

        try {
            response.setStatus(new Status(httpInfo.getStatusCode(), httpInfo.getMessage()));
            if (httpInfo.getStatusCode() == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                response.setModel(
                    objectMapper.readValue(httpInfo.getPayload(), new TypeReference<List<Model>>() {}));
            }
        } catch (Exception e) {
            logger.error("Unable to read Response object", e);
        }

        return response;
    }

    public Response getAppsFromEnvironment(String environmentId) {
        Response response = null;

        try {
            String api = String.format(GET_APPS_FROM_ENV_API, environmentId);
            String uri = String.format("https://%s%s", _endpoint, api);

            HTTPInfo httpInfo = HttpClient.doGet(uri, CONTENT_YAML_TYPE, NIRMATA_STR + _apiKey);
            response = getResponse(httpInfo);
        } catch (Exception e) {
            logger.error("Error encountered while getting apps from environment, {}", e);
        }

        return response;
    }

    public Response getAppsFromCatalog() {
        Response response = null;

        try {
            String uri = String.format("https://%s%s", _endpoint, GET_APPS_FROM_CAT_API);

            HTTPInfo httpInfo = HttpClient.doGet(uri, CONTENT_YAML_TYPE, NIRMATA_STR + _apiKey);
            response = getResponse(httpInfo);
        } catch (Exception e) {
            logger.error("Error encountered while getting apps from catalog, {}", e);
        }

        return response;
    }

    public HTTPInfo deleteAppsFromEnvironment(String applicationId) throws AbortException {
        HTTPInfo httpInfo = null;

        try {
            String api = String.format(DELETE_APPS_FROM_CAT_API, applicationId);
            String uri = String.format("https://%s%s", _endpoint, api);

            httpInfo = HttpClient.doDelete(uri, CONTENT_YAML_TYPE, NIRMATA_STR + _apiKey);
        } catch (Exception e) {
            throw new AbortException(String.format(
                "Error encountered while deleleting app (%s) from environment with Exception (%s)", applicationId, e));
        }

        return httpInfo;
    }

    public HTTPInfo deployAppsInEnvironment(String applicationId, String envName, String appName)
        throws AbortException {
        HTTPInfo httpInfo = null;

        try {
            String payload = String.format("{" + "\"run\": \"%s\", " + "\"environment\": \"%s\"" + "}", appName,
                envName);
            StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_FORM_URLENCODED);

            String api = String.format(DEPLOY_APPS_FROM_CAT_API, applicationId);
            String uri = String.format("https://%s%s", _endpoint, api);

            httpInfo = HttpClient.doPost(uri, CONTENT_JSON_TYPE, NIRMATA_STR + _apiKey, entity);
        } catch (Exception e) {
            throw new AbortException(String.format(
                "Error encountered while deploying app (%s) in environment with Exception (%s)", applicationId, e));
        }

        return httpInfo;
    }

    public HTTPInfo updateAppsInEnvironment(String applicationId, String yamlStr)
        throws AbortException {
        HTTPInfo httpInfo = null;

        try {
            StringEntity entity = new StringEntity(yamlStr, ContentType.APPLICATION_OCTET_STREAM);
            entity.setChunked(true);

            String api = String.format(UPDATE_APPS_FROM_ENV_API, applicationId);
            String uri = String.format("https://%s%s", _endpoint, api);

            httpInfo = HttpClient.doPost(uri, CONTENT_YAML_TYPE, NIRMATA_STR + _apiKey, entity);
        } catch (Exception e) {
            throw new AbortException(String.format(
                "Error encountered while updating app (%s) in environment with Exception (%s)", applicationId, e));
        }

        return httpInfo;
    }

    public HTTPInfo updateAppsInCatalog(String applicationId, String yamlStr) throws AbortException {
        HTTPInfo httpInfo = null;

        try {
            StringEntity entity = new StringEntity(yamlStr, ContentType.APPLICATION_OCTET_STREAM);
            entity.setChunked(true);

            String api = String.format(UPDATE_APPS_FROM_CAT_API, applicationId);
            String uri = String.format("https://%s%s", _endpoint, api);

            httpInfo = HttpClient.doPost(uri, CONTENT_YAML_TYPE, NIRMATA_STR + _apiKey, entity);
        } catch (Exception e) {
            throw new AbortException(String
                .format("Error encountered while updating app (%s) in catalog with Exception (%s)", applicationId, e));
        }

        return httpInfo;
    }
}
