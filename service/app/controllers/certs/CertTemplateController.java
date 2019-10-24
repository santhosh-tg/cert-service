package controllers.certs;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.BaseController;
import org.sunbird.response.Response;
import play.libs.Json;
import play.mvc.Result;
import play.mvc.Results;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class CertTemplateController extends BaseController {

    public CompletionStage<Result> create() {
        return returnResponse();
    }

    public CompletionStage<Result> update(String identifier) {
        return returnResponse();
    }

    public CompletionStage<Result> read(String identifier) {
        Response response = new Response();
        Map<String, Object> template = new HashMap<String, Object>() {{
           put("identifier", identifier);
           put("name", "Course completion certificate");
           put("template", "https://drive.google.com/uc?authuser=1&id=1ryB71i0Oqn2c3aqf9N6Lwvet-MZKytoM&export=download");
           put("params", Arrays.asList("issuer.name", "issuer.url"));
        }};
        response.put("template", template);
        CompletableFuture<JsonNode> future = new CompletableFuture<>();
        future.complete(Json.toJson(response));
        return future.thenApplyAsync(Results::ok, httpExecutionContext.current());
    }

    private CompletionStage<Result> returnResponse() {
        Response response = new Response();
        CompletableFuture<JsonNode> future = new CompletableFuture<>();
        future.complete(Json.toJson(response));
        return future.thenApplyAsync(Results::ok, httpExecutionContext.current());
    }
}
