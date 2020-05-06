package org.sunbird.response;

public class CertificateResponse {
    private String uuid;
    private String accessCode;
    private String jsonData;
    private String recipientId;
    private String pdfLink;
    private String jsonLink;
    public CertificateResponse() {
    }

    public CertificateResponse(String uuid, String accessCode, String jsonData, String recipientId) {
        this.uuid = uuid;
        this.accessCode = accessCode;
        this.jsonData = jsonData;
        this.recipientId = recipientId;
        this.pdfLink = pdfLink;
    }

    public CertificateResponse(String uuid, String accessCode, String jsonData, String recipientId, String pdfLink) {
        this.uuid = uuid;
        this.accessCode = accessCode;
        this.jsonData = jsonData;
        this.recipientId = recipientId;
        this.pdfLink = pdfLink;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getPdfLink() {
        return pdfLink;
    }

    public void setPdfLink(String pdfLink) {
        this.pdfLink = pdfLink;
    }

    public String getJsonLink() {
        return jsonLink;
    }

    public void setJsonLink(String jsonLink) {
        this.jsonLink = jsonLink;
    }

}
