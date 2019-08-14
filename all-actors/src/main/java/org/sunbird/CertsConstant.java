package org.sunbird;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * this constant file is used to get the Constants which is used by entire actors
 */
public class CertsConstant {
    private static Logger logger = Logger.getLogger(CertsConstant.class);

    private String BADGE_URL = "Badge.json";
    private String ISSUER_URL = "Issuer.json";
    private String CONTEXT = "v1/context.json";
    private String PUBLIC_KEY_URL="v1/PublicKey.json";
    private String VERIFICATION_TYPE = "SignedBadge";
    private String CLOUD_UPLOAD_RETRY_COUNT = "3";
    private String ACCESS_CODE_LENGTH = "6";
    private static String DOMAIN_URL = getDomainUrlFromEnv();
    private static String CONTAINER_NAME=getContainerNameFromEnv();


     public String getBADGE_URL(String rootOrgId, String batchId) {
        return String.format("%s/%s/%s/%s/%s", DOMAIN_URL,CONTAINER_NAME,rootOrgId,batchId,BADGE_URL);
    }

     public String getISSUER_URL(String rootOrgId) {
         return String.format("%s/%s/%s/%s", DOMAIN_URL,CONTAINER_NAME,rootOrgId,ISSUER_URL);
    }

    public String getCONTEXT() {
        return String.format("%s/%s/%s", DOMAIN_URL,CONTAINER_NAME,CONTEXT);
    }

    public String getPUBLIC_KEY_URL(String rootOrgId) {
        return String.format("%s/%s/%s/%s", DOMAIN_URL,CONTAINER_NAME,rootOrgId,PUBLIC_KEY_URL);
    }

    public String getVERIFICATION_TYPE() {
        return VERIFICATION_TYPE;
    }

    public String getCLOUD_UPLOAD_RETRY_COUNT() {
        return CLOUD_UPLOAD_RETRY_COUNT;
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

    private static String getDomainUrlFromEnv(){
//        String domainUrl=getPropertyFromEnv(JsonKey.DOMAIN_URL);
//        validateEnvProperty(domainUrl);
//        return domainUrl;
        return "https://dev.sunbirded.org";
    }

    private static String getContainerNameFromEnv(){
        String containerName=getPropertyFromEnv(JsonKey.CONTAINER_NAME);
        validateEnvProperty(containerName);
        return containerName;
    }
    private static String getPropertyFromEnv(String property){
         return System.getenv(property);
    }
    private  static void validateEnvProperty(String property){
        if(StringUtils.isBlank(property)){
            printErrorForMissingEnv(property);
            System.exit(-1);
        }
    }

    private static void  printErrorForMissingEnv(String env){
     logger.error("Constant:printErrorForMissingEnv:No env variable found ".concat(env));
    }
}
