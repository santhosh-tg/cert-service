package org.sunbird;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.incredible.certProcessor.store.LocalStore;
import org.incredible.certProcessor.views.HTMLTemplateValidator;
import org.incredible.certProcessor.views.HTMLVarResolver;
import org.incredible.pojos.CertificateExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.message.IResponseMessage;
import org.sunbird.message.ResponseCode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SvgGenerator {

    private Logger logger = LoggerFactory.getLogger(SvgGenerator.class);
    private String svgTemplate;
    private String directory;
    private static Map<String, String> encoderMap = new HashMap<>();


    static {
        Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        Velocity.init();
        encoderMap.put("<", "%3C");
        encoderMap.put(">", "%3E");
        encoderMap.put("#", "%23");
        encoderMap.put("%", "%25");
        encoderMap.put("\"", "\'");
    }

    public SvgGenerator(String svgTemplate, String directory) {
        this.svgTemplate = svgTemplate;
        this.directory = directory;
    }

    public String generate(CertificateExtension certificateExtension, String encodedQrCode) throws BaseException {
        String svgFileName = getSvgFileName();
        String svgContent;
        File file = new File(directory + svgFileName);
        if (!file.exists()) {
            download(svgFileName);
        }
        svgContent = readSvgContent(file.getAbsolutePath());
        String encodedSvg = null;
        try {
            encodedSvg = "data:image/svg+xml," + replaceTemplateVars(encodeData(svgContent), certificateExtension, encodedQrCode);
            encodedSvg = encodedSvg.replaceAll("\n", "").replaceAll("\t", "");
        } catch (IOException e) {
            logger.info("SvgGenerator:generate exception while encoding svgContent {}", e.getMessage());
        }
        logger.info("svg template string creation completed");
        return encodedSvg;
    }


    private String replaceTemplateVars(String svgContent, CertificateExtension certificateExtension, String encodeQrCode) throws IOException {
        HTMLVarResolver htmlVarResolver = new HTMLVarResolver(certificateExtension);
        VelocityContext context = new VelocityContext();
        Set<String> htmlReferenceVariable = HTMLTemplateValidator.storeAllHTMLTemplateVariables(svgContent);
        for (String var : htmlReferenceVariable) {
            String macro = var.substring(1);
            try {
                Method method = htmlVarResolver.getClass().getMethod("get" + StringUtils.capitalize(macro));
                method.setAccessible(true);
                String data = URLEncoder.encode((String) method.invoke(htmlVarResolver), "UTF-8");
                // URLEncoder.encode replace space with "+", but it should %20
                context.put(macro, data.replace("+", "%20"));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | UnsupportedEncodingException e) {
                logger.info("exception while replacing TemplateVars of svg {}", e.getMessage());
            }
        }
        context.put("qrCodeImage", "data:image/png;base64," + encodeQrCode);
        StringWriter stringWriter = new StringWriter();
        Velocity.evaluate(context, stringWriter, "velocity", svgContent);
        stringWriter.flush();
        stringWriter.close();
        return stringWriter.toString();
    }

    private String encodeData(String data) {
        StringBuffer stringBuffer = new StringBuffer();
        if (StringUtils.isNotEmpty(data)) {
            Pattern pattern = Pattern.compile("[<>#%\"]");
            Matcher matcher = pattern.matcher(data);
            while (matcher.find()) {
                matcher.appendReplacement(stringBuffer, encoderMap.get(matcher.group()));
            }
            matcher.appendTail(stringBuffer);
        }
        return stringBuffer.toString();
    }

    private String readSvgContent(String path)  throws BaseException {
        FileInputStream fis;
        String svgContent = null;
        try {
            fis = new FileInputStream(path);
            svgContent = IOUtils.toString(fis, StandardCharsets.UTF_8);
            fis.close();
        } catch (IOException e) {
            logger.info("Exception occurred while reading svg content {}", path);
            throw new BaseException(IResponseMessage.INTERNAL_ERROR, e.getMessage(), ResponseCode.SERVER_ERROR.getCode());
        }
        return svgContent;
    }

    private void download(String fileName) {
        LocalStore localStore = new LocalStore("");
        try {
            localStore.get(svgTemplate, fileName, directory);
        } catch (IOException e) {
            logger.info("Exception while downloading svg template {}", e.getMessage());
        }
    }

    private String getSvgFileName() {
        String fileName = null;
        try {
            URI uri = new URI(svgTemplate);
            String path = uri.getPath();
            fileName = path.substring(path.lastIndexOf('/') + 1);
            if (!fileName.endsWith(".svg"))
                return fileName.concat(".svg");
        } catch (URISyntaxException e) {
            logger.debug("Exception while getting file name from template url : {}", e.getMessage());
        }
        return fileName;
    }


}
