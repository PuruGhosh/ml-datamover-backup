package com.xperi.datamover.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xperi.datamover.exception.DataMoverException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

public class TokenUtil {
  private TokenUtil() {}

  private static final String KEY_ROLE = "roles";

  /**
   * This method is used to decode jwtTokenPayload (i.e. it is saved in Http Header with the name
   * jwt-xperi-claim which is coming from Istio Envoy Proxy) in base64 format.
   *
   * @param encodedJwtTokenPayload
   * @return Map of JWT Token Payload
   * @throws JsonProcessingException
   */
  private static Map<String, String> parseJwtTokenPayload(String encodedJwtTokenPayload)
      throws JsonProcessingException {
    var decodedBytes = Base64.getDecoder().decode(encodedJwtTokenPayload);
    var decodedString = new String(decodedBytes);
    var objectMapper = new ObjectMapper();
    TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {};
    return objectMapper.readValue(decodedString, typeRef);
  }

  /**
   * This method is used to get the user roles from encoded JWT Token Payload (i.e. it is saved in
   * Http Header with the name jwt-xperi-claim which is coming from Istio Envoy Proxy)
   *
   * @param encodedJwtTokenPayload
   * @return List of user roles
   * @throws JsonProcessingException
   */
  public static List<String> getUserRoles(String encodedJwtTokenPayload)
      throws JsonProcessingException {
    var tokenMap = parseJwtTokenPayload(encodedJwtTokenPayload);
    var roleString = tokenMap.get(KEY_ROLE);
    if (StringUtils.hasText(roleString)) {
      return Arrays.stream(roleString.split(",")).map(String::trim).collect(Collectors.toList());
    } else {
      throw new DataMoverException("token '%s' not found".formatted(KEY_ROLE));
    }
  }
}
