package com.xperi.datamover.exception;

/**
 * This is a generic exception class for Data Mover microservice. This exception will be thrown
 * whenever a custom exception will be required for Data Mover microservice.
 */
public class DataMoverException extends RuntimeException {
  public DataMoverException() {}

  /** @param message */
  public DataMoverException(String message) {
    super(message);
  }

  /** @param cause */
  public DataMoverException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public DataMoverException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   * @param cause
   * @param enableSuppression
   * @param writableStackTrace
   */
  public DataMoverException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
