package org.incredible.certProcessor.views;

import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.licensekey.LicenseKey;
import org.apache.commons.lang3.StringUtils;
import org.incredible.certProcessor.JsonKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class PdfConverter {

    private static Logger logger = LoggerFactory.getLogger(PdfConverter.class);

    public static void convertor(File htmlSource, String certUuid, String directory) {
        File file = new File(directory, certUuid + ".pdf");
        try {
            if(Boolean.parseBoolean(System.getenv(JsonKey.ITEXT_LICENSE_ENABLED)) &&
                    StringUtils.isNotEmpty(System.getenv(JsonKey.ITEXT_LICENSE_PATH))) {
                InputStream ip = PdfConverter.class.getResourceAsStream(System.getenv(JsonKey.ITEXT_LICENSE_PATH));
                LicenseKey.loadLicenseFile(ip);
                logger.info("license is loaded");
            }
            HtmlConverter.convertToPdf(htmlSource, file);
            logger.info("Pdf file is created ");
        } catch (FileNotFoundException e) {
            logger.error("exception while generating pdf file {}", e.getMessage());
        } catch (IOException e) {
            logger.error("exception while generating pdf file {}", e.getMessage());
        }
    }

}
