package com.xperi.datamover.advice;

import com.xperi.datamover.dto.RestResponse;
import com.xperi.datamover.exception.DataMoverException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/** Handle REST Controller exceptions */
@RestControllerAdvice
@Slf4j
public class RestControllerExceptionHandler {

  /**
   * Handle all request fields validation errors here
   *
   * @param e MethodArgumentNotValidException
   * @return RestResponse with errors
   */
  @ExceptionHandler(value = MethodArgumentNotValidException.class)
  public ResponseEntity<RestResponse<Map<String, String>>> handleValidationException(
      MethodArgumentNotValidException e) {
    final RestResponse<Map<String, String>> errors = new RestResponse<>();
    errors.addError("Data validation error");
    final Map<String, String> fieldMap = new HashMap<>();
    errors.setData(fieldMap);
    e.getBindingResult()
        .getAllErrors()
        .forEach(
            (error) -> {
              final String fieldName = ((FieldError) error).getField();
              final String errorMessage = error.getDefaultMessage();
              fieldMap.put(fieldName, errorMessage);
            });
    log.error("Data validation errors {}", e.getLocalizedMessage(), e);
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handle all HTTP message conversion exceptions here
   *
   * @param e HttpMessageConversionException
   * @return ResponseEntity with error
   */
  @ExceptionHandler(value = HttpMessageConversionException.class)
  public ResponseEntity<RestResponse<?>> handleMessageConversion(HttpMessageConversionException e) {
    final RestResponse<?> errors = new RestResponse<>();
    errors.addError("Error in parsing data");
    log.error("Failed to parse message {}", e.getLocalizedMessage(), e);
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handle all custom data mover exceptions here
   *
   * @param e DataMoverException
   * @return ResponseEntity with error
   */
  @ExceptionHandler(value = DataMoverException.class)
  public ResponseEntity<RestResponse<?>> handleDataMoverException(DataMoverException e) {
    final RestResponse<?> errors = new RestResponse<>();
    errors.addError(e.getLocalizedMessage());
    log.error("Error occurred: {}", e.getLocalizedMessage(), e);
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handle if request body is empty
   *
   * @param e IllegalArgumentException
   * @return ResponseEntity with error
   */
  @ExceptionHandler(value = IllegalArgumentException.class)
  public ResponseEntity<RestResponse<?>> handleIllegalArgumentException(
      IllegalArgumentException e) {
    final RestResponse<?> errors = new RestResponse<>();
    errors.addError(e.getLocalizedMessage());
    log.error("Error occurred: {}", e.getLocalizedMessage(), e);
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
  }

  /**
   * Handle global exception here
   *
   * @param e Exception
   * @return ResponseEntity with error
   */
  @ExceptionHandler(value = Exception.class)
  public ResponseEntity<RestResponse<?>> handleGlobalException(Exception e) {
    final RestResponse<?> errors = new RestResponse<>();
    errors.addError("System Error. Please contact support");
    log.error("Internal Server Error {}", e.getLocalizedMessage(), e);
    return new ResponseEntity<>(errors, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
