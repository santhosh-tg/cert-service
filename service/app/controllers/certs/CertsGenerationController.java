package controllers.certs;

import java.util.concurrent.CompletionStage;

import org.sunbird.cert.actor.operation.CertActorOperation;

import controllers.BaseController;
import org.sunbird.request.Request;
import play.mvc.Result;

/**
 * This controller is responsible for certificate generation.
 * @author manzarul
 *
 */
public class CertsGenerationController  extends BaseController{


	/**
	   * This method will accept request for certificate generation.
	   * it will do request validation and processing of request.
	   * @return a CompletableFuture of success response
	   */
	  public CompletionStage<Result> generateCerificate() {
		CompletionStage<Result> response = handleRequest(request(),
				request -> {
					Request req = (Request) request;
					new CertValidator().validateGenerateCertRequest(req);
					return null;
					},
				CertActorOperation.GENERATE_CERTIFICATE.getOperation());
	    return response;
	  }

	  public CompletionStage<Result> generateSignUrl() {
			CompletionStage<Result> response = handleRequest(request(),
					null,
					CertActorOperation.GET_SIGN_URL.getOperation());
		    return response;
		  }

    public CompletionStage<Result> verifyCerificate() {
        CompletionStage<Result> response = handleRequest(request(),
                request -> {
                    Request req = (Request) request;
                    new VerificationReqValidator().validateVerificationRequest(req);
                    return null;
                },
                CertActorOperation.VERIFY_CERT.getOperation());
        return response;
    }
}