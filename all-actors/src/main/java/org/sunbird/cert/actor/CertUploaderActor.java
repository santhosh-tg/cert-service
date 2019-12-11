package org.sunbird.cert.actor;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.incredible.certProcessor.store.ICertStore;
import org.sunbird.BaseActor;
import org.sunbird.JsonKey;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.request.Request;

@ActorConfig(
        tasks = {JsonKey.UPLOAD_CERT},
        asyncTasks = {}
)
public class CertUploaderActor extends BaseActor {
    private Logger logger = Logger.getLogger(CertUploaderActor.class);
    @Override
    public void onReceive(Request request) throws Throwable {
        String operation = request.getOperation();
        logger.info("onReceive method call start for operation " + operation);
        if (JsonKey.UPLOAD_CERT.equalsIgnoreCase(operation)) {
            upload(request);
        }
        logger.info("onReceive method call End");
    }

    private void upload(Request request) {
        ICertStore certStore = (ICertStore) request.getRequest().get("certStore");
        certStore.init();
        String fileName = (String)request.getRequest().get(JsonKey.DIRECTORY) + (String)request.getRequest().get(JsonKey.UUID);
        String cloudPath = (String)request.getRequest().get("certStoreFactory");
        Map<String, Object> resMap = new HashMap<>();
        File file = FileUtils.getFile(fileName.concat(".pdf"));
        try {
        resMap.put(JsonKey.PDF_URL, certStore.save(file, cloudPath));
        file = FileUtils.getFile(fileName.concat(".json"));
        resMap.put(JsonKey.JSON_URL, certStore.save(file, cloudPath));
        logger.info("json url after upload : "+resMap.get(JsonKey.JSON_URL));
        logger.info("pdf url after upload : "+resMap.get(JsonKey.PDF_URL));
        if (StringUtils.isBlank((String) resMap.get(JsonKey.PDF_URL)) || StringUtils.isBlank((String) resMap.get(JsonKey.JSON_URL))) {
            logger.error("CertUploaderActor:upload:Exception Occurred while uploading certificate pdfUrl and jsonUrl is null");
         }
        } catch (Exception ex) {
        	logger.error("CertUploaderActor:upload:Exception Occurred while uploading certificate ",ex);
        } finally {
        	cleanUp((String)request.getRequest().get(JsonKey.UUID),(String)request.getRequest().get(JsonKey.DIRECTORY));
        }
    }
    
    /**
     * used to clean up files which start with uuid.*
     *
     * @param fileName
     * @param path
     */
    public void cleanUp(String fileName, String path) {
        Boolean isDeleted = false;
        try {
            if (StringUtils.isNotBlank(fileName)) {
                File directory = new File(path);
                Collection<File> files = FileUtils.listFiles(directory, new WildcardFileFilter(fileName + ".*"), null);
                Iterator iterator = files.iterator();
                while (iterator.hasNext()) {
                    File file = (File) iterator.next();
                    isDeleted = file.delete();
                }
                logger.info("CertificateGeneratorActor: cleanUp completed: " + isDeleted);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

}
