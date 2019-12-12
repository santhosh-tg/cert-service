package org.incredible.certProcessor.views;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class HeadlessChromeHtmlToPdfConverter {

    public static void convert(File htmlFile,File pdfFile) {
        Process process = null;
        try {
            boolean isWindows = System.getProperty("os.name")
                    .toLowerCase().startsWith("windows");
            Runtime rt = Runtime.getRuntime();
                if (isWindows) {
                    process =  rt.exec(new String[]{"cmd.exe", "/c", "chromium-browser --no-sandbox --headless --print-to-pdf="+pdfFile.getAbsolutePath()+" "+htmlFile.getAbsolutePath()});
                } else {
                    process = rt.exec(new String[]{"sh", "-c", "chromium-browser --no-sandbox --headless --print-to-pdf="+pdfFile.getAbsolutePath()+" "+htmlFile.getAbsolutePath()});
                }
            System.out.println("Input stream ::: "+convertInputStreamToString(process.getInputStream()));
            System.out.println("Error stream ::: "+convertInputStreamToString(process.getErrorStream()));
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

    private static String convertInputStreamToString(InputStream inputStream)
            throws IOException {
        if (inputStream == null)
            return "Empty";
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        inputStream.close();
        return result.toString(StandardCharsets.UTF_8.name());
    }

}
