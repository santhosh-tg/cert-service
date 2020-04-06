package org.incredible.certProcessor.views;

import org.incredible.pojos.CertificateExtension;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

@PrepareForTest({PdfConverter.class})
@RunWith(PowerMockRunner.class)
public class HtmlGeneratorTest {

  File htmlFile = null;
  public static HtmlGenerator htmlGenerator;
  public static CertificateExtension certificateExtension;

  @Before
  public void setUp() {
    String htmlContent = "";
    htmlGenerator = new HtmlGenerator(htmlContent);
    certificateExtension = new CertificateExtension("");
    htmlFile = new File("src/test/resources/ValidTemplate.html");
    PowerMockito.mockStatic(PdfConverter.class);
  }

  @Test
  public void generate() throws Exception {
    String directory = htmlFile.getParent();
    certificateExtension.setId("https://dev.sunbirded.org/certs/01284093466818969624/41eac01b-da5d-4cf6-b101-bc173d8d5205");
    PowerMockito.doNothing().when(PdfConverter.class, "convertor", Mockito.any(File.class), Mockito.anyString(), Mockito.anyString());
    htmlGenerator.generate(certificateExtension ,directory);
    PowerMockito.verifyStatic(VerificationModeFactory.times(1));
    PdfConverter.convertor(Mockito.any(File.class), Mockito.anyString(), Mockito.anyString());
  }
}