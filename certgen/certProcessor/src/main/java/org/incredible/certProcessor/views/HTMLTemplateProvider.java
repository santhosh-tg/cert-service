package org.incredible.certProcessor.views;

import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.parser.node.ParserVisitor;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.runtime.visitor.BaseVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.cloud.storage.exception.StorageServiceException;


import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public abstract class HTMLTemplateProvider {

    private static Logger logger = LoggerFactory.getLogger(HTMLTemplateProvider.class);

    /**
     * variables present in html template
     */
    private static Set<String> htmlReferenceVariable = new HashSet<>();


    abstract public String getTemplateContent(String filePath) throws IOException, StorageServiceException;


    public static Boolean checkHtmlTemplateIsValid(String htmlString) {
        if (htmlString == null) {
            return false;
        } else {
            HTMLTemplateValidator htmlTemplateValidator = new HTMLTemplateValidator(storeAllHTMLTemplateVariables(htmlString));
            return htmlTemplateValidator.validate();
        }
    }

    /**
     * to check file is  exists or not
     *
     * @param file
     * @return
     */
    public static Boolean isFileExists(File file) {
        boolean isExits = false;
        if (file.exists()) {
            isExits = true;
        } else {
            isExits = false;
        }
        return isExits;
    }

    /**
     * to get all the reference variables present in htmlString
     *
     * @param htmlString html file read in the form of string
     * @return set of reference variables
     */
    public static Set<String> storeAllHTMLTemplateVariables(String htmlString) {
        RuntimeInstance runtimeInstance = new RuntimeInstance();
        SimpleNode node = null;
        try {
            node = runtimeInstance.parse(htmlString, null);
        } catch (ParseException e) {
            logger.debug("exception while storing template variables");
        }
        visitor.visit(node, null);
        return htmlReferenceVariable;
    }

    private static ParserVisitor visitor = new BaseVisitor() {
        @Override
        public Object visit(final ASTReference node, final Object data) {
            htmlReferenceVariable.add(node.literal());
            return null;
        }
    };

}

