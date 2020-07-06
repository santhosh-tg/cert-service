package org.sunbird.cert.actor;


import org.apache.log4j.Logger;
import org.incredible.certProcessor.JsonKey;
import org.incredible.certProcessor.store.LocalStore;
import org.incredible.certProcessor.views.HTMLTemplateValidator;
import org.incredible.certProcessor.views.HTMLTemplateZip;
import org.sunbird.BaseActor;
import org.sunbird.BaseException;
import org.sunbird.CertsConstant;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.cloud.storage.exception.StorageServiceException;
import org.sunbird.message.IResponseMessage;
import org.sunbird.message.ResponseCode;
import org.sunbird.request.Request;
import org.sunbird.response.Response;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Set;

/**
 * @author Aishwarya
 * This actor is responsible for Template validation
 */
@ActorConfig(
  dispatcher = "cert-dispatcher",
  tasks = {JsonKey.VALIDATE_TEMPLATE},
  asyncTasks = {}
)
public class TemplateValidateActor extends BaseActor {

    private Logger logger = Logger.getLogger(TemplateValidateActor.class);
    private CertsConstant certsConstant = new CertsConstant();

    @Override
    public void onReceive(Request request) throws Throwable {
        String operation = request.getOperation();
        logger.info("onReceive method call start for operation " + operation);
        if (JsonKey.VALIDATE_TEMPLATE.equalsIgnoreCase(operation)) {
            validateTemplate(request);
        }
    }


    private void validateTemplate(Request request) throws BaseException {
        HTMLValidatorResponse validatorResponse;
        String templateUrl = (String) request.getRequest().get(JsonKey.TEMPLATE_URL);
        HTMLTemplateZip htmlTemplateZip = new HTMLTemplateZip(new LocalStore(certsConstant.getDOMAIN_URL()), templateUrl);

        try {
            htmlTemplateZip.download(); //download the file
            //check if zip file downloaded or not ,if downloaded unzip
            if (Boolean.TRUE.equals(htmlTemplateZip.isZipFileExists())) {
                htmlTemplateZip.unzip();
                if (Boolean.TRUE.equals(htmlTemplateZip.isIndexHTMlFileExits())) {
                    validatorResponse = validateHtml(htmlTemplateZip);
                } else {
                    throw new BaseException("INVALID_ZIP_FILE", MessageFormat.format(IResponseMessage.INVALID_ZIP_FILE, ":zip file format is invalid, unable to find file index.html"), ResponseCode.BAD_REQUEST.getCode());
                }
            } else {
                throw new BaseException("INVALID_TEMPLATE_URL", MessageFormat.format(IResponseMessage.INVALID_TEMPLATE_URL, ": unable to download zip file , please provide valid url"), ResponseCode.BAD_REQUEST.getCode());
            }
        } catch (StorageServiceException e) {
            logger.info("exception while downloading " + e.getMessage());
            throw new BaseException(IResponseMessage.INTERNAL_ERROR, e.getMessage(), ResponseCode.SERVER_ERROR.getCode());
        } catch (IOException e) {
            logger.info("exception while unzipping " + e.getMessage());
            throw new BaseException("INVALID_ZIP_FILE", MessageFormat.format(IResponseMessage.INVALID_ZIP_FILE, ": exception while unzipping " + e.getMessage()), ResponseCode.BAD_REQUEST.getCode());
        } finally {
            htmlTemplateZip.cleanUp();
        }

        Response response = new Response();
        response.getResult().put("response", validatorResponse);
        sender().tell(response, getSelf());
        logger.info("onReceive method call End");
    }

    private HTMLValidatorResponse validateHtml(HTMLTemplateZip htmlTemplateZip) {
        HTMLValidatorResponse validatorResponse = new HTMLValidatorResponse();
        String htmlContent;
        try {
            htmlContent = htmlTemplateZip.getTemplateContent();
            HTMLTemplateValidator htmlTemplateValidator = new HTMLTemplateValidator(htmlContent);
            Set<String> invalidVars = htmlTemplateValidator.validate();
            if (invalidVars.isEmpty()) {
                logger.info(String.format("given template %s is valid", htmlTemplateZip.getTemplateUrl()));
                validatorResponse.setValid(true);
            } else {
                validatorResponse.setValid(false);
                validatorResponse.setMessage(invalidVars);
                logger.info(String.format("given template %s is invalid , it contains invalid variables : %s ", htmlTemplateZip.getTemplateUrl(), invalidVars));
            }
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
        return validatorResponse;
    }

}
