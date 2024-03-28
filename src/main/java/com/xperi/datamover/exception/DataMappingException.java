/** */
package com.xperi.datamover.exception;

/**
 * This is a custom exception class which will be used during mapping between AssetJobDto object and
 * AssetJobEntity object and vice-versa
 */
public class DataMappingException extends RuntimeException {

  public DataMappingException() {}

  /** @param message */
  public DataMappingException(String message) {
    super(message);
  }

  /** @param cause */
  public DataMappingException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public DataMappingException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public DataMappingException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
