package org.sunbird.cert.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.math3.analysis.function.Power;
import org.incredible.CertificateGenerator;
import org.incredible.UrlManager;
import org.incredible.certProcessor.CertModel;
import org.incredible.certProcessor.CertificateFactory;
import org.incredible.certProcessor.qrcode.AccessCodeGenerator;
import org.incredible.certProcessor.qrcode.QRCodeGenerationModel;
import org.incredible.certProcessor.qrcode.utils.QRCodeImageGenerator;
import org.incredible.certProcessor.signature.exceptions.SignatureException;
import org.incredible.certProcessor.store.*;
import org.incredible.certProcessor.views.HTMLTemplateProvider;
import org.incredible.certProcessor.views.HTMLTemplateZip;
import org.incredible.certProcessor.views.HeadlessChromeHtmlToPdfConverter;
import org.incredible.certProcessor.views.HtmlGenerator;
import org.incredible.pojos.CertificateExtension;
import org.incredible.pojos.CertificateResponse;
import org.incredible.pojos.ob.exeptions.InvalidDateFormatException;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.BaseException;
import org.sunbird.CertMapper;
import org.sunbird.CertsConstant;
import org.sunbird.JsonKey;
import org.sunbird.cert.actor.CertificateGeneratorActor;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.exception.StorageServiceException;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;
import org.sunbird.message.Localizer;
import org.sunbird.message.ResponseCode;
import org.sunbird.request.Request;
import org.sunbird.response.Response;
import scala.concurrent.duration.Duration;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.cert.CertStore;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({HeadlessChromeHtmlToPdfConverter.class,
        CloudStorage.class,
        ICertStore.class,
        Localizer.class,
        CertificateGenerator.class,
        AzureStore.class,
        FileUtils.class,
        File.class,
        ObjectMapper.class,
        StorageServiceFactory.class})
@PowerMockIgnore("javax.management.*")
public class CertificateGeneratorActorTest {
    private static ActorSystem system = ActorSystem.create("system");
    private static final Props props = Props.create(CertificateGeneratorActor.class);

    @BeforeClass
    public static void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void generateCertificate() throws Exception {
        Request request = createCertRequest();
        mock(request);
        TestKit probe = new TestKit(system);
        ActorRef subject = system.actorOf(props);
        subject.tell(request, probe.getRef());
        Response res = probe.expectMsgClass(Duration.create(10, TimeUnit.SECONDS),Response.class);
        Assert.assertTrue(null != res && res.getResponseCode() == ResponseCode.OK);
    }

    private void mock(Request request) throws Exception {
        PowerMockito.mockStatic(HeadlessChromeHtmlToPdfConverter.class);
        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.mockStatic(Localizer.class);
        when(Localizer.getInstance()).thenReturn(null);
        ObjectMapper mapper = PowerMockito.mock(ObjectMapper.class);
        PowerMockito.whenNew(ObjectMapper.class).withNoArguments().thenReturn(mapper);
        CertStoreFactory certStoreFactory = PowerMockito.mock(CertStoreFactory.class);
        PowerMockito.whenNew(CertStoreFactory.class).withArguments(Mockito.anyMap()).thenReturn(certStoreFactory);
        StoreConfig storeParams = PowerMockito.mock(StoreConfig.class);
        PowerMockito.whenNew(StoreConfig.class).withArguments(Mockito.anyMap()).thenReturn(storeParams);
        ICertStore htmlTemplateStore = PowerMockito.mock(ICertStore.class);
        when(certStoreFactory.getHtmlTemplateStore(Mockito.anyString(),Mockito.any(StoreConfig.class))).thenReturn(htmlTemplateStore);
        ICertStore certStore = PowerMockito.mock(ICertStore.class);
        when(certStoreFactory.getCertStore(Mockito.any(StoreConfig.class),Mockito.anyBoolean())).thenReturn(certStore);
        CertMapper certMapper = PowerMockito.mock(CertMapper.class);
        PowerMockito.whenNew(CertMapper.class).withArguments(Mockito.anyMap()).thenReturn(certMapper);
        HTMLTemplateZip hTMLTemplateZip = PowerMockito.mock(HTMLTemplateZip.class);
        PowerMockito.whenNew(HTMLTemplateZip.class).withArguments(Mockito.any(ICertStore.class),Mockito.anyString()).thenReturn(hTMLTemplateZip);
        PowerMockito.when(certStoreFactory.getDirectoryName(Mockito.anyString())).thenReturn("directory");
        CertificateGenerator certificateGenerator = PowerMockito.mock(CertificateGenerator.class);
        PowerMockito.whenNew(CertificateGenerator.class).withArguments(Mockito.anyMap(),Mockito.anyString()).thenReturn(certificateGenerator);
        CertificateResponse certificateResponse = PowerMockito.mock(CertificateResponse.class);
        CertificateResponse certificateResponse2 = PowerMockito.mock(CertificateResponse.class);
        PowerMockito.whenNew(CertificateResponse.class).withNoArguments().thenReturn(certificateResponse).thenReturn(certificateResponse2);
        PowerMockito.whenNew(CertificateResponse.class).withArguments(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(),Mockito.anyString()).thenReturn(certificateResponse2);
        //CertificateResponse(uuid, accessCode, jsonData, certModel.getIdentifier());
        CertModel certModel = PowerMockito.mock(CertModel.class);
        List<CertModel> certModelList = new ArrayList<>();
        certModelList.add(certModel);
        PowerMockito.when(certMapper.toList(Mockito.anyMap())).thenReturn(certModelList);
        CertificateExtension certificateExtension = PowerMockito.mock(CertificateExtension.class);
        CertificateFactory certificateFactory = PowerMockito.mock(CertificateFactory.class);
        PowerMockito.whenNew(CertificateFactory.class).withNoArguments().thenReturn(certificateFactory);
        PowerMockito.when(certificateFactory.createCertificate(Mockito.any(CertModel.class),Mockito.anyMap())).thenReturn(certificateExtension);
        PowerMockito.when(certificateExtension.getId()).thenReturn("AnyString").thenReturn("anotherString");
        AccessCodeGenerator accessCodeGenerator = PowerMockito.mock(AccessCodeGenerator.class);
        PowerMockito.whenNew(AccessCodeGenerator.class).withArguments(Mockito.anyDouble()).thenReturn(accessCodeGenerator);
        PowerMockito.when(accessCodeGenerator.generate()).thenReturn("anyString");
        QRCodeGenerationModel qrCodeGenerationModel = PowerMockito.mock(QRCodeGenerationModel.class);
        PowerMockito.whenNew(QRCodeGenerationModel.class).withNoArguments().thenReturn(qrCodeGenerationModel);
        QRCodeImageGenerator qrCodeImageGenerator = PowerMockito.mock(QRCodeImageGenerator.class);
        PowerMockito.whenNew(QRCodeImageGenerator.class).withNoArguments().thenReturn(qrCodeImageGenerator);
        File newFile = PowerMockito.mock(File.class);
        PowerMockito.when(qrCodeImageGenerator.createQRImages(qrCodeGenerationModel)).thenReturn(newFile);
        HTMLTemplateProvider hTMLTemplateProvider = PowerMockito.mock(HTMLTemplateProvider.class);
        PowerMockito.when(hTMLTemplateProvider.getTemplateContent(Mockito.anyString())).thenReturn("anyString");
        PowerMockito.when(certificateGenerator.createCertificate(Mockito.any(CertModel.class),Mockito.any(HTMLTemplateZip.class))).thenReturn(certificateResponse2);
        PowerMockito.when(storeParams.getType()).thenReturn("Mockito.anyString()");
        File file6 = PowerMockito.mock(File.class);
        PowerMockito.when(FileUtils.getFile(Mockito.anyString())).thenReturn(file6);
        when(file6.exists()).thenReturn(true);
        AzureStoreConfig azureStoreConfig = PowerMockito.mock(AzureStoreConfig.class);
        PowerMockito.when(storeParams.getAzureStoreConfig()).thenReturn(azureStoreConfig);
        PowerMockito.when(azureStoreConfig.getAccount()).thenReturn("Mockito.anyString()");
        PowerMockito.when(azureStoreConfig.getKey()).thenReturn("Mockito.anyString()");
        StorageConfig storageConfig = PowerMockito.mock(StorageConfig.class);
        PowerMockito.when(storeParams.getType()).thenReturn("Mockito.anyString()");
        PowerMockito.whenNew(StorageConfig.class).withArguments(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()).thenReturn(storageConfig);
        BaseStorageService storageService = PowerMockito.mock(BaseStorageService.class);
        PowerMockito.mockStatic(StorageServiceFactory.class);
        PowerMockito.when(StorageServiceFactory.getStorageService(Mockito.any(StorageConfig.class))).thenReturn(storageService);
        PowerMockito.when(certStore.save(Mockito.any(File.class), Mockito.anyString())).thenReturn("Mockito.anyString()");
        PowerMockito.when(certificateResponse2.getUuid()).thenReturn("Mockito.anyString()");
        PowerMockito.when(certificateResponse2.getRecipientId()).thenReturn("Mockito.anyString()");
        PowerMockito.when(certificateResponse2.getAccessCode()).thenReturn("Mockito.anyString()");
        PowerMockito.when(certificateResponse2.getJsonData()).thenReturn("{\"name\":\"john\",\"age\":22,\"class\":\"mca\"}");
        Mockito.when(mapper.readValue(Mockito.anyString(), (TypeReference) Mockito.any(Map.class))).thenReturn(new HashMap());
        File file2 = PowerMockito.mock(File.class);
        File file3 = PowerMockito.mock(File.class);
        List<File> fileList = new ArrayList<>();
        fileList.add(file2);
        WildcardFileFilter wildcardFileFilter = PowerMockito.mock(WildcardFileFilter.class);
        PowerMockito.when(FileUtils.listFiles(Mockito.any(File.class),Mockito.any(WildcardFileFilter.class),Mockito.any())).thenReturn(fileList);
        PowerMockito.when(file3.delete()).thenReturn(true);
        Iterator iterator = PowerMockito.mock(Iterator.class);
        PowerMockito.when(iterator.hasNext()).thenReturn(false);
    }

    private Request createCertRequest() {
        Request reqObj = new Request();
        reqObj.setOperation(JsonKey.GENERATE_CERT);
        Map<String, Object> innerMap = new HashMap<>();
        List<Map<String, Object>> listOfData = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        data.put(JsonKey.RECIPIENT_NAME, "name");
        listOfData.add(data);
        Map<String, Object> issuer = new HashMap<>();
        issuer.put(JsonKey.NAME, "issuer name");
        issuer.put(JsonKey.URL, "issuer url");
        List<Map<String, Object>> signatoryList = new ArrayList<>();
        Map<String, Object> signatory = new HashMap<>();
        signatory.put(JsonKey.NAME, "signatory name");
        signatoryList.add(signatory);
        Map<String,Object>storeMap=new HashMap<>();
        storeMap.put("cloudStore",false);
        innerMap.put(JsonKey.STORE,storeMap);
        innerMap.put(JsonKey.SIGNATORY_LIST, signatoryList);
        innerMap.put(JsonKey.DATA, listOfData);
        innerMap.put(JsonKey.KEYS, null);
        innerMap.put(JsonKey.ISSUER, issuer);
        innerMap.put(JsonKey.HTML_TEMPLATE, "https://drive.google.com/a/ilimi.in/uc?authuser=1&id=16WgZrm-1Dh44uFryMTo_0uVjZv65mp4u&export=download");
        reqObj.getRequest().put(JsonKey.CERTIFICATE, innerMap);
        return reqObj;
    }

}