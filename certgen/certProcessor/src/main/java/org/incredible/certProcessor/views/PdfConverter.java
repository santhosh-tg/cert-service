package org.incredible.certProcessor.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public class PdfConverter {

    private static Logger logger = LoggerFactory.getLogger(PdfConverter.class);

    public static void convertor(File sourceFile, String certUuid, String directory) {
        File file = new File(directory, certUuid + ".pdf");
//      File file = new File(directory, certUuid + "cert.png");
//      File file = new File(directory, certUuid + ".jpeg");
        try {
            //html to pdf convertion using headLess chrome
//            HeadlessChromeHtmlToPdfConverter.convert(sourceFile, file);

            //using Itext
            ItextHtmlToPdfConverter.convert(sourceFile,file);

            //svg to pdf
//            BatikSvgToPdfConverter.convert(sourceFile.getAbsolutePath(), file);

            //svg to png
//            BatikSvgToPngConverter.convert(sourceFile.getAbsolutePath(), file);

            //svg to jpeg
//            BatikSvgToJpegConverter.convert(sourceFile.getAbsolutePath(), file);
            logger.info("Pdf file is created for the {} ", certUuid);
        } catch (Exception e) {
            logger.error("exception while generating pdf file {}", e.getMessage());
        }
    }

}
