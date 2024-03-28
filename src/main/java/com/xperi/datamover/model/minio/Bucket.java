package com.xperi.datamover.model.minio;

import lombok.Data;

import java.util.Map;

/**
 * This class contains the properties for Minio Bucket object. These properties are used during
 * parsing Minio event.
 */
@Data
public class Bucket {

    private String name;
    private Map<String, String> ownerIdentity;
}
