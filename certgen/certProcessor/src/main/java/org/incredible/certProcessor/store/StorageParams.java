package org.incredible.certProcessor.store;

import org.apache.commons.lang3.StringUtils;
import org.incredible.certProcessor.JsonKey;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;

import java.io.File;
import java.util.Map;

public class StorageParams {


    private static BaseStorageService storageService = null;

    private static Map<String, String> properties;

    public StorageParams(Map<String, String> properties) {
        this.properties = properties;
    }

    public void init() {
        String cloudStoreType = properties.get(JsonKey.CLOUD_STORAGE_TYPE);
        if (StringUtils.equalsIgnoreCase(cloudStoreType, JsonKey.AZURE)) {
            String storageKey = properties.get(JsonKey.AZURE_STORAGE_KEY);
            String storageSecret = properties.get(JsonKey.AZURE_STORAGE_SECRET);
            StorageConfig storageConfig = new StorageConfig(cloudStoreType, storageKey, storageSecret);
            storageService = StorageServiceFactory.getStorageService(storageConfig);
            } else if (StringUtils.equalsIgnoreCase(cloudStoreType,JsonKey.AWS)) {
                String storageKey = properties.get(JsonKey.AWS_STORAGE_KEY);
                String storageSecret = properties.get(JsonKey.AWS_STORAGE_SECRET);
                storageService = StorageServiceFactory.getStorageService(new StorageConfig(cloudStoreType, storageKey, storageSecret));
        } else {
//            throw new ServerException("ERR_INVALID_CLOUD_STORAGE Error while initialising cloud storage");
        }
    }

    public String upload(String container, String path, File file, boolean isDirectory) {
        CloudStorage cloudStorage = new CloudStorage(storageService);
        int retryCount= Integer.parseInt(properties.get(JsonKey.CLOUD_UPLOAD_RETRY_COUNT));
        return cloudStorage.uploadFile(container, path, file, isDirectory,retryCount);
    }
}
