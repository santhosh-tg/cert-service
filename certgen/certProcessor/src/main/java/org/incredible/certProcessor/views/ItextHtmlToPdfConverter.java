package org.incredible.certProcessor.views;


import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.licensekey.LicenseKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class ItextHtmlToPdfConverter {

    private static Logger logger = LoggerFactory.getLogger(ItextHtmlToPdfConverter.class);

    public static void convert(File htmlSource, File pdfFile) {
        try {
            LicenseKey.loadLicenseFile("conf/itextkey1593594576874_0.xml");
            HtmlConverter.convertToPdf(htmlSource, pdfFile);
            logger.info("Pdf file is created ");
        } catch (FileNotFoundException e) {
            logger.error("exception while generating pdf file {}", e.getMessage());
        } catch (IOException e) {
            logger.error("exception while generating pdf file {}", e.getMessage());
        }
    }

}
