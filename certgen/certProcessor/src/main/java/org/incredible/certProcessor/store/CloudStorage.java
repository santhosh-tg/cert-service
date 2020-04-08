package org.incredible.certProcessor.store;


import org.apache.log4j.Logger;
import org.incredible.UrlManager;
import org.sunbird.cloud.storage.BaseStorageService;
import scala.Option;

import java.io.File;

public class CloudStorage {

    private Logger logger = Logger.getLogger(CloudStorage.class);

    private  BaseStorageService storageService;


    public CloudStorage(BaseStorageService storageService) {
        this.storageService = storageService;
    }

    public  String upload(String container, String path, File file, boolean isDirectory,int retryCount) {
        String objectKey = path + file.getName();
        String url = storageService.upload(container,
                file.getAbsolutePath(),
                objectKey,
                Option.apply(isDirectory),
                Option.apply(1),
                Option.apply(retryCount), Option.apply(1));
        return url;
    }

    public  String uploadFile(String container, String path, File file, boolean isDirectory,int retryCount) {
        String objectKey = path + file.getName();
        String url = storageService.upload(container,
                file.getAbsolutePath(),
                objectKey,
                Option.apply(isDirectory),
                Option.apply(1),
                Option.apply(retryCount), Option.apply(1));
        return UrlManager.getSharableUrl(url,container);
         }


    public void downloadFile(String container, String fileName, String localPath, boolean isDirectory) {
        storageService.download(container, fileName, localPath, Option.apply(isDirectory));
        logger.info(fileName + " downloaded successfully");
    }
}
