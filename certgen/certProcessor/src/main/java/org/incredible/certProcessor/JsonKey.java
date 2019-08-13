package org.incredible.certProcessor;

public interface JsonKey {

    String CONTEXT = "http://localhost:8080/container/v1/context.json";
    String DOMAIN_PATH = "http://localhost:8080/";
    String ISSUER_URL = "http://localhost:8080/container/orgId/issuer.json";
    String BADGE_URL = "http://localhost:8080/container/orgId/badge.json";
    String PUBLIC_KEY_URL = "http://localhost:8080/container/orgid/_orgIDpublicKey.json";
    String VERIFICATION_TYPE = "hosted";

    String CLAIM = "claim";
    String SIGNATURE_VALUE = "signatureValue";
    String KEY_ID = "keyId";
    String SIGN_CREATOR = "SIGN_CREATOR";
    String SIGN_URL = "SIGN_URL";
    String SIGN_HEALTH_CHECK_URL = "SIGN_HEALTH_CHECK_URL";
    String SIGN_VERIFY_URL = "SIGN_VERIFY_URL";
}
