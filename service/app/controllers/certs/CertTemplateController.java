package controllers.certs;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.BaseController;
import controllers.RequestHandler;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.sunbird.BaseException;
import org.sunbird.es.ElasticSearchUtil;
import org.sunbird.message.ResponseCode;
import org.sunbird.request.Request;
import org.sunbird.response.Response;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;
import utils.RequestMapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CertTemplateController extends BaseController {

    private static String indexName = "cert-templates";
    private static String docType = "_doc";

    public CompletionStage<Result> create() {
        try {
            Map<String, Object> template = getTemplate(getRequest(request()));
            validateTemplate(template);
            String identifier = (String) template.get("identifier");
            if (StringUtils.isBlank(identifier))
                identifier = UUID.randomUUID() + "";
            ElasticSearchUtil.addDocument(indexName, docType, template, identifier);
            Map<String, Object> result = new HashMap<>();
            result.put("identifier", identifier);
            return returnResponse(result);
        } catch (Exception ex) {
            ex.printStackTrace();
            return RequestHandler.handleFailureResponse(ex, httpExecutionContext);
        }
    }

    public CompletionStage<Result> update(String identifier) {
        Map<String, Object> result = new HashMap<>();
        result.put("identifier", identifier);
        return returnResponse(result);
    }

    public CompletionStage<Result> read(String identifier) {
        CompletableFuture<JsonNode> future = new CompletableFuture<>();
        Response response = new Response();
        try {
            Map<String, Object> template = ElasticSearchUtil.getDocument(indexName, docType, identifier);
            response.put("certificate", new HashMap<String, Object>() {{
                put("template", template);
            }});
            future.complete(Json.toJson(response));
            return future.thenApplyAsync(Results::ok, httpExecutionContext.current());
        } catch (Exception ex) {
            ex.printStackTrace();
            return RequestHandler.handleFailureResponse(ex, httpExecutionContext);
        }
    }

    private Request getRequest(play.mvc.Http.Request req) throws Exception {
        Request request = new Request();
        if (req.body() != null && req.body().asJson() != null) {
            request = (Request) RequestMapper.mapRequest(req, Request.class);
        }
        return request;
    }

    private Map<String, Object> getTemplate(Request request) {
        Map<String, Object> cert = (Map<String, Object>) request.getRequest().get("certificate");
        if (MapUtils.isNotEmpty(cert)) {
            Map<String, Object> template = (Map<String, Object>) cert.get("template");
            if (MapUtils.isNotEmpty(template)) {
                return template;
            }
        }
        return null;
    }

    private void validateTemplate(Map<String, Object> template) throws BaseException {
        if (MapUtils.isNotEmpty(template)) {
            if (!template.keySet().containsAll(Arrays.asList("name", "template")))
                throw new BaseException("CLIENT_ERROR", "name or template missing in request.", ResponseCode.BAD_REQUEST.getCode());
        } else {
            throw new BaseException("CLIENT_ERROR", "Request is empty.", ResponseCode.BAD_REQUEST.getCode());
        }
    }

    private CompletionStage<Result> returnResponse(Map<String, Object> result) {
        Response response = new Response();
        response.putAll(result);
        CompletableFuture<JsonNode> future = new CompletableFuture<>();
        future.complete(Json.toJson(response));
        return future.thenApplyAsync(Results::ok, httpExecutionContext.current());
    }
}
