package org.incredible.certProcessor.views;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class HTMLTemplateFile extends HTMLTemplateProvider {

    private String htmlTemplateName;

    private String content = null;

    private static Logger logger = LoggerFactory.getLogger(HTMLTemplateFile.class);


    public HTMLTemplateFile(String fileName) {
        htmlTemplateName = fileName;
    }

    private void fetchFile() {
        File htmlTemplateFile = new File(getPath(htmlTemplateName));
        try {
            content = FileUtils.readFileToString(htmlTemplateFile);
        } catch (IOException e) {
            logger.error("Exception while fetching  file {}", e.getMessage());

        }
    }

    @Override
    public String getTemplateContent(String filePath) {
        if (content == null) {
            fetchFile();
        }
        return content;
    }

    private static String getPath(String file) {
        ClassLoader loader = HtmlGenerator.class.getClassLoader();

        String result = null;
        try {
            result = loader.getResource(file).getFile();
        } catch (Exception e) {
            logger.error("Exception while retrieving the path of the file {}", e.getMessage());
        } finally {
            return result;
        }
    }

}

