
package io.jenkins.plugins.nirmata.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

public class FileOperations {

    private static final Logger logger = LoggerFactory.getLogger(FileOperations.class);

    private FileOperations() {

    }

    public static List<String> getList(String listOfNames) {
        if (Strings.isNullOrEmpty(listOfNames)) {
            return null;
        }

        List<String> names = new ArrayList<>();
        for (String name : listOfNames.split(",")) {
            names.add(name.trim());
        }

        return names;
    }

    public static String appendFiles(List<String> files) {
        StringBuffer stringBuffer = new StringBuffer();

        try {
            for (String file : files) {
                String fileContent = readFile(file);
                stringBuffer.append(fileContent);
            }
        } catch (Exception e) {
            logger.error("Failed to read files, ", e);
        }

        return stringBuffer.toString();
    }

    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        return file.delete();
    }

    public static String writeToTempFile(String fileContent) {
        File tempFile = null;

        try {
            tempFile = File.createTempFile("tempfile", ".yaml");

            BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
            bw.write(fileContent);
            bw.close();
        } catch (Exception e) {
            logger.error("Failed to write to a temp file, {}", e);
        }

        return tempFile == null ? null : tempFile.getAbsolutePath();
    }

    public static String readFile(String fileName) throws Exception {
        String fileContent = null;

        try {
            fileContent = IOUtils.toString(new FileInputStream(fileName));
            logger.debug("Read file {} content: {}", fileName, fileContent);
        } catch (IOException e) {
            logger.error("Failed to read file {}: ", fileName, e);
            throw new RuntimeException(e);
        }

        return fileContent;
    }

    public static String appendBasePath(String basePath, String relativePath) {
        String appendedPath = null;
        if (!relativePath.startsWith(basePath)) {
            appendedPath = basePath.trim() + "/" + relativePath.trim();
        } else {
            appendedPath = relativePath.trim();
        }

        return appendedPath;
    }
}
