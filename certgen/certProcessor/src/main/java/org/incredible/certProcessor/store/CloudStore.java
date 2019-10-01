package org.incredible.certProcessor.store;

import java.io.File;

public abstract class CloudStore implements ICertStore {

    public CloudStore() {
    }

    @Override
    public String save(File file, String uploadPath) {
        return upload(file, uploadPath);
    }

    @Override
    public void get(String url, String fileName, String localPath) {
        download(fileName, localPath);
    }

    abstract public String upload(File file, String uploadPath);

    abstract public void download(String fileName, String localPath);


}
