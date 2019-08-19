package org.sunbird;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * this constant file is used to get the Constants which is used by entire actors
 */
public class CertsConstant {
    private static Logger logger = Logger.getLogger(CertsConstant.class);

    private static final String BADGE_URL = "Badge.json";
    private static final String ISSUER_URL = "Issuer.json";
    private static final String CONTEXT = "v1/context.json";
    private static final String PUBLIC_KEY_URL = "v1/PublicKey.json";
    private static final String VERIFICATION_TYPE = "SignedBadge";
    private static final String CLOUD_UPLOAD_RETRY_COUNT = "3";
    private static final String ACCESS_CODE_LENGTH = "6";
    public static final String DOWNLOAD_LINK_EXPIRY_TIMEOUT = "download_link_expiry_timeout";
    private static final String LINK_TIMEOUT = "600";
    private static final String DOMAIN_URL = getDomainUrlFromEnv();
    private static final String CONTAINER_NAME = getContainerNameFromEnv();
    private static final String ENC_SERVICE_URL = getEncServiceUrl();
    private static final String CLOUD_STORAGE_TYPE=getCloudStorageTypeFromEnv();
    private static final String AZURE_STORAGE_SECRET=getStorageSecret();
    private static final String AZURE_STORAGE_KEY=getStorageKey();

    public String getBADGE_URL(String rootOrgId, String batchId) {
        return String.format("%s/%s/%s/%s/%s", DOMAIN_URL, CONTAINER_NAME, rootOrgId, batchId, BADGE_URL);
    }


	public String getISSUER_URL(String rootOrgId) {
        return String.format("%s/%s/%s/%s", DOMAIN_URL, CONTAINER_NAME, rootOrgId, ISSUER_URL);
    }

    public String getCONTEXT() {
        return String.format("%s/%s/%s", DOMAIN_URL, CONTAINER_NAME, CONTEXT);
    }

    public String getPUBLIC_KEY_URL(String rootOrgId) {
        return String.format("%s/%s/%s/%s", DOMAIN_URL, CONTAINER_NAME, rootOrgId, PUBLIC_KEY_URL);
    }

    public String getVERIFICATION_TYPE() {
        return VERIFICATION_TYPE;
    }

    public String getCLOUD_UPLOAD_RETRY_COUNT() {
        String retryCount = getPropertyFromEnv(JsonKey.CLOUD_UPLOAD_RETRY_COUNT);
        return StringUtils.isNotBlank(retryCount)?retryCount:CLOUD_UPLOAD_RETRY_COUNT;
    }

    public String getACCESS_CODE_LENGTH() {
        return ACCESS_CODE_LENGTH;
    }

    public String getDOMAIN_URL() {
        return DOMAIN_URL;
    }

    public String getCONTAINER_NAME() {
        return CONTAINER_NAME;
    }

    private static String getDomainUrlFromEnv() {
        String domainUrl = getPropertyFromEnv(JsonKey.DOMAIN_URL);
        return StringUtils.isNotBlank(domainUrl) ? domainUrl : "https://dev.sunbirded.org";
//        validateEnvProperty(domainUrl);
//        return domainUrl;
    }

    private static String getContainerNameFromEnv() {
        String containerName = getPropertyFromEnv(JsonKey.CONTAINER_NAME);
        validateEnvProperty(containerName);
        return containerName;
    }

    private static String getPropertyFromEnv(String property) {
        return System.getenv(property);
    }

    private static void validateEnvProperty(String property) {
        if (StringUtils.isBlank(property)) {
            printErrorForMissingEnv(property);
            System.exit(-1);
        }
    }

    private static void printErrorForMissingEnv(String env) {
        logger.error("Constant:printErrorForMissingEnv:No env variable found ".concat(env));
    }

    private static String getEncServiceUrl() {
        String encServiceUrl = getPropertyFromEnv(JsonKey.ENC_SERVICE_URL);
//        validateEnvProperty(encServiceUrl);
//        return encServiceUrl;
        return StringUtils.isNotBlank(encServiceUrl) ? encServiceUrl : "http://enc-service:8013";

    }

    public String getEncSignUrl() {
        return String.format("%s/%s", ENC_SERVICE_URL, JsonKey.SIGN);
    }

    public String getEncSignVerifyUrl() {
        return String.format("%s/%s", ENC_SERVICE_URL, JsonKey.VERIFY);

    }


    public String getSignCreator(String keyId) {
        return String.format("%s/%s/%s", DOMAIN_URL, JsonKey.KEYS, keyId);
    }

    public String getEncryptionServiceUrl() {
        return getEncServiceUrl();
    }
    
    public static String getExpiryLink(String key) {
    	 return getPropertyFromEnv(key) != null ? getPropertyFromEnv(key) :LINK_TIMEOUT ;
	}


    private static String getCloudStorageTypeFromEnv() {
        String cloudStorageType = getPropertyFromEnv(JsonKey.CLOUD_STORAGE_TYPE);
        validateEnvProperty(cloudStorageType);
        return cloudStorageType;
    }

    private static String getStorageKey() {
        String storageKey = getPropertyFromEnv(JsonKey.AZURE_STORAGE_KEY);
        validateEnvProperty(storageKey);
        return storageKey;
    }

    private static String getStorageSecret() {
        String storageSecret = getPropertyFromEnv(JsonKey.AZURE_STORAGE_SECRET);
        validateEnvProperty(storageSecret);
        return storageSecret;
    }

    public  String getCloudStorageType() {
        return CLOUD_STORAGE_TYPE;
    }

    public  String getAzureStorageSecret() {
        return AZURE_STORAGE_SECRET;
    }

    public  String getAzureStorageKey() {
        return AZURE_STORAGE_KEY;
    }

}
