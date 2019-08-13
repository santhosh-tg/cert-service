package org.incredible.certProcessor.signature;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.incredible.certProcessor.JsonKey;
import org.incredible.certProcessor.signature.exceptions.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SignatureHelper {

    private Map<String, String> properties;


    public SignatureHelper(Map<String, String> properties) {
        this.properties = properties;
    }

    private ObjectMapper mapper = new ObjectMapper();


    private static Logger logger = LoggerFactory.getLogger(SignatureHelper.class);


    /**
     * This method calls signature service for signing the object
     *
     * @param rootNode - contains input need to be signed
     * @return - signed data with key
     * @throws SignatureException.UnreachableException
     * @throws SignatureException.CreationException
     */
    public Map<String, Object> generateSignature(JsonNode rootNode)
            throws SignatureException.UnreachableException, SignatureException.CreationException {
        Map signReq = new HashMap<String, Object>();
        signReq.put("entity", rootNode);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(properties.get(JsonKey.SIGN_URL));
        try {
            StringEntity entity = new StringEntity(mapper.writeValueAsString(signReq));
            httpPost.setEntity(entity);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            CloseableHttpResponse response = client.execute(httpPost);
            return mapper.readValue(response.getEntity().getContent(),
                    new TypeReference<Map<String, Object>>() {
                    });
        } catch (ClientProtocolException e) {
            logger.error("ClientProtocolException when signing: {}", e.getMessage());
            throw new SignatureException().new UnreachableException(e.getMessage());
        } catch (IOException e) {
            logger.error("RestClientException when signing: {}", e.getMessage());
            throw new SignatureException().new CreationException(e.getMessage());

        }

    }

    /**
     * This method checks signature service is available or not
     *
     * @return - true or false
     * @throws SignatureException.UnreachableException
     */
    public boolean checkServiceIsUp() throws SignatureException.UnreachableException {
        boolean isSignServiceUp = false;
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(properties.get(JsonKey.SIGN_HEALTH_CHECK_URL));
            httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            CloseableHttpResponse response = client.execute(httpGet);
            String result = mapper.readValue(response.getEntity().getContent(),
                    new TypeReference<String>() {
                    });
            if (result.equalsIgnoreCase("UP")) {
                isSignServiceUp = true;
                logger.debug("Signature service running !");
            }
        } catch (IOException e) {
            logger.error("ClientProtocolException when checking the health of the  signature service:{} ", e.getMessage());
            throw new SignatureException().new UnreachableException(e.getMessage());
        }
        return isSignServiceUp;
    }


    public boolean verify(JsonNode rootNode)
            throws SignatureException.UnreachableException, SignatureException.VerificationException {
        logger.debug("verify method starts with value {}", rootNode);
        Map signReq = new HashMap<String, Object>();
        signReq.put("entity", rootNode);
        boolean result = false;
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(properties.get(JsonKey.SIGN_VERIFY_URL));
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        try {
            StringEntity entity = new StringEntity(mapper.writeValueAsString(signReq));
            httpPost.setEntity(entity);
            CloseableHttpResponse response = client.execute(httpPost);
            result = mapper.readValue(response.getEntity().getContent(),
                    new TypeReference<Boolean>() {
                    });

        } catch (ClientProtocolException ex) {
            logger.error("ClientProtocolException when verifying: ", ex);
            throw new SignatureException().new UnreachableException(ex.getMessage());
        } catch (Exception e) {
            logger.error("ClientProtocolException when verifying: ", e);
            throw new SignatureException().new VerificationException(e.getMessage());
        }
        logger.debug("verify method ends with value {}", result);
        return result;
    }

}