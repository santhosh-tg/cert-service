package org.incredible.certProcessor.views;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;

import java.io.*;

public class PdfConverter {


    public static void convertor(File htmlSource, String id) {

        File file = new File(id + ".pdf");
        try {
            ConverterProperties converterProperties = new ConverterProperties();
            HtmlConverter.convertToPdf(new FileInputStream(htmlSource),
                    new FileOutputStream(file), converterProperties);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
