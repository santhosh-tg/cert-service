package org.sunbird.incredible.processor.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sunbird.incredible.processor.JsonKey;

import java.util.Map;

public class StoreConfig {

    private ObjectMapper mapper = new ObjectMapper();

    private String type;

    private String cloudRetryCount = "3";

    private AzureStoreConfig azureStoreConfig;

    private AwsStoreConfig awsStoreConfig;

    private GcpStoreConfig gcpStoreConfig;

    private StoreConfig() {
    }

    public StoreConfig(Map<String, Object> storeParams) {
        setType((String) storeParams.get(JsonKey.TYPE));
        if (storeParams.containsKey(JsonKey.AZURE)) {
            AzureStoreConfig azureStoreConfig = mapper.convertValue(storeParams.get(JsonKey.AZURE), AzureStoreConfig.class);
            setAzureStoreConfig(azureStoreConfig);
        } else if (storeParams.containsKey(JsonKey.AWS)) {
            AwsStoreConfig awsStoreConfig = mapper.convertValue(storeParams.get(JsonKey.AWS), AwsStoreConfig.class);
            setAwsStoreConfig(awsStoreConfig);
        } else if (storeParams.containsKey(JsonKey.GCP)) {
            GcpStoreConfig gcpStoreConfig = mapper.convertValue(storeParams.get(JsonKey.GCP), GcpStoreConfig.class);
            setGcpStoreConfig(gcpStoreConfig);
        }
    }

    public boolean isCloudStore() {
        return (azureStoreConfig != null || awsStoreConfig != null || gcpStoreConfig != null);
    }

    public String getContainerName() {
        String containerName = null;
        if (JsonKey.AZURE.equals(getType())) {
            containerName = azureStoreConfig.getContainerName();
        } else if (JsonKey.AWS.equals(getType())) {
            containerName = awsStoreConfig.getContainerName();
        } else if (JsonKey.GCP.equals(getType())) {
            containerName = gcpStoreConfig.getContainerName();
        }
        return containerName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCloudRetryCount() {
        return cloudRetryCount;
    }

    public void setCloudRetryCount(String cloudRetryCount) {
        this.cloudRetryCount = cloudRetryCount;
    }

    public AzureStoreConfig getAzureStoreConfig() {
        return azureStoreConfig;
    }

    public void setAzureStoreConfig(AzureStoreConfig azureStoreConfig) {
        this.azureStoreConfig = azureStoreConfig;
    }

    public AwsStoreConfig getAwsStoreConfig() {
        return awsStoreConfig;
    }

    public void setAwsStoreConfig(AwsStoreConfig awsStoreConfig) {
        this.awsStoreConfig = awsStoreConfig;
    }

    public GcpStoreConfig getGcpStoreConfig() {
        return gcpStoreConfig;
    }

    public void setGcpStoreConfig(GcpStoreConfig gcpStoreConfig) {
        this.gcpStoreConfig = gcpStoreConfig;
    }

    @Override
    public String toString() {
        String stringRep = null;
        try {
            stringRep = mapper.writeValueAsString(this);
        } catch (JsonProcessingException jpe) {
        }
        return stringRep;
    }
}
