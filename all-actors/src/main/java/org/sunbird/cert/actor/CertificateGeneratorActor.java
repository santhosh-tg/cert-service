package org.sunbird.cert.actor;

import com.fasterxml.jackson.core.type.TypeReference;
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
import org.sunbird.BaseActor;
import org.sunbird.BaseException;
import org.sunbird.CertMapper;
import org.sunbird.CertsConstant;
import org.sunbird.PdfGenerator;
import org.sunbird.QRStorageParams;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.cert.actor.operation.CertActorOperation;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;
import org.sunbird.message.IResponseMessage;
import org.sunbird.message.ResponseCode;
import org.sunbird.request.Request;
import org.sunbird.response.CertificateResponse;
import org.sunbird.response.CertificateResponseV1;
import org.sunbird.response.CertificateResponseV2;
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
  dispatcher = "cert-dispatcher",
  tasks = {JsonKey.GENERATE_CERT, JsonKey.GET_SIGN_URL, JsonKey.GENERATE_CERT_V2},
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
        } else if (CertActorOperation.GENERATE_CERTIFICATE_V2.getOperation().equalsIgnoreCase(operation)) {
            generateCertificateV2(request);
        }
        logger.info("onReceive method call End");
    }

    private void generateSignUrl(Request request) {
      BaseStorageService storageService = null;
        try {
            logger.info("CertificateGeneratorActor:generateSignUrl:generate request got : ".concat(request.getRequest() + ""));
            storageService = getStorageService();
            String uri = UrlManager.getContainerRelativePath((String) request.getRequest().get(JsonKey.PDF_URL));
            logger.info("CertificateGeneratorActor:generateSignUrl:generate sign url method called for uri: ".concat(uri));
            String signUrl = storageService.getSignedURL(certVar.getCONTAINER_NAME(), uri, Some.apply(getTimeoutInSeconds()),
                    Some.apply("r"));
            logger.info("CertificateGeneratorActor:generateSignUrl:signedUrl got: ".concat(signUrl));
            Response response = new Response();
            response.put(JsonKey.RESPONSE, JsonKey.SUCCESS);
            response.put(JsonKey.SIGNED_URL, signUrl);
            sender().tell(response, self());
        } catch (Exception e) {
            logger.error("CertificateGeneratorActor:generateSignUrl: error in generating sign url " + e);
            Response response = new Response();
            response.put(JsonKey.RESPONSE, "failure");
            response.put(JsonKey.SIGNED_URL, "");
            sender().tell(response, self());
        } finally {
          try {
            if (null != storageService) {
              storageService.closeContext();
            }
          } catch (Exception ex) {
            logger.info("CertificateGeneratorActor:generateSignUrl : Exception occurred while closing connection");
          }
        }

    }


    private BaseStorageService getStorageService() {
        StorageConfig storageConfig = new StorageConfig(certVar.getCloudStorageType(), certVar.getAzureStorageKey(), certVar.getAzureStorageSecret());
        logger.info("CertificateGeneratorActor:getStorageService:storage object formed:".concat(storageConfig.toString()));
        return StorageServiceFactory.getStorageService(storageConfig);
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
        CertMapper certMapper = new CertMapper(properties);
        List<CertModel> certModelList = certMapper.toList(request.getRequest());
        CertificateGenerator certificateGenerator = new CertificateGenerator(properties,directory);
        List<Map<String, Object>> certUrlList = new ArrayList<>();
        for (CertModel certModel : certModelList) {
            String uuid = null;
            try {
                CertificateExtension certificateExtension = certificateGenerator.getCertificateExtension(certModel);
                uuid = certificateGenerator.getUUID(certificateExtension);
                Map<String,Object> qrMap = certificateGenerator.generateQrCode();
                String qrImageUrl = uploadQrCode((File)qrMap.get(JsonKey.QR_CODE_FILE),properties);
                String accessCode = (String)qrMap.get(JsonKey.ACCESS_CODE);
                String jsonData = certificateGenerator.generateCertificateJson();
                Map<String, Object> uploadRes = uploadJson(directory + uuid, certStore, certStoreFactory.setCloudPath(storeParams));
                String version = (String) request.getContext().get(JsonKey.VERSION);
                CertificateResponse certificateResponse;
                if (version.equalsIgnoreCase(JsonKey.VERSION_2)) {
                    certificateResponse = new CertificateResponseV2(uuid, accessCode, certModel.getIdentifier(), mapper.readValue(jsonData, Map.class), qrImageUrl);
                } else {
                    String htmlTemplateUrl =  (String)((Map) request.get(JsonKey.CERTIFICATE)).get(JsonKey.HTML_TEMPLATE);
                    String pdfLink = PdfGenerator.generate(htmlTemplateUrl, certificateExtension, qrImageUrl, getContainerName(storeParams), certStoreFactory.setCloudPath(storeParams));
                    certificateResponse = new CertificateResponseV1(uuid, accessCode,certModel.getIdentifier(), mapper.readValue(jsonData, Map.class), properties.get(JsonKey.BASE_PATH).concat(pdfLink));
                }
                certificateResponse.setJsonUrl(properties.get(JsonKey.BASE_PATH).concat((String) uploadRes.get(JsonKey.JSON_URL)));
                certUrlList.add(mapper.convertValue(certificateResponse, new TypeReference<Map<String, Object>>(){}));
            } catch (Exception ex) {
                logger.error("CertificateGeneratorActor:generateCertificate:Exception Occurred while generating certificate. : " + ex.getMessage());
                throw new BaseException(IResponseMessage.INTERNAL_ERROR, ex.getMessage(), ResponseCode.SERVER_ERROR.getCode());
            } finally {
              try{
                certStoreFactory.cleanUp(uuid, directory);
                cleanup(directory, uuid);
              } catch (Exception ex) {
                logger.error("Exception occurred during resource clean");
              }
            }
        }
        certStore.close();
        Response response = new Response();
        response.getResult().put("response", certUrlList);
        sender().tell(response, getSelf());
        logger.info("onReceive method call End");
    }

    private void generateCertificateV2(Request request) throws BaseException {
        logger.info("generateCertificateV2 request received==" + request.getRequest());
        generateCertificate(request);
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

    private void cleanup(String path, String fileName) {
      try {
        File directory = new File(path);
        File[] files = directory.listFiles();
        for (File file : files) {
          if (file.getName().startsWith(fileName)) file.delete();
        }
        logger.info("CertificateGeneratorActor: cleanUp completed");
      } catch (Exception ex) {
        logger.error(ex.getMessage(), ex);
      }
    }

}
