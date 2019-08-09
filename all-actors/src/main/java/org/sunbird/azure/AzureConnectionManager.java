/** */
package org.sunbird.azure;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.sunbird.JsonKey;
import org.sunbird.cert.actor.CertificateGeneratorActor;


/**
 * This class will manage azure connection.
 *
 * @author Manzarul
 */
public class AzureConnectionManager {
  private static Logger logger = Logger.getLogger(AzureConnectionManager.class);
  private static String accountName = "";
  private static String accountKey = "";
  private static String storageAccountString;
  private static AzureConnectionManager connectionManager;

  static {
    String name = System.getenv(JsonKey.AZURE_STORAGE_KEY);
    String key = System.getenv(JsonKey.AZURE_STORAGE_SECRET);
    if (StringUtils.isBlank(name) || StringUtils.isBlank(key)) {
      storageAccountString =
          "DefaultEndpointsProtocol=https;AccountName="
              + accountName
              + ";AccountKey="
              + accountKey
              + ";EndpointSuffix=core.windows.net";
    } else {
      accountName = name;
      accountKey = key;

      storageAccountString =
          "DefaultEndpointsProtocol=https;AccountName="
              + accountName
              + ";AccountKey="
              + accountKey
              + ";EndpointSuffix=core.windows.net";
    }
  }

  private AzureConnectionManager() throws CloneNotSupportedException {
    if (connectionManager != null) throw new CloneNotSupportedException();
  }

  /**
   * This method will provide Azure CloudBlobContainer object or in case of error it will provide
   * null;
   *
   * @param containerName String
   * @return CloudBlobContainer or null
   */
  public static CloudBlobContainer getContainer(String containerName, boolean isPublicAccess) {

    try {
      CloudBlobClient cloudBlobClient = getBlobClient();
      // Get a reference to a container , The container name must be lower case
      CloudBlobContainer container =
          cloudBlobClient.getContainerReference(containerName.toLowerCase(Locale.ENGLISH));
      // Create the container if it does not exist.
      boolean response = container.createIfNotExists();
      logger.info("container creation done if not exist==" + response);
      // Create a permissions object.
      if (isPublicAccess) {
        BlobContainerPermissions containerPermissions = new BlobContainerPermissions();
        // Include public access in the permissions object.
        containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);
        // Set the permissions on the container.
        container.uploadPermissions(containerPermissions);
      }
      return container;
    } catch (Exception e) {
      logger.info(e.getMessage(), e);
    }
    return null;
  }

  public static CloudBlobContainer getContainerReference(String containerName) {

    CloudBlobContainer container = null;
    try {
      // Create the blob client.
      CloudBlobClient blobClient = getBlobClient();
      // Retrieve reference to a previously created container.
      container = blobClient.getContainerReference(containerName.toLowerCase(Locale.ENGLISH));
      if (container.exists()) {
        return container;
      }
    } catch (URISyntaxException e) {
      logger.info(e.getMessage(), e);
    } catch (StorageException e) {
      logger.info(e.getMessage(), e);
    }
    logger.info("Container does not exist ==" + containerName);
    return null;
  }

  private static CloudBlobClient getBlobClient() {

    // Retrieve storage account from connection-string.
    CloudStorageAccount storageAccount = null;
    CloudBlobClient blobClient = null;
    try {
      storageAccount = CloudStorageAccount.parse(storageAccountString);
      // Create the blob client.
      blobClient = storageAccount.createCloudBlobClient();
    } catch (URISyntaxException e) {
      logger.info(e.getMessage(), e);
    } catch (InvalidKeyException e) {
      logger.info(e.getMessage(), e);
    }
    return blobClient;
  }
}
