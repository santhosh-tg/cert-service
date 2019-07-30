package org.incredible.certProcessor.store;

import org.apache.commons.lang3.StringUtils;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class StorageParams {

    //todo remove dependency on env file or properties


    private static BaseStorageService storageService = null;

    final static String resourceName = "application.properties";

    static Properties properties = readPropertiesFile();


    private static String cloudStoreType = properties.getProperty("CLOUD_STORAGE_TYPE");

    static {

        if (StringUtils.equalsIgnoreCase(cloudStoreType, "azure")) {
            String storageKey = System.getenv("azure_account_name");
            String storageSecret = System.getenv("azure_storage_key");
            storageService = StorageServiceFactory.getStorageService(new StorageConfig(cloudStoreType, storageKey, storageSecret));
        } else if (StringUtils.equalsIgnoreCase(cloudStoreType, "aws")) {
            String storageKey = System.getenv("aws_storage_key");
            String storageSecret = System.getenv("aws_storage_secret");
            storageService = StorageServiceFactory.getStorageService(new StorageConfig(cloudStoreType, storageKey, storageSecret));
        } else {
//            throw new ServerException("ERR_INVALID_CLOUD_STORAGE Error while initialising cloud storage");
        }
    }


    public static String upload(String container, String path, File file, boolean isDirectory) {
        CloudStorage cloudStorage = new CloudStorage(storageService);
        return cloudStorage.uploadFile(container, path, file, isDirectory);
    }

    public static Properties readPropertiesFile() {
        ClassLoader loader = StorageParams.class.getClassLoader();
        Properties properties = new Properties();
        try (InputStream resourceStream = loader.getResourceAsStream(resourceName)) {
            properties.load(resourceStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
