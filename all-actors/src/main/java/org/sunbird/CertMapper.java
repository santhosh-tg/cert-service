package org.sunbird;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.incredible.certProcessor.CertModel;
import org.incredible.pojos.SignatoryExtension;
import org.incredible.pojos.ob.Issuer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CertMapper {

    private ObjectMapper mapper = new ObjectMapper();

    public static List<CertModel> toList(Map<String,Object> request){
        Map<String,Object> json = (Map<String,Object>)request.get(JsonKey.CERTIFICATE);
        List<Map<String,Object>> dataList = (List<Map<String,Object>>)json.get(JsonKey.DATA);
        Issuer issuer = getIssuer((Map<String,Object>)json.get(JsonKey.ISSUER));
        SignatoryExtension[] signatoryArr = getSignatoryArray((List<Map<String,Object>>)json.get(JsonKey.SIGNATORY_LIST));
        List<CertModel> certList = dataList.stream().map(data -> getCertModel(data)).collect(Collectors.toList());
        certList.stream().forEach(cert -> {
            cert.setIssuer(issuer);
            cert.setSignatoryList(signatoryArr);
            cert.setCourseName((String)json.get(JsonKey.COURSE_NAME));
            cert.setCertificateDescription((String)json.get(JsonKey.DESCRIPTION));
            cert.setCertificateLogo((String)json.get(JsonKey.LOGO));
            cert.setIssuedDate((String)json.get(JsonKey.ISSUED_DATE));
        });
        return certList;
    }

    private static SignatoryExtension[] getSignatoryArray(List<Map<String, Object>> signatoryList) {
        return signatoryList.stream().map(signatory ->
            getSignatory(signatory)).toArray(SignatoryExtension[] ::new);
    }

    private static SignatoryExtension getSignatory(Map<String, Object> signatory) {
        SignatoryExtension signatoryExt = new SignatoryExtension(System.getenv(JsonKey.CONTEXT));
        //signatoryExt.setName((String)signatory.get(JsonKey.NAME));
        signatoryExt.setIdentity((String)signatory.get(JsonKey.ID));
        signatoryExt.setDesignation((String)signatory.get(JsonKey.DESIGNATION));
        return signatoryExt;
    }


    private static Issuer getIssuer(Map<String, Object> issuerData) {
        Issuer issuer = new Issuer(System.getenv(JsonKey.CONTEXT));
        issuer.setName((String)issuerData.get(JsonKey.NAME));
        issuer.setUrl((String)issuerData.get(JsonKey.URL));
        List<String> keyList = (List<String>) issuerData.get(JsonKey.PUBLIC_KEY);
        if(CollectionUtils.isNotEmpty(keyList)){
            String[] keyArr = keyList.stream().toArray(String[] ::new);
            issuer.setPublicKey(keyArr);
        }
        return issuer;
    }


    private static CertModel getCertModel(Map<String,Object> data){
        CertModel certModel = new CertModel();
        certModel.setRecipientName((String)data.get(JsonKey.RECIPIENT_NAME));
        certModel.setRecipientEmail((String)data.get(JsonKey.RECIPIENT_EMAIl));
        certModel.setRecipientPhone((String)data.get(JsonKey.RECIPIENT_PHONE));
        certModel.setIdentifier((String)data.get(JsonKey.RECIPIENT_ID));
        certModel.setValidFrom((String)data.get(JsonKey.VALID_FROM));
        certModel.setExpiry((String)data.get(JsonKey.EXPIRY));
        //certModel.setIdentifier((String)data.get(JsonKey.OLD_ID));
        return certModel;
    }
}
