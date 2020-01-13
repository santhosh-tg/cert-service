package org.incredible.certProcessor.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class HeadlessChromeHtmlToPdfConverter {
    private static Logger logger = LoggerFactory.getLogger(HeadlessChromeHtmlToPdfConverter.class);

    public static void convert(File htmlFile, File pdfFile) {
        Process process = null;
        try {
            String operatingSystem = System.getProperty("os.name");
            boolean isWindows = operatingSystem.toLowerCase().startsWith("windows");
            boolean isMac = operatingSystem.contains("Mac OS X");
            String appInvokeCommand = "";
            String appArgs = "--no-sandbox --headless --print-to-pdf=" + pdfFile.getAbsolutePath() + " " + htmlFile.getAbsolutePath();
            Runtime rt = Runtime.getRuntime();
            logger.info("HeadlessChromeHtmlToPdfConverter: convert: operating system used is {}", operatingSystem);
            if (isWindows) {
                appInvokeCommand = "chromium-browser";
                process = rt.exec(new String[]{"cmd.exe", "/c", appInvokeCommand + " " + appArgs});
            } else if (isMac) {
                appInvokeCommand = "/Applications/Chromium.app/Contents/MacOS/Chromium";
                process = rt.exec(appInvokeCommand + " " + appArgs);
            } else {
                appInvokeCommand = "chromium-browser";
                process = rt.exec(new String[]{"sh", "-c", appInvokeCommand + " " + appArgs});
            }
            if(process.waitFor()==1){
                process.destroy();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
