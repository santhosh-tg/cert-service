package org.incredible.certProcessor;

public interface jsonKey {

   String CONTEXT="http://localhost:8080/container/v1/context.json";
   String DOMAIN_PATH="http://localhost:8080";
   String ISSUER_URL="http://localhost:8080/container/orgId/issuer.json";
   String BADGE_URL="http://localhost:8080/container/orgId/badge.json";
   String PUBLIC_KEY_URL="http://localhost:8080/container/orgid/_orgIDpublicKey.json";
   String VERIFICATION_TYPE="hosted";
}
