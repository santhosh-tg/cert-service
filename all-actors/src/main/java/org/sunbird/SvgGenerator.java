package org.sunbird;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringSubstitutor;
import org.incredible.certProcessor.store.LocalStore;
import org.incredible.certProcessor.views.HTMLVarResolver;
import org.incredible.pojos.CertificateExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.message.IResponseMessage;
import org.sunbird.message.ResponseCode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SvgGenerator {

    private Logger logger = LoggerFactory.getLogger(SvgGenerator.class);
    private String svgTemplate;
    private String directory;
    private static Map<String, String> encoderMap = new HashMap<>();
    private static Map<String, String> cachedSvgTemplates = new HashMap<>();


    static {
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
            logger.info("{} file does not exits , downloading", svgFileName);
            download(svgFileName);
            svgContent = readSvgContent(file.getAbsolutePath());
            String encodedSvg = "data:image/svg+xml," + encodeData(svgContent);
            encodedSvg = encodedSvg.replaceAll("\n", "").replaceAll("\t", "");
            cachedSvgTemplates.put(this.svgTemplate, encodedSvg);
        }
        logger.info("svg template is cached {}", cachedSvgTemplates.containsKey(this.svgTemplate));
        String svgData = replaceTemplateVars(cachedSvgTemplates.get(this.svgTemplate), certificateExtension, encodedQrCode);
        logger.info("svg template string creation completed");
        return svgData;
    }


    private String replaceTemplateVars(String svgContent, CertificateExtension certificateExtension, String encodeQrCode) {
        HTMLVarResolver htmlVarResolver = new HTMLVarResolver(certificateExtension);
        Map<String, String> certData = htmlVarResolver.getCertMetaData();
        certData.put("qrCodeImage", "data:image/png;base64," + encodeQrCode);
        StringSubstitutor sub = new StringSubstitutor(certData);
        String resolvedString = sub.replace(svgContent);
        logger.info("replacing temp vars completed");
        return resolvedString;
    }

    private String encodeData(String data) {
        StringBuffer stringBuffer = new StringBuffer();
        Pattern pattern = Pattern.compile("[<>#%\"]");
        Matcher matcher = pattern.matcher(data);
        while (matcher.find()) {
            matcher.appendReplacement(stringBuffer, encoderMap.get(matcher.group()));
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    private String readSvgContent(String path) throws BaseException {
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
