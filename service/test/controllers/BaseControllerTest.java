package controllers;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sunbird.es.ElasticSearchUtil;
import org.sunbird.response.Response;

import akka.actor.ActorRef;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;


@RunWith(PowerMockRunner.class)
@PrepareForTest({org.sunbird.Application.class, BaseController.class, ActorRef.class, Await.class, ElasticSearchUtil.class})
@PowerMockIgnore({"javax.management.*", "javax.net.ssl.*", "javax.security.*"})

public class BaseControllerTest {
  private org.sunbird.Application application;
  private static ActorRef actorRef;
  private static BaseController baseController;

  public BaseControllerTest() {
    baseControllerTestsetUp();
  }

  public void baseControllerTestsetUp() {
    try{
      application = PowerMockito.mock(org.sunbird.Application.class);
      PowerMockito.mockStatic(org.sunbird.Application.class);
      PowerMockito.when(org.sunbird.Application.getInstance()).thenReturn(application);
      application.init();
      mockRequestHandler();
      PowerMockito.mockStatic(ElasticSearchUtil.class);
      PowerMockito.doNothing().when(ElasticSearchUtil.class, "initialiseESClient", Mockito.anyString(), Mockito.anyString());
    }
    catch (Exception e){
      e.printStackTrace();
    }
  }

    
  public void mockRequestHandler() {

    try {
      baseController = Mockito.mock(BaseController.class);
      actorRef = Mockito.mock(ActorRef.class);
      Mockito.when(baseController.getActorRef(Mockito.anyString())).thenReturn(actorRef);
      PowerMockito.mockStatic(Await.class);
      PowerMockito.when(Await.result(Mockito.any(Future.class), Mockito.any(FiniteDuration.class)))
              .thenReturn(getResponseObject());
    }catch (Exception ex) {
    }
  }

  private Response getResponseObject() {

    Response response = new Response();
    response.put("ResponseCode", "success");
    return response;
  }
  
  @Test
  public void getTimeStampSuccess() {
	 Long val = new BaseController().getTimeStamp();
	 Assert.assertTrue(val<=System.currentTimeMillis());
  }
}