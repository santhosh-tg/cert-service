package org.sunbird;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;
import scala.Option;

import java.io.File;


public class CloudStorage {

    private static Logger logger = LoggerFactory.getLogger(CloudStorage.class);

    private static BaseStorageService storageService = null;

    private static String cloudStoreType = System.getenv("CLOUD_STORAGE_TYPE");

    private static String containerName = System.getenv("CONTAINER_NAME");

    static {
        logger.info("CLOUD_STORAGE_TYPE {}", cloudStoreType);
        logger.info("container name {}", containerName);
        if (StringUtils.equalsIgnoreCase(cloudStoreType, "azure")
                    || StringUtils.equalsIgnoreCase(cloudStoreType, "aws")
                    || StringUtils.equalsIgnoreCase(cloudStoreType, "gcloud")) {
            String storageKey = System.getenv("PRIVATE_CLOUD_STORAGE_SECRET");
            String storageSecret = System.getenv("PRIVATE_CLOUD_STORAGE_KEY");
            storageService = StorageServiceFactory.getStorageService(new StorageConfig(cloudStoreType, storageKey, storageSecret));
        } else try {
            throw new Exception("ERR_INVALID_CLOUD_STORAGE Error while initialising cloud storage");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String uploadFile(String path, File file) {
        String objectKey = path + file.getName();
        String url = storageService.upload(containerName,
                file.getAbsolutePath(),
                objectKey,
                Option.apply(false),
                Option.apply(1),
                Option.apply(5), Option.apply(1));
        return UrlManager.getSharableUrl(url, containerName);
    }


    public static void closeConnection() {
        storageService.closeContext();
    }
}
