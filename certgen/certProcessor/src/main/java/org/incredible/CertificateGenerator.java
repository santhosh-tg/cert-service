package org.incredible;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import org.apache.commons.lang.StringUtils;
import org.incredible.certProcessor.CertModel;
import org.incredible.certProcessor.CertificateFactory;
import org.incredible.certProcessor.qrcode.AccessCodeGenerator;
import org.incredible.certProcessor.qrcode.QRCodeGenerationModel;
import org.incredible.certProcessor.views.HTMLGenerator;
import org.incredible.certProcessor.views.HTMLTemplateProvider;
import org.incredible.pojos.CertificateExtension;
import org.incredible.pojos.ob.exeptions.InvalidDateFormatException;
import org.incredible.certProcessor.qrcode.utils.QRCodeImageGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;


public class CertificateGenerator {


    private static Logger logger = LoggerFactory.getLogger(CertificateFactory.class);

    private Map<String, String> properties;
    private ObjectMapper objectMapper = new ObjectMapper();

    String directory = "conf/certificate/";

    public CertificateGenerator(Map<String, String> properties) {
        this.properties = properties;
    }

    private CertificateFactory certificateFactory = new CertificateFactory();


    public String createCertificate(CertModel certModel, HTMLTemplateProvider htmlTemplateProvider, String signatureConfig) throws InvalidDateFormatException {
        CertificateExtension certificateExtension = certificateFactory.createCertificate(certModel, properties, signatureConfig);
        generateCertificateJson(certificateExtension);
        generateQRCodeForCertificate(certificateExtension);
        if (htmlTemplateProvider.checkHtmlTemplateIsValid(htmlTemplateProvider.getTemplateContent())) {
            HTMLGenerator htmlGenerator = new HTMLGenerator(htmlTemplateProvider.getTemplateContent());
            htmlGenerator.generate(certificateExtension);
            return getUUID(certificateExtension.getId());
        } else return null;
    }

    private String getUUID(String id) {
        try {
            URI uri = new URI(id);
            String path = uri.getPath();
            String idStr = path.substring(path.lastIndexOf('/') + 1);
            return StringUtils.substringBefore(idStr, ".");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void generateCertificateJson(CertificateExtension certificateExtension) {
        File file = new File(directory + getUUID(certificateExtension.getId()) + ".json");
        checkDirectoryExists();
        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.writeValue(file, certificateExtension);
            logger.info("Json file has been generated for the certificate");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkDirectoryExists() {
        File file = new File("conf/certificate");
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private void generateQRCodeForCertificate(CertificateExtension certificateExtension) {
        AccessCodeGenerator accessCodeGenerator = new AccessCodeGenerator(Double.valueOf(properties.get("ACCESS_CODE_LENGTH")));
        QRCodeGenerationModel qrCodeGenerationModel = new QRCodeGenerationModel();
        qrCodeGenerationModel.setText(accessCodeGenerator.generate());
        qrCodeGenerationModel.setFileName(directory + getUUID(certificateExtension.getId()));
        qrCodeGenerationModel.setData(certificateExtension.getId());
        try {
            QRCodeImageGenerator qrCodeImageGenerator = new QRCodeImageGenerator();
            File Qrcode = qrCodeImageGenerator.createQRImages(qrCodeGenerationModel);
        } catch (IOException | WriterException | FontFormatException | NotFoundException e) {
            logger.error("Exception while generating QRcode {}", e.getMessage());
        }
    }
}