package org.sunbird.response;

import java.util.Map;

public class CertificateResponseV2 extends CertificateResponse {

    private String qrCodeUrl;

    public CertificateResponseV2(String id, String accessCode, String recipientId, Map<String, Object> jsonData, String qrCodeUrl) {
        super(id, accessCode, recipientId, jsonData);
        setQrCodeUrl(qrCodeUrl);
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }
}
