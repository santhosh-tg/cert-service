package org.incredible.certProcessor.views;

import java.io.*;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeadlessChromeHtmlToPdfConverter {
    private static Logger logger = LoggerFactory.getLogger(HeadlessChromeHtmlToPdfConverter.class);

    public static void convert(File htmlFile, File pdfFile) {
        Process process = null;
        try {
            String operatingSystem = System.getProperty("os.name");
            boolean isWindows = operatingSystem.toLowerCase().startsWith("windows");
            boolean isMac = operatingSystem.contains("Mac OS X");
            Runtime rt = Runtime.getRuntime();
            if (isWindows) {
                process = rt.exec(new String[]{"cmd.exe", "/c", "chromium-browser --no-sandbox --headless --print-to-pdf=" + pdfFile.getAbsolutePath() + " " + htmlFile.getAbsolutePath()});
            } else if (isMac) {
                process = rt.exec("/Applications/Chromium.app/Contents/MacOS/Chromium --headless --print-to-pdf=" + pdfFile.getAbsolutePath() + " " + htmlFile.getAbsolutePath());
            } else {
                process = rt.exec(new String[]{"sh", "-c", "chromium-browser --no-sandbox --headless --print-to-pdf=" + pdfFile.getAbsolutePath() + " " + htmlFile.getAbsolutePath()});
            }
            logger.info("Input stream ::: " + convertInputStreamToString(process.getInputStream()));
            logger.info("Error stream ::: " + convertInputStreamToString(process.getErrorStream()));

        } catch (Exception e) {
            e.printStackTrace();
        }
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
