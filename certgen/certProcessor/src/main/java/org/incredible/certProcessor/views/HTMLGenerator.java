package org.incredible.certProcessor.views;

import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.incredible.pojos.CertificateExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;


public class HTMLGenerator {

    private static Logger logger = LoggerFactory.getLogger(HTMLGenerator.class);

    private String htmlString;

    private Set<String> htmlReferenceVariable;


    public HTMLGenerator(String htmlString) {
        this.htmlString = htmlString;
        htmlReferenceVariable = HTMLTemplateProvider.storeAllHTMLTemplateVariables(this.htmlString);
    }


    public void initVelocity() {
        Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        Velocity.init();
    }

    /**
     * create velocity context  where Velocity.init() called once
     *
     * @param certificateExtension
     */

    public void generate(CertificateExtension certificateExtension, String directory) {
        initVelocity();
        VelocityContext context = new VelocityContext();
        HTMLVarResolver htmlVarResolver = new HTMLVarResolver(certificateExtension);
        htmlReferenceVariable = HTMLTemplateProvider.storeAllHTMLTemplateVariables(htmlString);
        Iterator<String> iterator = htmlReferenceVariable.iterator();
        while (iterator.hasNext()) {
            String macro = iterator.next().substring(1);
            try {
                Method method = htmlVarResolver.getClass().getMethod("get" + capitalize(macro));
                method.setAccessible(true);
                context.put(macro, method.invoke(htmlVarResolver));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                logger.info("exception while generating html for certificate {}", e.getMessage());
            }
        }
        createHTMLFile(context, getUUID(certificateExtension.getId()), directory);
    }

    private String getUUID(String certId) {
        try {
            URI uri = new URI(certId);
            String path = uri.getPath();
            String idStr = path.substring(path.lastIndexOf('/') + 1);
            return StringUtils.substringBefore(idStr, ".");
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private void createHTMLFile(VelocityContext context, String certUuid, String directory) {
        try {
            File file = new File(directory, certUuid + ".html");
            Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            Velocity.evaluate(context, writer, "velocity", htmlString);
            writer.flush();
            writer.close();
            logger.info("html file is created {}", file.getName());
            PdfConverter.convertor(file, certUuid, directory);
        } catch (IOException e) {
            logger.error("IO exception while creating html file :{}", e.getMessage());
        }
    }

    private String capitalize(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }


}