
package io.jenkins.plugins.nirmata.util;

import static com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;

import hudson.security.ACL;
import jenkins.model.Jenkins;

public class NirmataCredentials {

    private static final Logger logger = LoggerFactory.getLogger(NirmataCredentials.class);

    public List<StringCredentials> getCredentials() {
        return lookupCredentials(
            StringCredentials.class,
            Jenkins.getInstanceOrNull(),
            ACL.SYSTEM, Collections.<DomainRequirement> emptyList());
    }

    public Optional<StringCredentials> getCredential(String credentialId) {
        List<StringCredentials> idCredentials = getCredentials();
        CredentialsMatcher matcher = CredentialsMatchers.withId(credentialId);
        return Optional.ofNullable(CredentialsMatchers.firstOrNull(idCredentials, matcher));
    }
}
