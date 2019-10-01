package org.incredible.certProcessor.views;

import com.itextpdf.html2pdf.HtmlConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class PdfConverter {

    private static Logger logger = LoggerFactory.getLogger(PdfConverter.class);

    public static void convertor(File htmlSource, String certUuid, String directory) {
        File file = new File(directory, certUuid + ".pdf");
        try {
            HtmlConverter.convertToPdf(htmlSource, file);
            logger.info("Pdf file is created ");
        } catch (FileNotFoundException e) {
            logger.error("exception while generating pdf file {}", e.getMessage());
        } catch (IOException e) {
            logger.error("exception while generating pdf file {}", e.getMessage());
        }
    }

}
