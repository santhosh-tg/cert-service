package org.incredible.certProcessor.store;

import java.io.File;
import java.io.IOException;

public interface ICertStore {

    String save(File file, String uploadPath) throws IOException;

    void get(String url, String fileName, String localPath) throws IOException;

    void init();


}
