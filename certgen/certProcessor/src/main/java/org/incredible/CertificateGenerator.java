package org.incredible;

import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import org.incredible.certProcessor.CertModel;
import org.incredible.certProcessor.CertificateFactory;
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
import java.util.HashMap;


public class CertificateGenerator {


    /**
     * context file url
     **/
    private final String context;

    private static Logger logger = LoggerFactory.getLogger(CertificateFactory.class);

    private HashMap<String, String> properties;

    public CertificateGenerator(String context, HashMap<String, String> properties) {
        this.context = context;
        this.properties = properties;
    }

    private CertificateFactory certificateFactory = new CertificateFactory();


    public String createCertificate(CertModel certModel, HTMLTemplateProvider htmlTemplateProvider) throws InvalidDateFormatException {
        CertificateExtension certificateExtension = certificateFactory.createCertificate(certModel, context, properties);
        generateQRCodeForCertificate(certificateExtension);
        if (htmlTemplateProvider.checkHtmlTemplateIsValid(htmlTemplateProvider.getTemplateContent())) {
            HTMLGenerator htmlGenerator = new HTMLGenerator(htmlTemplateProvider.getTemplateContent());
            htmlGenerator.generate(certificateExtension);
            return certificateExtension.getId().split("Certificate/")[1];
        } else return null;
    }

    private void generateQRCodeForCertificate(CertificateExtension certificateExtension) {
        String id = certificateExtension.getId().split("Certificate/")[1];
        File Qrcode;
        //todo generate n digit code
        QRCodeGenerationModel qrCodeGenerationModel = new QRCodeGenerationModel();
        qrCodeGenerationModel.setText("123456");
        qrCodeGenerationModel.setFileName(id);
        qrCodeGenerationModel.setData(certificateExtension.getId() + ".json");
        try {
            Qrcode = QRCodeImageGenerator.createQRImages(qrCodeGenerationModel);

        } catch (IOException | WriterException | FontFormatException | NotFoundException e) {
            logger.error("Exception while generating QRcode {}", e.getMessage());
        }

    }
}
