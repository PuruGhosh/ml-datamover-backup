package com.xperi.datamover.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;

@Slf4j
public class FileUtil {

  private FileUtil(){}

  public static String getSha256(String fileName){
    try{
      var messageDigest = MessageDigest.getInstance("SHA-256");
      var fileStream = Files.newInputStream((Paths.get(fileName)));
      var digestFileStream = new DigestInputStream(fileStream, messageDigest);
      while(digestFileStream.read() != -1){}
      byte[] bytes = messageDigest.digest();
      var bigInt = new BigInteger(1, bytes);
      String hash = bigInt.toString(16);
      if(hash.length()<64){
        var hexString = new StringBuilder(bigInt.toString(16));
        while(hexString.length()<64){
          hexString.insert(0, '0');
        }
        hash = hexString.toString();
      }
      return hash;
    } catch (Exception e){
      log.error("Exception occurred while hashing file {}", fileName, e);
      return null;
    }
  }
}