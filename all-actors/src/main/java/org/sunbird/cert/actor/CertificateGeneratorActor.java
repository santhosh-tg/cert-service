package org.sunbird.cert.actor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.log4j.Logger;
import org.incredible.CertificateGenerator;
import org.incredible.UrlManager;
import org.incredible.certProcessor.CertModel;
import org.incredible.certProcessor.JsonKey;
import org.incredible.certProcessor.store.CertStoreFactory;
import org.incredible.certProcessor.store.ICertStore;
import org.incredible.certProcessor.store.StoreConfig;
import org.incredible.pojos.CertificateExtension;
import org.sunbird.*;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.cert.actor.operation.CertActorOperation;
import org.sunbird.cloud.storage.IStorageService;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;
import org.sunbird.message.IResponseMessage;
import org.sunbird.message.ResponseCode;
import org.sunbird.request.Request;
import org.sunbird.response.CertificateResponse;
import org.sunbird.response.Response;
import scala.Some;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This actor is responsible for certificate generation.
 *
 * @author manzarul
 */
@ActorConfig(
        tasks = {JsonKey.GENERATE_CERT, JsonKey.GET_SIGN_URL},
        asyncTasks = {}
)
public class CertificateGeneratorActor extends BaseActor {
    private Logger logger = Logger.getLogger(CertificateGeneratorActor.class);
    private static CertsConstant certVar = new CertsConstant();
    private ObjectMapper mapper = new ObjectMapper();
    String directory = "conf/";

    @Override
    public void onReceive(Request request) throws Throwable {
        String operation = request.getOperation();
        logger.info("onReceive method call start for operation " + operation);
        if (JsonKey.GENERATE_CERT.equalsIgnoreCase(operation)) {
            generateCertificate(request);
        } else if (CertActorOperation.GET_SIGN_URL.getOperation().equalsIgnoreCase(operation)) {
            generateSignUrl(request);
        }
        logger.info("onReceive method call End");
    }

    private void generateSignUrl(Request request) {
        try {
            logger.info("CertificateGeneratorActor:generateSignUrl:generate request got : ".concat(request.getRequest() + ""));
            String uri = UrlManager.getContainerRelativePath((String) request.getRequest().get(JsonKey.PDF_URL));
            logger.info("CertificateGeneratorActor:generateSignUrl:generate sign url method called for uri: ".concat(uri));
            IStorageService storageService = getStorageService();
            String signUrl = storageService.getSignedURL(certVar.getCONTAINER_NAME(), uri, Some.apply(getTimeoutInSeconds()),
                    Some.apply("r"));
            logger.info("CertificateGeneratorActor:generateSignUrl:signedUrl got: ".concat(signUrl));
            Response response = new Response();
            response.put(JsonKey.RESPONSE, JsonKey.SUCCESS);
            response.put(JsonKey.SIGNED_URL, signUrl);
            sender().tell(response, self());
        } catch (Exception e) {
            logger.error("CertificateGeneratorActor:generateSignUrl: error in genrerating sign url " + e);
            Response response = new Response();
            response.put(JsonKey.RESPONSE, "failure");
            response.put(JsonKey.SIGNED_URL, "");
            sender().tell(response, self());
        }

    }


    private IStorageService getStorageService() {
        StorageConfig storageConfig = new StorageConfig(certVar.getCloudStorageType(), certVar.getAzureStorageKey(), certVar.getAzureStorageSecret());
        logger.info("CertificateGeneratorActor:getStorageService:storage object formed:".concat(storageConfig.toString()));
        IStorageService storageService = StorageServiceFactory.getStorageService(storageConfig);
        return storageService;
    }

    private int getTimeoutInSeconds() {
        String timeoutInSecondsStr = CertsConstant.getExpiryLink(CertsConstant.DOWNLOAD_LINK_EXPIRY_TIMEOUT);
        logger.info("CertificateGeneratorActor:getTimeoutInSeconds:timeout got: ".concat(timeoutInSecondsStr));
        return Integer.parseInt(timeoutInSecondsStr);
    }

    private void generateCertificate(Request request) throws BaseException {
        logger.info("Request received==" + request.getRequest());
        Map<String, String> properties = populatePropertiesMap(request);

        CertStoreFactory certStoreFactory = new CertStoreFactory(properties);
        StoreConfig storeParams = new StoreConfig(getStorageParamsFromRequestOrEnv((Map<String, Object>) ((Map) request.get(JsonKey.CERTIFICATE)).get(JsonKey.STORE)));
       ICertStore certStore = certStoreFactory.getCertStore(storeParams, BooleanUtils.toBoolean(properties.get(JsonKey.PREVIEW)));
        String htmlTemplateUrl =  (String)((Map) request.get(JsonKey.CERTIFICATE)).get(JsonKey.HTML_TEMPLATE);

        CertMapper certMapper = new CertMapper(properties);
        List<CertModel> certModelList = certMapper.toList(request.getRequest());
        CertificateGenerator certificateGenerator = new CertificateGenerator(properties,directory);
        List<Map<String, Object>> certUrlList = new ArrayList<>();
        for (CertModel certModel : certModelList) {
            CertificateResponse certificateResponse = null;
            try {
                CertificateExtension certificateExtension = certificateGenerator.getCertificateExtension(certModel);
                Map<String,Object> qrMap = certificateGenerator.generateQrCode();
                String qrImageUrl = uploadQrCode((File)qrMap.get(JsonKey.QR_CODE_FILE),properties);

                String pdfLink = PdfGenerator.generate(htmlTemplateUrl,certificateExtension,qrImageUrl, getContainerName(storeParams),certStoreFactory.setCloudPath(storeParams));

                String uuid = certificateGenerator.getUUID(certificateExtension);
                String accessCode = (String)qrMap.get(JsonKey.ACCESS_CODE);
                String jsonData = certificateGenerator.generateCertificateJson();
                Map<String, Object> uploadRes = uploadJson(directory + uuid, certStore, certStoreFactory.setCloudPath(storeParams));

                certificateResponse = new CertificateResponse(uuid, accessCode , jsonData, certModel.getIdentifier(), pdfLink);
                certificateResponse.setJsonLink(properties.get(JsonKey.BASE_PATH).concat((String)uploadRes.get(JsonKey.JSON_URL)));
                certificateResponse.setPdfLink(properties.get(JsonKey.BASE_PATH).concat(certificateResponse.getPdfLink()));
                certUrlList.add(getResponse(certificateResponse));
            } catch (Exception ex) {
                logger.error("CertificateGeneratorActor:generateCertificate:Exception Occurred while generating certificate. : " + ex.getMessage());
                throw new BaseException(IResponseMessage.INTERNAL_ERROR, ex.getMessage(), ResponseCode.SERVER_ERROR.getCode());
            } finally {
                certStoreFactory.cleanUp(certificateResponse.getUuid(), directory);
            }
        }
        certStore.close();
        Response response = new Response();
        response.getResult().put("response", certUrlList);
        sender().tell(response, getSelf());
        logger.info("onReceive method call End");
    }

    private String uploadQrCode(File qrCodeFile,Map<String, String> properties) throws IOException {
        CertStoreFactory certStoreFactory = new CertStoreFactory(properties);
        QRStorageParams qrStorageParams = new QRStorageParams(certVar.getCloudStorageType());
        StoreConfig storeConfig = new StoreConfig(qrStorageParams.storeParams);
        ICertStore certStore = certStoreFactory.getCertStore(storeConfig, BooleanUtils.toBoolean(properties.get(JsonKey.PREVIEW)));
        String qrImageUrl = certStore.getPublicLink(qrCodeFile, certStoreFactory.setCloudPath(storeConfig));
        certStore.close();
        logger.info("QR code is created for the certificate : "+ qrCodeFile.getName() + " URL : " + qrImageUrl);
        return qrImageUrl;
    }

    private String getContainerName (StoreConfig storeParams) {
        String type = storeParams.getType();
        if (JsonKey.AZURE.equalsIgnoreCase(type)) {
            return storeParams.getAzureStoreConfig().getContainerName();
        } else {
            return storeParams.getAwsStoreConfig().getContainerName();
        }
    }

    private Map<String, Object> uploadJson(String fileName, ICertStore certStore, String cloudPath) throws IOException {
        certStore.init();
        Map<String, Object> resMap = new HashMap<>();
        File file = FileUtils.getFile(fileName.concat(".json"));
        resMap.put(JsonKey.JSON_URL, certStore.save(file, cloudPath));
        return resMap;
    }


    private Map<String, Object> getResponse(CertificateResponse certificateResponse) {
        Map<String, Object> resMap = new HashMap<>();
        resMap.put(JsonKey.UNIQUE_ID, certificateResponse.getUuid());
        resMap.put(JsonKey.RECIPIENT_ID, certificateResponse.getRecipientId());
        resMap.put(JsonKey.ACCESS_CODE, certificateResponse.getAccessCode());
        resMap.put(JsonKey.PDF_URL, certificateResponse.getPdfLink());
        resMap.put(JsonKey.JSON_URL, certificateResponse.getJsonLink());
        try {
            resMap.put(JsonKey.JSON_DATA, mapper.readValue(certificateResponse.getJsonData(), Map.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resMap;
    }

    private HashMap<String, String> populatePropertiesMap(Request request) {
        HashMap<String, String> properties = new HashMap<>();
        String tag = (String) ((Map) request.get(JsonKey.CERTIFICATE)).get(JsonKey.TAG);
        String preview = (String) ((Map<String, Object>) request.getRequest().get(JsonKey.CERTIFICATE)).get(JsonKey.PREVIEW);
        Map<String, Object> keysObject = (Map<String, Object>) ((Map) request.get(JsonKey.CERTIFICATE)).get(JsonKey.KEYS);
        certVar.setBasePath((String) ((Map<String, Object>) request.getRequest().get(JsonKey.CERTIFICATE))
                .get(JsonKey.BASE_PATH));
        if (MapUtils.isNotEmpty(keysObject)) {
            String keyId = (String) keysObject.get(JsonKey.ID);
            properties.put(JsonKey.KEY_ID, keyId);
            properties.put(JsonKey.SIGN_CREATOR, certVar.getSignCreator(keyId));
            properties.put(JsonKey.PUBLIC_KEY_URL, certVar.getPUBLIC_KEY_URL(keyId));
            logger.info("populatePropertiesMap: keys after".concat(keyId));
        }
        properties.put(JsonKey.TAG, tag);
        properties.put(JsonKey.CONTAINER_NAME, certVar.getCONTAINER_NAME());
        properties.put(JsonKey.BADGE_URL, certVar.getBADGE_URL(tag));
        properties.put(JsonKey.ISSUER_URL, certVar.getISSUER_URL());
        properties.put(JsonKey.CONTEXT, certVar.getCONTEXT());
        properties.put(JsonKey.VERIFICATION_TYPE, certVar.getVERIFICATION_TYPE());
        properties.put(JsonKey.ACCESS_CODE_LENGTH, certVar.getACCESS_CODE_LENGTH());
        properties.put(JsonKey.SIGN_URL, certVar.getEncSignUrl());
        properties.put(JsonKey.SIGN_VERIFY_URL, certVar.getEncSignVerifyUrl());
        properties.put(JsonKey.ENC_SERVICE_URL, certVar.getEncryptionServiceUrl());
        properties.put(JsonKey.SIGNATORY_EXTENSION, certVar.getSignatoryExtensionUrl());
        properties.put(JsonKey.SLUG, certVar.getSlug());
        properties.put(JsonKey.PREVIEW, certVar.getPreview(preview));
        properties.put(JsonKey.BASE_PATH, certVar.getBasePath());

        logger.info("CertificateGeneratorActor:getProperties:properties got from Constant File ".concat(Collections.singleton(properties.toString()) + ""));
        return properties;
    }

    private Map<String, Object> getStorageParamsFromRequestOrEnv(Map<String, Object> storeParams) {
        if (MapUtils.isNotEmpty(storeParams)) {
            return storeParams;
        } else {
            return certVar.getStorageParamsFromEvn();
        }
    }

}
