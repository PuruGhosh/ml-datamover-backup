/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.xperi.schema.metadata;
@org.apache.avro.specific.AvroGenerated
public enum MetadataType implements org.apache.avro.generic.GenericEnumSymbol<MetadataType> {
  XML, CSV, JSON, TEXT, UNKNOWN  ;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"enum\",\"name\":\"MetadataType\",\"namespace\":\"com.xperi.schema.metadata\",\"symbols\":[\"XML\",\"CSV\",\"JSON\",\"TEXT\",\"UNKNOWN\"]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
}