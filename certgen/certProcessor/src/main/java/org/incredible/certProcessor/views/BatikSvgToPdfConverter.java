package org.incredible.certProcessor.views;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.fop.svg.PDFTranscoder;
import org.apache.tika.metadata.PDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatikSvgToPdfConverter {

    private static Logger logger = LoggerFactory.getLogger(BatikSvgToPdfConverter.class);

    public static void convert(String svgFilePath, File pdfFile) throws TranscoderException, IOException {
        Long startTime = System.currentTimeMillis();
        logger.info("batik svg to pdf start time{}",startTime);
        String svgURIInput = Paths.get(svgFilePath).toUri().toURL().toString();
        OutputStream outputStream = new FileOutputStream(pdfFile);
        try {
            Transcoder pdfTranscoder = new PDFTranscoder();
            TranscoderInput input = new TranscoderInput(svgURIInput);
            TranscoderOutput output = new TranscoderOutput(outputStream);
            pdfTranscoder.addTranscodingHint(PDFTranscoder.KEY_AUTO_FONTS, false);
            pdfTranscoder.addTranscodingHint(PDFTranscoder.KEY_HEIGHT, 593.00f);
            pdfTranscoder.addTranscodingHint(PDFTranscoder.KEY_WIDTH, 841.50f);
            pdfTranscoder.addTranscodingHint(PDFTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, (25.4f / 72f));
//          pdfTranscoder.addTranscodingHint(PDFTranscoder.KEY_PIXEL_TO_MM, new Float(0.2645833f));

            pdfTranscoder.transcode(input, output);
            logger.info("batik svg to pdf end time{}", System.currentTimeMillis() - startTime);
        } finally {
            outputStream.close();
        }
    }
}
