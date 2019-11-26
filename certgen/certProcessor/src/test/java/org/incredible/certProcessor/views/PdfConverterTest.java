package org.incredible.certProcessor.views;

import com.itextpdf.html2pdf.HtmlConverter;
/*import com.itextpdf.licensekey.LicenseKey;*/
import org.incredible.certProcessor.JsonKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.InputStream;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PdfConverter.class, HtmlConverter.class})
@PowerMockIgnore("javax.management.*")
public class PdfConverterTest {

  File htmlFile = null;

  @Before
  public void setUp() {
    htmlFile = new File("src/test/resources/ValidTemplate.html");
  }

  @Test
  public void convertorTest() throws Exception {
    String certUuid = "";
    String directory = htmlFile.getParent();
    mockStatic(HtmlConverter.class);
    mockStatic(System.class);
    /*mockStatic(LicenseKey.class);*/
    PowerMockito.when(System.getenv(Mockito.anyString())).thenReturn(licenseDetails(JsonKey.ITEXT_LICENSE_ENABLED)).
            thenReturn(licenseDetails(JsonKey.ITEXT_LICENSE_PATH));
    PowerMockito.doNothing().when(HtmlConverter.class, "convertToPdf", Mockito.any(File.class), Mockito.any(File.class));
    /*PowerMockito.doNothing().when(LicenseKey.class,"loadLicenseFile", Mockito.any(InputStream.class));*/
    PdfConverter.convertor(htmlFile, certUuid, directory);
    /*PowerMockito.verifyStatic(VerificationModeFactory.times(1));*/
    /*LicenseKey.loadLicenseFile(Mockito.any(InputStream.class));*/
    PowerMockito.verifyStatic(VerificationModeFactory.times(1));
    HtmlConverter.convertToPdf(Mockito.any(File.class), Mockito.any(File.class));
  }

  private String licenseDetails(String input) {
    if(input.equals(JsonKey.ITEXT_LICENSE_ENABLED)) {
      return "true";
    } else {
      return "resources";
    }
  }

}