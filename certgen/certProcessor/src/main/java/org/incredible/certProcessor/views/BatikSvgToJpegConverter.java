package org.incredible.certProcessor.views;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;

public class BatikSvgToJpegConverter {


    private static Logger logger = LoggerFactory.getLogger(BatikSvgToJpegConverter.class);

    public static void convert(String svgFilePath, File png) throws TranscoderException, IOException {
        Long startTime = System.currentTimeMillis();
        logger.info("batik svg to jpeg start time {}", startTime);
        JPEGTranscoder jpegTranscoder = new JPEGTranscoder();
        String svgURIInput = Paths.get(svgFilePath).toUri().toURL().toString();
        OutputStream outputStream = new FileOutputStream(png);
        try {
            TranscoderInput transcoderInput = new TranscoderInput(svgURIInput);
            TranscoderOutput transcoderOutput = new TranscoderOutput(outputStream);
            jpegTranscoder.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, 593.00f);
            jpegTranscoder.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, 841.50f);
            // KEY_QUALITY 0-1.0 with 1.0 being No Loss.  Value must be of type Float.  0.95 is 30% smaller and looks great.
            jpegTranscoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, 1.0f);
            jpegTranscoder.transcode(transcoderInput, transcoderOutput);
            logger.info("batik svg to jpeg end time {}", System.currentTimeMillis() - startTime);
        } finally {
            outputStream.close();
        }
    }

//    public static void main(String[] args) throws TranscoderException, IOException {
//        convert("/Users/aishwarya/workspace/cert-service/service/conf/0125450863553740809_File-01303624021545779237/cert.svg", new File("cert.jpeg"));
//    }

}

