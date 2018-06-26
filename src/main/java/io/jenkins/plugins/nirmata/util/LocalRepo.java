
package io.jenkins.plugins.nirmata.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import hudson.FilePath;

public class LocalRepo {

    private static final Logger logger = LoggerFactory.getLogger(LocalRepo.class);

    private LocalRepo() {

    }

    public static List<String> getFilesInDirectory(List<String> directories, String includes, String excludes) {

        List<String> listOfFiles = new ArrayList<String>();
        String includesWithYamlnJson = String.format("*.yaml,*.yml,*.json%s",
            Strings.isNullOrEmpty(includes) ? "" : "," + includes);

        for (String directory : directories) {
            try {
                FilePath filePath = new FilePath(new File(directory));
                FilePath[] files = filePath.list(includesWithYamlnJson, excludes);

                for (FilePath file : files) {
                    listOfFiles.add(directory + "/" + file.getName());
                }
            } catch (Exception e) {
                logger.error("Error listing files, {}", e);
            }
        }

        return listOfFiles;
    }

}
