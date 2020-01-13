package org.sunbird.message;

/**
 * This interface will hold all the response key and message
 *
 * @author Amit Kumar
 */
public interface IResponseMessage {

  String INVALID_REQUESTED_DATA = "INVALID_REQUESTED_DATA";
  String INVALID_OPERATION_NAME = "INVALID_OPERATION_NAME";
  String INTERNAL_ERROR = "INTERNAL_ERROR";
  String UNAUTHORIZED = "UNAUTHORIZED";
  String MANDATORY_PARAMETER_MISSING = "Mandatory parameter {0} is missing.";
  String INVALID_PARAM_VALUE = "Invalid value {0} for parameter {1}.";
  String ERROR_UPLOADING_CERTIFICATE="ERROR_UPLOADING_CERTIFICATE";
  String ERROR_GENERATING_CERTIFICATE = "ERROR_GENERATING_CERTIFICATE";
}
