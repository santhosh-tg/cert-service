package org.sunbird.cert.actor;

import java.io.File;

import org.apache.log4j.Logger;
import org.incredible.certProcessor.views.PdfConverter;
import org.sunbird.BaseActor;
import org.sunbird.JsonKey;
import org.sunbird.actor.core.ActorCache;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.request.Request;

import akka.actor.ActorRef;

@ActorConfig(
        tasks = {JsonKey.GENERATE_PDF},
        asyncTasks = {}
)
public class PDFGeneratorActor extends BaseActor {
    private Logger logger = Logger.getLogger(PDFGeneratorActor.class);
    @Override
    public void onReceive(Request request) throws Throwable {
        String operation = request.getOperation();
        logger.info("onReceive method call start for operation " + operation);
        if (JsonKey.GENERATE_PDF.equalsIgnoreCase(operation)) {
            generatePDFCertificate(request);
        }
        logger.info("onReceive method call End");
    }

    private void generatePDFCertificate(Request request)
    {
    	System.out.println("Inside PDFGeneratorActor");
        File file = new File((String)request.getRequest().get(JsonKey.DIRECTORY), request.getRequest().get(JsonKey.UUID) + ".html");
        PdfConverter.convertor(file, (String)request.getRequest().get(JsonKey.UUID), (String)request.getRequest().get(JsonKey.DIRECTORY));
        uploadCertificate(request);
    }

    private void uploadCertificate(Request request) {
    	Request req = new Request();
    	req.setOperation(JsonKey.UPLOAD_CERT);
    	req.getRequest().putAll(request.getRequest());
    	ActorCache.getActorRef(JsonKey.UPLOAD_CERT).tell(req, ActorRef.noSender());
    }
}
