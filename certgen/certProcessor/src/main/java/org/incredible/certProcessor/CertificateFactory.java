package org.incredible.certProcessor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.incredible.builders.*;
import org.incredible.certProcessor.signature.SignatureHelper;
import org.incredible.certProcessor.signature.exceptions.SignatureException;
import org.incredible.pojos.CertificateExtension;
import org.incredible.pojos.ob.Criteria;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.incredible.pojos.ob.SignedVerification;
import org.incredible.pojos.ob.exeptions.InvalidDateFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CertificateFactory {

    private static String uuid;

    private static Logger logger = LoggerFactory.getLogger(CertificateFactory.class);

    final String resourceName = "application.properties";

    private static SignatureHelper signatureHelper;

    private ObjectMapper mapper = new ObjectMapper();

    public CertificateExtension createCertificate(CertModel certModel, Map<String, String> properties) throws InvalidDateFormatException {

        uuid = JsonKey.DOMAIN_PATH + UUID.randomUUID().toString() + ".json";

        CertificateExtensionBuilder certificateExtensionBuilder = new CertificateExtensionBuilder(JsonKey.CONTEXT);
        CompositeIdentityObjectBuilder compositeIdentityObjectBuilder = new CompositeIdentityObjectBuilder(JsonKey.CONTEXT);
        BadgeClassBuilder badgeClassBuilder = new BadgeClassBuilder(JsonKey.CONTEXT);
        IssuerBuilder issuerBuilder = new IssuerBuilder(JsonKey.CONTEXT);
        SignedVerification signedVerification = new SignedVerification();
        SignatureBuilder signatureBuilder = new SignatureBuilder();


        Criteria criteria = new Criteria();
        criteria.setNarrative("For exhibiting outstanding performance");
        criteria.setId(uuid);


        //todo decide hosted or signed badge based on config
        if ((JsonKey.VERIFICATION_TYPE).equals("hosted")) {
            signedVerification.setType(new String[]{JsonKey.VERIFICATION_TYPE});
        } else {
            signedVerification.setCreator(JsonKey.PUBLIC_KEY_URL);
        }

        /**
         *  recipient object
         *  **/
        compositeIdentityObjectBuilder.setName(certModel.getRecipientName()).setId(certModel.getIdentifier())
                .setHashed(false).
                setType(new String[]{"id"});

        issuerBuilder.setId(JsonKey.ISSUER_URL).setName(certModel.getIssuer().getName())
                .setUrl(certModel.getIssuer().getUrl()).setPublicKey(certModel.getIssuer().getPublicKey());
        /**
         * badge class object
         * **/
        badgeClassBuilder.setName(certModel.getCourseName()).setDescription(certModel.getCertificateDescription())
                .setId(JsonKey.BADGE_URL).setCriteria(criteria)
                .setImage(certModel.getCertificateLogo()).
                setIssuer(issuerBuilder.build());

        /**
         *
         * Certificate extension object
         */
        certificateExtensionBuilder.setId(uuid).setRecipient(compositeIdentityObjectBuilder.build())
                .setBadge(badgeClassBuilder.build())
                .setIssuedOn(certModel.getIssuedDate()).setExpires(certModel.getExpiry())
                .setValidFrom(certModel.getValidFrom()).setVerification(signedVerification).setSignatory(certModel.getSignatoryList());

        /** certificate  signature value **/
        String signatureValue = getSignatureValue(certificateExtensionBuilder.build(), properties);

        logger.info("signed certificate is valid {}", verifySignature(certificateExtensionBuilder.build(), signatureValue, properties));

        /**
         * to assign signature value
         */
        signatureBuilder.setCreated(Instant.now().toString()).setCreator(properties.get(JsonKey.SIGN_CREATOR))
                .setSignatureValue(signatureValue);

        certificateExtensionBuilder.setSignature(signatureBuilder.build());

        logger.info("certificate extension => {}", certificateExtensionBuilder.build());
        return certificateExtensionBuilder.build();
    }

    /**
     * to verify signature value
     *
     * @param certificate
     * @param signatureValue
     * @param properties
     * @return
     */
    public boolean verifySignature(CertificateExtension certificate, String signatureValue, Map<String, String> properties) {
        boolean isValid = false;
        SignatureHelper signatureHelper = new SignatureHelper(properties);
        try {
            Map signReq = new HashMap<String, Object>();
            signReq.put(JsonKey.CLAIM, certificate);
            signReq.put(JsonKey.SIGNATURE_VALUE, signatureValue);
            signReq.put(JsonKey.KEY_ID, getKeyId(properties.get(JsonKey.SIGN_CREATOR)));
            JsonNode jsonNode = mapper.valueToTree(signReq);
            isValid = signatureHelper.verify(jsonNode);
        } catch (SignatureException.UnreachableException | SignatureException.VerificationException e) {
            logger.error("exception while verifying Signature : {}", e.getMessage());
        }
        return isValid;
    }

    /**
     * to get signature value of certificate
     *
     * @param certificateExtension
     * @param properties
     * @return
     */
    private String getSignatureValue(CertificateExtension certificateExtension, Map<String, String> properties) {
        SignatureHelper signatureHelper = new SignatureHelper(properties);
        Map<String, Object> signMap;
        try {
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String request = mapper.writeValueAsString(certificateExtension);
            JsonNode jsonNode = mapper.readTree(request);
            signMap = signatureHelper.generateSignature(jsonNode);
            return (String) signMap.get(JsonKey.SIGNATURE_VALUE);
        } catch (IOException | SignatureException.UnreachableException | SignatureException.CreationException e) {
            logger.debug("Exception while generating signature for certificate : {}", e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * to get the KeyId from the sign_creator url , key id used for verifying signature
     *
     * @param creator
     * @return
     */
    private int getKeyId(String creator) {
        String idStr = null;
        try {
            URI uri = new URI(creator);
            String path = uri.getPath();
            idStr = path.substring(path.lastIndexOf('/') + 1);
        } catch (URISyntaxException e) {
            logger.debug("Exception while getting key id from the sign-creator url : {}", e.getMessage());
        }
        return Integer.parseInt(idStr);
    }

}

