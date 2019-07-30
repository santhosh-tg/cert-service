package org.incredible.certProcessor.store;


import org.sunbird.cloud.storage.BaseStorageService;
import scala.Option;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

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
                Option.apply(isDirectory),
                Option.apply(1),
                Option.apply(retryCount), Option.empty());
        return url;
    }

    public static void downloadFile(String downloadUrl, File fileToSave) throws IOException {
        URL url = new URL(downloadUrl);
        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(fileToSave);
        FileChannel fileChannel = fileOutputStream.getChannel();
        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        fileChannel.close();
        fileOutputStream.close();
        readableByteChannel.close();
    }

}
