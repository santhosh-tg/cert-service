package org.incredible.certProcessor.views;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;

public class BatikSvgToPngConverter {

    private static Logger logger = LoggerFactory.getLogger(BatikSvgToPngConverter.class);

    public static void convert(String svgFilePath, File png) throws TranscoderException, IOException {
        Long startTime = System.currentTimeMillis();
        logger.info("batik svg to png start time {}", startTime);
        PNGTranscoder pngTranscoder = new PNGTranscoder();
        String svgURIInput = Paths.get(svgFilePath).toUri().toURL().toString();
        OutputStream outputStream = new FileOutputStream(png);
        try {
            TranscoderInput transcoderInput = new TranscoderInput(svgURIInput);
            TranscoderOutput transcoderOutput = new TranscoderOutput(outputStream);
            pngTranscoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, 593.00f);
            pngTranscoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, 841.50f);
            pngTranscoder.addTranscodingHint(PNGTranscoder.KEY_BACKGROUND_COLOR, Color.WHITE);
            pngTranscoder.addTranscodingHint(PNGTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, (25.4f / 72f));
            pngTranscoder.addTranscodingHint(PNGTranscoder.KEY_PIXEL_TO_MM, new Float(0.2645833f));
            pngTranscoder.transcode(transcoderInput, transcoderOutput);

            logger.info("batik svg to png end time {}", System.currentTimeMillis() - startTime);
        } finally {
            outputStream.close();
        }
    }

//    public static void main(String[] args) throws TranscoderException, IOException {
//        convert("/Users/aishwarya/workspace/cert-service/service/conf/0125450863553740809_File-01303624021545779237/cert.svg", new File("testpng.png"));
//    }

}
