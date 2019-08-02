package org.sunbird.cert.actor;

import org.apache.log4j.Logger;
import org.sunbird.BaseActor;
import org.sunbird.actor.core.ActorConfig;
import org.sunbird.request.Request;
import org.sunbird.response.Response;

/**
 * This actor is responsible for certificate generation.
 * @author manzarul
 *
 */
@ActorConfig(
        tasks = {"generateCert"},
        asyncTasks = {}
)
public class CertificateGeneratorActor extends BaseActor {
	private Logger logger = Logger.getLogger(CertificateGeneratorActor.class);

	@Override
	public void onReceive(Request request) throws Throwable {
		String operation = request.getOperation();
		logger.info("onReceive method call start for operation " + operation);
		switch (operation) {
		case "generateCert":
			generateCertificate(request);
			break;

		default:
			onReceiveUnsupportedMessage(this.getClass().getName());
			break;
		}
		logger.info("onReceive method call End");
	}

	private void generateCertificate(Request request) {
		logger.info("Request received==" + request.getRequest());
		Response response = new Response();
		response.getResult().put("response", "Success");
		sender().tell(response, getSelf());
		logger.info("onReceive method call End");
	}

}
