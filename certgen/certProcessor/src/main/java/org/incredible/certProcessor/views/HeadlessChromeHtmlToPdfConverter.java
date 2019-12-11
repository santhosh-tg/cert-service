package org.incredible.certProcessor.views;

import java.io.*;

public class HeadlessChromeHtmlToPdfConverter {

    public static void convert(File htmlFile,File pdfFile) {
        try {
            boolean isWindows = System.getProperty("os.name")
                    .toLowerCase().startsWith("windows");
            Runtime rt = Runtime.getRuntime();
                if (isWindows) {
                    rt.exec(new String[]{"cmd.exe", "/c", "google-chrome --headless --print-to-pdf="+pdfFile.getAbsolutePath()+" "+htmlFile.getAbsolutePath()});
                } else {
                    rt.exec(new String[]{"sh", "-c", "google-chrome --headless --print-to-pdf="+pdfFile.getAbsolutePath()+" "+htmlFile.getAbsolutePath()});
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //for(int i=0; i<40;i++) {
            File hFile = new File("/home/amit/sunbird/cert-service/service/conf/0125450863553740809_certificate", "index.html");
            File pFile = new File("/home/amit/sunbird/cert-service/service/conf/0125450863553740809_certificate", 43+".pdf");
            convert(hFile, pFile);
       // }
    }

}
