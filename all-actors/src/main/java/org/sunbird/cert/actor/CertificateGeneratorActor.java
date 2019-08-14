package org.sunbird.cert.actor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.incredible.CertificateGenerator;
import org.incredible.certProcessor.CertModel;
import org.incredible.certProcessor.views.HTMLTempalteZip;
import org.sunbird.*;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.azure.AzureFileUtility;
import org.sunbird.message.IResponseMessage;
import org.sunbird.message.ResponseCode;
import org.sunbird.request.Request;
import org.sunbird.response.Response;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

/**
 * This actor is responsible for certificate generation.
 * @author manzarul
 *
 */
@ActorConfig(
        tasks = {"generateCert"},
        asyncTasks = {}
)
public class CertificateGeneratorActor extends BaseActor {
	private Logger logger = Logger.getLogger(CertificateGeneratorActor.class);
	private static CertsConstant certVar=new CertsConstant();

	@Override
	public void onReceive(Request request) throws Throwable {
		String operation = request.getOperation();
		logger.info("onReceive method call start for operation " + operation);
		if("generateCert".equalsIgnoreCase(operation)){
			generateCertificate(request);
		}
		logger.info("onReceive method call End");
	}

	private void generateCertificate(Request request) throws BaseException {
		logger.info("Request received==" + request.getRequest());
		List<CertModel> certModelList = CertMapper.toList(request.getRequest());
		CertificateGenerator certificateGenerator = new CertificateGenerator(populatePropertiesMap(request));
		HTMLTempalteZip htmlTempalteZip = null;
		String url = (String)((Map<String,Object>)request.getRequest().get(JsonKey.CERTIFICATE)).get(JsonKey.HTML_TEMPLATE);
		try {
			htmlTempalteZip = new HTMLTempalteZip(new URL(url));
		} catch (Exception ex) {
			logger.error("CertificateGeneratorActor:generateCertificate:Exception Occurred while creating HtmlTemplate provider.",ex);
			throw new  BaseException("INVALID_PARAM_VALUE", MessageFormat.format(IResponseMessage.INVALID_PARAM_VALUE,url,JsonKey.HTML_TEMPLATE), ResponseCode.CLIENT_ERROR.getCode());
		}
		List<Map<String,String>> certUrlList = new ArrayList<>();
		for(CertModel certModel : certModelList){
			String certUUID = "";
			try {
				certUUID = certificateGenerator.createCertificate(certModel,htmlTempalteZip);
			} catch (Exception ex) {
				cleanup();
				logger.error("CertificateGeneratorActor:generateCertificate:Exception Occurred while generating certificate.",ex);
				throw new  BaseException("INTERNAL_SERVER_ERROR", IResponseMessage.INTERNAL_ERROR, ResponseCode.SERVER_ERROR.getCode());
			}
			certUrlList.add(uploadCertificate(certUUID));
		}
		Response response = new Response();
		response.getResult().put("response", certUrlList);
		sender().tell(response, getSelf());
		cleanup();
		logger.info("onReceive method call End");
	}

	private void cleanup() {
		try {
			File file = new File("conf/certificate");
			FileUtils.deleteDirectory(file);
		} catch (Exception ex) {
			logger.info(ex.getMessage(),ex);
		}
	}

	private Map<String,String> uploadCertificate(String certUUID) {
		Map<String,String> resMap = new HashMap<>();
		String certFileName = certUUID+".html";
		resMap.put(certFileName,upload(certFileName));
		certFileName = certUUID+".pdf";
		resMap.put(certFileName,upload(certFileName));
		certFileName = certUUID+".json";
		resMap.put(certFileName,upload(certFileName));
		return resMap;
	}

	private String upload(String certFileName) {
		try {
			//TODO  Un comment this to use cloud storage jar to upload file to azure as of now
			// not using because of jar conflict issue

			//HashMap<String,String> properties = new HashMap<>();
			//properties.put(JsonKey.CONTAINER_NAME,System.getenv(JsonKey.CONTAINER_NAME));
			//properties.put(JsonKey.CLOUD_STORAGE_TYPE,System.getenv(JsonKey.CLOUD_STORAGE_TYPE));
			//properties.put(JsonKey.CLOUD_UPLOAD_RETRY_COUNT,System.getenv(JsonKey.CLOUD_UPLOAD_RETRY_COUNT));
			//properties.put(JsonKey.AZURE_STORAGE_SECRET,System.getenv(JsonKey.AZURE_STORAGE_SECRET));
			//properties.put(JsonKey.AZURE_STORAGE_KEY,System.getenv(JsonKey.AZURE_STORAGE_KEY));

			//StorageParams storageParams = new StorageParams(properties);
			//storageParams.init();
			//return storageParams.upload(System.getenv(JsonKey.CONTAINER_NAME), "/", file, false);

			File file = FileUtils.getFile("conf/certificate/"+certFileName);
			logger.info("CertificateGeneratorActor:upload:container name got from env is: ".concat(System.getenv(JsonKey.CONTAINER_NAME)));
			return AzureFileUtility.uploadFile(System.getenv(JsonKey.CONTAINER_NAME),file);
		}catch (Exception ex) {
			logger.info("CertificateGeneratorActor:upload: Exception occurred while uploading certificate.",ex);
		}
		return StringUtils.EMPTY;
	}

	private HashMap<String,String> populatePropertiesMap(Request request){
		String orgId=(String)((Map)request.get(JsonKey.CERTIFICATE)).get(JsonKey.ORG_ID);
		String tag=(String) ((Map)request.get(JsonKey.CERTIFICATE)).get(JsonKey.TAG);
		HashMap<String,String> properties = new HashMap<>();
		properties.put(JsonKey.DOMAIN_URL, certVar.getDOMAIN_URL());
		properties.put(JsonKey.BADGE_URL,certVar.getBADGE_URL(orgId,tag));
		properties.put(JsonKey.ISSUER_URL,certVar.getISSUER_URL(orgId));
		properties.put(JsonKey.CONTEXT,certVar.getCONTEXT());
		properties.put(JsonKey.VERIFICATION_TYPE,certVar.getVERIFICATION_TYPE());
		properties.put(JsonKey.ACCESS_CODE_LENGTH,certVar.getACCESS_CODE_LENGTH());
		properties.put(JsonKey.PUBLIC_KEY,certVar.getPUBLIC_KEY_URL(orgId));
		logger.info("CertificateGeneratorActor:getProperties:properties got from Constant File ".concat(Collections.singleton(properties.toString())+""));
		return properties;
	}

}
