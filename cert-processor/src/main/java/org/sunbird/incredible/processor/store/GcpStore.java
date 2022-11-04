package org.sunbird.incredible.processor.store;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;
import scala.Option;

import java.io.File;

public class GcpStore extends CloudStore {
    private StoreConfig gcpStoreConfig;

    private Logger logger = LoggerFactory.getLogger(GcpStore.class);

    private BaseStorageService storageService = null;

    private CloudStorage cloudStorage = null;

    private int retryCount = 0;

    public GcpStore(StoreConfig gcpStoreConfig) {
        this.gcpStoreConfig = gcpStoreConfig;
        retryCount = Integer.parseInt(gcpStoreConfig.getCloudRetryCount());
        init();
    }


    @Override
    public String upload(File file, String path) {
        String uploadPath = getPath(path);
        return cloudStorage.uploadFile(gcpStoreConfig.getGcpStoreConfig().getContainerName(), uploadPath, file, false, retryCount);
    }

    @Override
    public void download(String fileName, String localPath) {
        cloudStorage.downloadFile(gcpStoreConfig.getGcpStoreConfig().getContainerName(), fileName, localPath, false);
    }

    private String getPath(String path) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(path);
        if (StringUtils.isNotBlank(gcpStoreConfig.getGcpStoreConfig().getPath())) {
            stringBuilder.append(gcpStoreConfig.getGcpStoreConfig().getPath() + "/");
        }
        return stringBuilder.toString();
    }

    @Override
    public String getPublicLink(File file, String uploadPath) {
        String path = getPath(uploadPath);
        return cloudStorage.upload(gcpStoreConfig.getGcpStoreConfig().getContainerName(), path, file, false, retryCount);
    }

    @Override
    public void init() {
        if (StringUtils.isNotBlank(gcpStoreConfig.getType())) {
            String storageKey = gcpStoreConfig.getGcpStoreConfig().getAccount();
            String storageSecret = gcpStoreConfig.getGcpStoreConfig().getKey();
            StorageConfig storageConfig = new StorageConfig(gcpStoreConfig.getType(), storageKey, storageSecret);
            logger.info("StorageParams:init:all storage params initialized for gcp block");
            storageService = StorageServiceFactory.getStorageService(storageConfig);
            cloudStorage = new CloudStorage(storageService);
        } else {
            logger.error("StorageParams:init:provided cloud store type doesn't match supported storage devices:".concat(gcpStoreConfig.getType()));
        }

    }

    @Override
    public void close(){
        cloudStorage.closeConnection();
    }
}
