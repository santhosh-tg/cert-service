package org.incredible.certProcessor.store;


import org.apache.commons.lang3.StringUtils;
import org.sunbird.cloud.storage.BaseStorageService;
import scala.Option;

import java.io.File;
import java.net.URL;

public class CloudStorage {

    private static BaseStorageService storageService = null;


    public CloudStorage(BaseStorageService storageService) {
        this.storageService = storageService;
    }


    public static String uploadFile(String container, String path, File file, boolean isDirectory) {
        int retryCount = 2;
        String objectKey = path + file.getName();
        String url = storageService.upload(container,
                file.getAbsolutePath(),
                objectKey,
                Option.apply(false),
                Option.apply(isDirectory),
                Option.apply(1),
                Option.apply(retryCount), 1);
        try{
            URL blobUrl=new URL(url);
            return blobUrl.getFile();
        }
        catch (Exception e){
            return StringUtils.EMPTY;
        } }
}
