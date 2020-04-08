package org.sunbird;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.incredible.certProcessor.JsonKey;
import org.incredible.certProcessor.views.HTMLVarResolver;
import org.incredible.certProcessor.views.HTMLVars;
import org.incredible.pojos.CertificateExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.capitalize;

public class PdfGenerator {

    private static Logger logger = LoggerFactory.getLogger(PdfGenerator.class);
    private static ObjectMapper mapper = new ObjectMapper();
    private static final String PRINT_SERVICE_URL = "print-service:5000/v1/print/pdf";

    public static String generate(String htmlTemplateUrl, CertificateExtension certificateExtension , String qrImageUrl,
                                  String container, String path) throws IOException {
        Map<String, Object> printServiceReq = new HashMap<>();
        Map<String, Object> request = new HashMap<>();
        printServiceReq.put(JsonKey.REQUEST, request);
        request.put("context", getContext(certificateExtension, qrImageUrl));
        request.put(JsonKey.HTML_TEMPLATE, htmlTemplateUrl);
        Map<String, String> storageParams = new HashMap<>();
        storageParams.put(JsonKey.containerName,container);
        storageParams.put(JsonKey.PATH,path);
        request.put("storageParams",storageParams);
        String pdfUrl = callPrintService(printServiceReq);
        String [] arr = pdfUrl.split("/");
        return "/"+path+arr[arr.length-1];
    }

    private static  Map<String,Object> getContext(CertificateExtension certificateExtension, String qrImageUrl) {
        Map<String,Object> context = new HashMap<>();
        HTMLVarResolver htmlVarResolver = new HTMLVarResolver(certificateExtension);
        List<String> supportedVarList = HTMLVars.get();
        Iterator<String> iterator = supportedVarList.iterator();
        while (iterator.hasNext()) {
            String macro = iterator.next().substring(1);
            try {
                Method method = htmlVarResolver.getClass().getMethod("get" + capitalize(macro));
                method.setAccessible(true);
                context.put(macro, method.invoke(htmlVarResolver));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                logger.error("exception "+ e.getMessage(),e);
            }
        }
        context.put("qrCodeImage",qrImageUrl);
        return context;
    }

    private static String callPrintService(Map<String, Object> request) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(PRINT_SERVICE_URL);
        String json = mapper.writeValueAsString(request);
        json = new String(json.getBytes(), StandardCharsets.UTF_8);
        StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        HttpResponse response = client.execute(httpPost);
        String pdfUrl = generateResponse(response);
        return pdfUrl;
    }

    private static String generateResponse(HttpResponse httpResponse) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader br =
                new BufferedReader(new InputStreamReader((httpResponse.getEntity().getContent())));
        String output;
        while ((output = br.readLine()) != null) {
            builder.append(output);
        }
        Map<String,Object> resMap = mapper.readValue(builder.toString(),Map.class);
        Map<String,Object> printResponse = (Map<String,Object>)resMap.get(JsonKey.RESULT);
        String pdfUrl = (String)(printResponse.get(JsonKey.PDF_URL));
        return pdfUrl;
    }

}
