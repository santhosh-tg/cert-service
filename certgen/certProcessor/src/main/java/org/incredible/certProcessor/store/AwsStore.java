package org.incredible.certProcessor.store;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.exception.StorageServiceException;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;

import java.io.File;

/**
 * used to upload or downloads files to aws
 */
public class AwsStore extends CloudStore {

    private StoreConfig awsStoreConfig;

    private Logger logger = Logger.getLogger(AwsStore.class);

    private BaseStorageService storageService = null;

    public AwsStore(StoreConfig awsStoreConfig) {
        this.awsStoreConfig = awsStoreConfig;
    }


    @Override
    public String upload(File file, String path) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(path);
        if (StringUtils.isNotBlank(awsStoreConfig.getAwsStoreConfig().getPath())) {
            stringBuilder.append(awsStoreConfig.getAwsStoreConfig().getPath() + "/");
        }
        CloudStorage cloudStorage = new CloudStorage(storageService);
        int retryCount = Integer.parseInt(awsStoreConfig.getCloudRetryCount());
        return cloudStorage.uploadFile(awsStoreConfig.getAwsStoreConfig().getContainerName(), stringBuilder.toString(), file, false, retryCount);
    }

    @Override
    public void download(String fileName, String localPath) throws StorageServiceException {
        CloudStorage cloudStorage = new CloudStorage(storageService);
        cloudStorage.downloadFile(awsStoreConfig.getAwsStoreConfig().getContainerName(), fileName, localPath, false);
    }

    @Override
    public void init() {
        if (StringUtils.isNotBlank(awsStoreConfig.getType())) {
            String storageKey = awsStoreConfig.getAwsStoreConfig().getAccount();
            String storageSecret = awsStoreConfig.getAwsStoreConfig().getKey();
            StorageConfig storageConfig = new StorageConfig(awsStoreConfig.getType(), storageKey, storageSecret);
            logger.info("StorageParams:init:all storage params initialized for aws block");
            storageService = StorageServiceFactory.getStorageService(storageConfig);
        } else {
            logger.error("StorageParams:init:provided cloud store type doesn't match supported storage devices:".concat(awsStoreConfig.getType()));
        }

    }
}
