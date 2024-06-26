/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.xperi.schema.subjobevent;

import org.apache.avro.generic.GenericArray;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@org.apache.avro.specific.AvroGenerated
public class AssetSubJobEvent extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = 2972001062684147099L;


  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"AssetSubJobEvent\",\"namespace\":\"com.xperi.schema.subjobevent\",\"fields\":[{\"name\":\"filePath\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}},{\"name\":\"jobName\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"metaDataFileUrl\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"parentJobId\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"subJobId\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"subJobType\",\"type\":{\"type\":\"enum\",\"name\":\"AssetCategory\",\"symbols\":[\"METADATA\",\"ASSET\"]}},{\"name\":\"userRoles\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static final SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<AssetSubJobEvent> ENCODER =
      new BinaryMessageEncoder<AssetSubJobEvent>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<AssetSubJobEvent> DECODER =
      new BinaryMessageDecoder<AssetSubJobEvent>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageEncoder instance used by this class.
   * @return the message encoder used by this class
   */
  public static BinaryMessageEncoder<AssetSubJobEvent> getEncoder() {
    return ENCODER;
  }

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   * @return the message decoder used by this class
   */
  public static BinaryMessageDecoder<AssetSubJobEvent> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
   */
  public static BinaryMessageDecoder<AssetSubJobEvent> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<AssetSubJobEvent>(MODEL$, SCHEMA$, resolver);
  }

  /**
   * Serializes this AssetSubJobEvent to a ByteBuffer.
   * @return a buffer holding the serialized data for this instance
   * @throws java.io.IOException if this instance could not be serialized
   */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /**
   * Deserializes a AssetSubJobEvent from a ByteBuffer.
   * @param b a byte buffer holding serialized data for an instance of this class
   * @return a AssetSubJobEvent instance decoded from the given buffer
   * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
   */
  public static AssetSubJobEvent fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

  private java.util.List<java.lang.String> filePath;
  private java.lang.String jobName;
  private java.lang.String metaDataFileUrl;
  private java.lang.String parentJobId;
  private java.lang.String subJobId;
  private com.xperi.schema.subjobevent.AssetCategory subJobType;
  private java.util.List<java.lang.String> userRoles;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public AssetSubJobEvent() {}

  /**
   * All-args constructor.
   * @param filePath The new value for filePath
   * @param jobName The new value for jobName
   * @param metaDataFileUrl The new value for metaDataFileUrl
   * @param parentJobId The new value for parentJobId
   * @param subJobId The new value for subJobId
   * @param subJobType The new value for subJobType
   * @param userRoles The new value for userRoles
   */
  public AssetSubJobEvent(java.util.List<java.lang.String> filePath, java.lang.String jobName, java.lang.String metaDataFileUrl, java.lang.String parentJobId, java.lang.String subJobId, com.xperi.schema.subjobevent.AssetCategory subJobType, java.util.List<java.lang.String> userRoles) {
    this.filePath = filePath;
    this.jobName = jobName;
    this.metaDataFileUrl = metaDataFileUrl;
    this.parentJobId = parentJobId;
    this.subJobId = subJobId;
    this.subJobType = subJobType;
    this.userRoles = userRoles;
  }

  public org.apache.avro.specific.SpecificData getSpecificData() { return MODEL$; }
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return filePath;
    case 1: return jobName;
    case 2: return metaDataFileUrl;
    case 3: return parentJobId;
    case 4: return subJobId;
    case 5: return subJobType;
    case 6: return userRoles;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: filePath = (java.util.List<java.lang.String>)value$; break;
    case 1: jobName = value$ != null ? value$.toString() : null; break;
    case 2: metaDataFileUrl = value$ != null ? value$.toString() : null; break;
    case 3: parentJobId = value$ != null ? value$.toString() : null; break;
    case 4: subJobId = value$ != null ? value$.toString() : null; break;
    case 5: subJobType = (com.xperi.schema.subjobevent.AssetCategory)value$; break;
    case 6: userRoles = (java.util.List<java.lang.String>)value$; break;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  /**
   * Gets the value of the 'filePath' field.
   * @return The value of the 'filePath' field.
   */
  public java.util.List<java.lang.String> getFilePath() {
    return filePath;
  }


  /**
   * Sets the value of the 'filePath' field.
   * @param value the value to set.
   */
  public void setFilePath(java.util.List<java.lang.String> value) {
    this.filePath = value;
  }

  /**
   * Gets the value of the 'jobName' field.
   * @return The value of the 'jobName' field.
   */
  public java.lang.String getJobName() {
    return jobName;
  }


  /**
   * Sets the value of the 'jobName' field.
   * @param value the value to set.
   */
  public void setJobName(java.lang.String value) {
    this.jobName = value;
  }

  /**
   * Gets the value of the 'metaDataFileUrl' field.
   * @return The value of the 'metaDataFileUrl' field.
   */
  public java.lang.String getMetaDataFileUrl() {
    return metaDataFileUrl;
  }


  /**
   * Sets the value of the 'metaDataFileUrl' field.
   * @param value the value to set.
   */
  public void setMetaDataFileUrl(java.lang.String value) {
    this.metaDataFileUrl = value;
  }

  /**
   * Gets the value of the 'parentJobId' field.
   * @return The value of the 'parentJobId' field.
   */
  public java.lang.String getParentJobId() {
    return parentJobId;
  }


  /**
   * Sets the value of the 'parentJobId' field.
   * @param value the value to set.
   */
  public void setParentJobId(java.lang.String value) {
    this.parentJobId = value;
  }

  /**
   * Gets the value of the 'subJobId' field.
   * @return The value of the 'subJobId' field.
   */
  public java.lang.String getSubJobId() {
    return subJobId;
  }


  /**
   * Sets the value of the 'subJobId' field.
   * @param value the value to set.
   */
  public void setSubJobId(java.lang.String value) {
    this.subJobId = value;
  }

  /**
   * Gets the value of the 'subJobType' field.
   * @return The value of the 'subJobType' field.
   */
  public com.xperi.schema.subjobevent.AssetCategory getSubJobType() {
    return subJobType;
  }


  /**
   * Sets the value of the 'subJobType' field.
   * @param value the value to set.
   */
  public void setSubJobType(com.xperi.schema.subjobevent.AssetCategory value) {
    this.subJobType = value;
  }

  /**
   * Gets the value of the 'userRoles' field.
   * @return The value of the 'userRoles' field.
   */
  public java.util.List<java.lang.String> getUserRoles() {
    return userRoles;
  }


  /**
   * Sets the value of the 'userRoles' field.
   * @param value the value to set.
   */
  public void setUserRoles(java.util.List<java.lang.String> value) {
    this.userRoles = value;
  }

  /**
   * Creates a new AssetSubJobEvent RecordBuilder.
   * @return A new AssetSubJobEvent RecordBuilder
   */
  public static com.xperi.schema.subjobevent.AssetSubJobEvent.Builder newBuilder() {
    return new com.xperi.schema.subjobevent.AssetSubJobEvent.Builder();
  }

  /**
   * Creates a new AssetSubJobEvent RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new AssetSubJobEvent RecordBuilder
   */
  public static com.xperi.schema.subjobevent.AssetSubJobEvent.Builder newBuilder(com.xperi.schema.subjobevent.AssetSubJobEvent.Builder other) {
    if (other == null) {
      return new com.xperi.schema.subjobevent.AssetSubJobEvent.Builder();
    } else {
      return new com.xperi.schema.subjobevent.AssetSubJobEvent.Builder(other);
    }
  }

  /**
   * Creates a new AssetSubJobEvent RecordBuilder by copying an existing AssetSubJobEvent instance.
   * @param other The existing instance to copy.
   * @return A new AssetSubJobEvent RecordBuilder
   */
  public static com.xperi.schema.subjobevent.AssetSubJobEvent.Builder newBuilder(com.xperi.schema.subjobevent.AssetSubJobEvent other) {
    if (other == null) {
      return new com.xperi.schema.subjobevent.AssetSubJobEvent.Builder();
    } else {
      return new com.xperi.schema.subjobevent.AssetSubJobEvent.Builder(other);
    }
  }

  /**
   * RecordBuilder for AssetSubJobEvent instances.
   */
  @org.apache.avro.specific.AvroGenerated
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<AssetSubJobEvent>
    implements org.apache.avro.data.RecordBuilder<AssetSubJobEvent> {

    private java.util.List<java.lang.String> filePath;
    private java.lang.String jobName;
    private java.lang.String metaDataFileUrl;
    private java.lang.String parentJobId;
    private java.lang.String subJobId;
    private com.xperi.schema.subjobevent.AssetCategory subJobType;
    private java.util.List<java.lang.String> userRoles;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$, MODEL$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(com.xperi.schema.subjobevent.AssetSubJobEvent.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.filePath)) {
        this.filePath = data().deepCopy(fields()[0].schema(), other.filePath);
        fieldSetFlags()[0] = other.fieldSetFlags()[0];
      }
      if (isValidValue(fields()[1], other.jobName)) {
        this.jobName = data().deepCopy(fields()[1].schema(), other.jobName);
        fieldSetFlags()[1] = other.fieldSetFlags()[1];
      }
      if (isValidValue(fields()[2], other.metaDataFileUrl)) {
        this.metaDataFileUrl = data().deepCopy(fields()[2].schema(), other.metaDataFileUrl);
        fieldSetFlags()[2] = other.fieldSetFlags()[2];
      }
      if (isValidValue(fields()[3], other.parentJobId)) {
        this.parentJobId = data().deepCopy(fields()[3].schema(), other.parentJobId);
        fieldSetFlags()[3] = other.fieldSetFlags()[3];
      }
      if (isValidValue(fields()[4], other.subJobId)) {
        this.subJobId = data().deepCopy(fields()[4].schema(), other.subJobId);
        fieldSetFlags()[4] = other.fieldSetFlags()[4];
      }
      if (isValidValue(fields()[5], other.subJobType)) {
        this.subJobType = data().deepCopy(fields()[5].schema(), other.subJobType);
        fieldSetFlags()[5] = other.fieldSetFlags()[5];
      }
      if (isValidValue(fields()[6], other.userRoles)) {
        this.userRoles = data().deepCopy(fields()[6].schema(), other.userRoles);
        fieldSetFlags()[6] = other.fieldSetFlags()[6];
      }
    }

    /**
     * Creates a Builder by copying an existing AssetSubJobEvent instance
     * @param other The existing instance to copy.
     */
    private Builder(com.xperi.schema.subjobevent.AssetSubJobEvent other) {
      super(SCHEMA$, MODEL$);
      if (isValidValue(fields()[0], other.filePath)) {
        this.filePath = data().deepCopy(fields()[0].schema(), other.filePath);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.jobName)) {
        this.jobName = data().deepCopy(fields()[1].schema(), other.jobName);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.metaDataFileUrl)) {
        this.metaDataFileUrl = data().deepCopy(fields()[2].schema(), other.metaDataFileUrl);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.parentJobId)) {
        this.parentJobId = data().deepCopy(fields()[3].schema(), other.parentJobId);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.subJobId)) {
        this.subJobId = data().deepCopy(fields()[4].schema(), other.subJobId);
        fieldSetFlags()[4] = true;
      }
      if (isValidValue(fields()[5], other.subJobType)) {
        this.subJobType = data().deepCopy(fields()[5].schema(), other.subJobType);
        fieldSetFlags()[5] = true;
      }
      if (isValidValue(fields()[6], other.userRoles)) {
        this.userRoles = data().deepCopy(fields()[6].schema(), other.userRoles);
        fieldSetFlags()[6] = true;
      }
    }

    /**
      * Gets the value of the 'filePath' field.
      * @return The value.
      */
    public java.util.List<java.lang.String> getFilePath() {
      return filePath;
    }


    /**
      * Sets the value of the 'filePath' field.
      * @param value The value of 'filePath'.
      * @return This builder.
      */
    public com.xperi.schema.subjobevent.AssetSubJobEvent.Builder setFilePath(java.util.List<java.lang.String> value) {
      validate(fields()[0], value);
      this.filePath = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'filePath' field has been set.
      * @return True if the 'filePath' field has been set, false otherwise.
      */
    public boolean hasFilePath() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'filePath' field.
      * @return This builder.
      */
    public com.xperi.schema.subjobevent.AssetSubJobEvent.Builder clearFilePath() {
      filePath = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'jobName' field.
      * @return The value.
      */
    public java.lang.String getJobName() {
      return jobName;
    }


    /**
      * Sets the value of the 'jobName' field.
      * @param value The value of 'jobName'.
      * @return This builder.
      */
    public com.xperi.schema.subjobevent.AssetSubJobEvent.Builder setJobName(java.lang.String value) {
      validate(fields()[1], value);
      this.jobName = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'jobName' field has been set.
      * @return True if the 'jobName' field has been set, false otherwise.
      */
    public boolean hasJobName() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'jobName' field.
      * @return This builder.
      */
    public com.xperi.schema.subjobevent.AssetSubJobEvent.Builder clearJobName() {
      jobName = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'metaDataFileUrl' field.
      * @return The value.
      */
    public java.lang.String getMetaDataFileUrl() {
      return metaDataFileUrl;
    }


    /**
      * Sets the value of the 'metaDataFileUrl' field.
      * @param value The value of 'metaDataFileUrl'.
      * @return This builder.
      */
    public com.xperi.schema.subjobevent.AssetSubJobEvent.Builder setMetaDataFileUrl(java.lang.String value) {
      validate(fields()[2], value);
      this.metaDataFileUrl = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'metaDataFileUrl' field has been set.
      * @return True if the 'metaDataFileUrl' field has been set, false otherwise.
      */
    public boolean hasMetaDataFileUrl() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'metaDataFileUrl' field.
      * @return This builder.
      */
    public com.xperi.schema.subjobevent.AssetSubJobEvent.Builder clearMetaDataFileUrl() {
      metaDataFileUrl = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /**
      * Gets the value of the 'parentJobId' field.
      * @return The value.
      */
    public java.lang.String getParentJobId() {
      return parentJobId;
    }


    /**
      * Sets the value of the 'parentJobId' field.
      * @param value The value of 'parentJobId'.
      * @return This builder.
      */
    public com.xperi.schema.subjobevent.AssetSubJobEvent.Builder setParentJobId(java.lang.String value) {
      validate(fields()[3], value);
      this.parentJobId = value;
      fieldSetFlags()[3] = true;
      return this;
    }

    /**
      * Checks whether the 'parentJobId' field has been set.
      * @return True if the 'parentJobId' field has been set, false otherwise.
      */
    public boolean hasParentJobId() {
      return fieldSetFlags()[3];
    }


    /**
      * Clears the value of the 'parentJobId' field.
      * @return This builder.
      */
    public com.xperi.schema.subjobevent.AssetSubJobEvent.Builder clearParentJobId() {
      parentJobId = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    /**
      * Gets the value of the 'subJobId' field.
      * @return The value.
      */
    public java.lang.String getSubJobId() {
      return subJobId;
    }


    /**
      * Sets the value of the 'subJobId' field.
      * @param value The value of 'subJobId'.
      * @return This builder.
      */
    public com.xperi.schema.subjobevent.AssetSubJobEvent.Builder setSubJobId(java.lang.String value) {
      validate(fields()[4], value);
      this.subJobId = value;
      fieldSetFlags()[4] = true;
      return this;
    }

    /**
      * Checks whether the 'subJobId' field has been set.
      * @return True if the 'subJobId' field has been set, false otherwise.
      */
    public boolean hasSubJobId() {
      return fieldSetFlags()[4];
    }


    /**
      * Clears the value of the 'subJobId' field.
      * @return This builder.
      */
    public com.xperi.schema.subjobevent.AssetSubJobEvent.Builder clearSubJobId() {
      subJobId = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    /**
      * Gets the value of the 'subJobType' field.
      * @return The value.
      */
    public com.xperi.schema.subjobevent.AssetCategory getSubJobType() {
      return subJobType;
    }


    /**
      * Sets the value of the 'subJobType' field.
      * @param value The value of 'subJobType'.
      * @return This builder.
      */
    public com.xperi.schema.subjobevent.AssetSubJobEvent.Builder setSubJobType(com.xperi.schema.subjobevent.AssetCategory value) {
      validate(fields()[5], value);
      this.subJobType = value;
      fieldSetFlags()[5] = true;
      return this;
    }

    /**
      * Checks whether the 'subJobType' field has been set.
      * @return True if the 'subJobType' field has been set, false otherwise.
      */
    public boolean hasSubJobType() {
      return fieldSetFlags()[5];
    }


    /**
      * Clears the value of the 'subJobType' field.
      * @return This builder.
      */
    public com.xperi.schema.subjobevent.AssetSubJobEvent.Builder clearSubJobType() {
      subJobType = null;
      fieldSetFlags()[5] = false;
      return this;
    }

    /**
      * Gets the value of the 'userRoles' field.
      * @return The value.
      */
    public java.util.List<java.lang.String> getUserRoles() {
      return userRoles;
    }


    /**
      * Sets the value of the 'userRoles' field.
      * @param value The value of 'userRoles'.
      * @return This builder.
      */
    public com.xperi.schema.subjobevent.AssetSubJobEvent.Builder setUserRoles(java.util.List<java.lang.String> value) {
      validate(fields()[6], value);
      this.userRoles = value;
      fieldSetFlags()[6] = true;
      return this;
    }

    /**
      * Checks whether the 'userRoles' field has been set.
      * @return True if the 'userRoles' field has been set, false otherwise.
      */
    public boolean hasUserRoles() {
      return fieldSetFlags()[6];
    }


    /**
      * Clears the value of the 'userRoles' field.
      * @return This builder.
      */
    public com.xperi.schema.subjobevent.AssetSubJobEvent.Builder clearUserRoles() {
      userRoles = null;
      fieldSetFlags()[6] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AssetSubJobEvent build() {
      try {
        AssetSubJobEvent record = new AssetSubJobEvent();
        record.filePath = fieldSetFlags()[0] ? this.filePath : (java.util.List<java.lang.String>) defaultValue(fields()[0]);
        record.jobName = fieldSetFlags()[1] ? this.jobName : (java.lang.String) defaultValue(fields()[1]);
        record.metaDataFileUrl = fieldSetFlags()[2] ? this.metaDataFileUrl : (java.lang.String) defaultValue(fields()[2]);
        record.parentJobId = fieldSetFlags()[3] ? this.parentJobId : (java.lang.String) defaultValue(fields()[3]);
        record.subJobId = fieldSetFlags()[4] ? this.subJobId : (java.lang.String) defaultValue(fields()[4]);
        record.subJobType = fieldSetFlags()[5] ? this.subJobType : (com.xperi.schema.subjobevent.AssetCategory) defaultValue(fields()[5]);
        record.userRoles = fieldSetFlags()[6] ? this.userRoles : (java.util.List<java.lang.String>) defaultValue(fields()[6]);
        return record;
      } catch (org.apache.avro.AvroMissingFieldException e) {
        throw e;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<AssetSubJobEvent>
    WRITER$ = (org.apache.avro.io.DatumWriter<AssetSubJobEvent>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<AssetSubJobEvent>
    READER$ = (org.apache.avro.io.DatumReader<AssetSubJobEvent>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

  @Override protected boolean hasCustomCoders() { return true; }

  @Override public void customEncode(org.apache.avro.io.Encoder out)
    throws java.io.IOException
  {
    long size0 = this.filePath.size();
    out.writeArrayStart();
    out.setItemCount(size0);
    long actualSize0 = 0;
    for (java.lang.String e0: this.filePath) {
      actualSize0++;
      out.startItem();
      out.writeString(e0);
    }
    out.writeArrayEnd();
    if (actualSize0 != size0)
      throw new java.util.ConcurrentModificationException("Array-size written was " + size0 + ", but element count was " + actualSize0 + ".");

    out.writeString(this.jobName);

    out.writeString(this.metaDataFileUrl);

    out.writeString(this.parentJobId);

    out.writeString(this.subJobId);

    out.writeEnum(this.subJobType.ordinal());

    long size1 = this.userRoles.size();
    out.writeArrayStart();
    out.setItemCount(size1);
    long actualSize1 = 0;
    for (java.lang.String e1: this.userRoles) {
      actualSize1++;
      out.startItem();
      out.writeString(e1);
    }
    out.writeArrayEnd();
    if (actualSize1 != size1)
      throw new java.util.ConcurrentModificationException("Array-size written was " + size1 + ", but element count was " + actualSize1 + ".");

  }

  @Override public void customDecode(org.apache.avro.io.ResolvingDecoder in)
    throws java.io.IOException
  {
    org.apache.avro.Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
    if (fieldOrder == null) {
      long size0 = in.readArrayStart();
      java.util.List<java.lang.String> a0 = this.filePath;
      if (a0 == null) {
        a0 = new SpecificData.Array<java.lang.String>((int)size0, SCHEMA$.getField("filePath").schema());
        this.filePath = a0;
      } else a0.clear();
      SpecificData.Array<java.lang.String> ga0 = (a0 instanceof SpecificData.Array ? (SpecificData.Array<java.lang.String>)a0 : null);
      for ( ; 0 < size0; size0 = in.arrayNext()) {
        for ( ; size0 != 0; size0--) {
          java.lang.String e0 = (ga0 != null ? ga0.peek() : null);
          e0 = in.readString();
          a0.add(e0);
        }
      }

      this.jobName = in.readString();

      this.metaDataFileUrl = in.readString();

      this.parentJobId = in.readString();

      this.subJobId = in.readString();

      this.subJobType = com.xperi.schema.subjobevent.AssetCategory.values()[in.readEnum()];

      long size1 = in.readArrayStart();
      java.util.List<java.lang.String> a1 = this.userRoles;
      if (a1 == null) {
        a1 = new SpecificData.Array<java.lang.String>((int)size1, SCHEMA$.getField("userRoles").schema());
        this.userRoles = a1;
      } else a1.clear();
      SpecificData.Array<java.lang.String> ga1 = (a1 instanceof SpecificData.Array ? (SpecificData.Array<java.lang.String>)a1 : null);
      for ( ; 0 < size1; size1 = in.arrayNext()) {
        for ( ; size1 != 0; size1--) {
          java.lang.String e1 = (ga1 != null ? ga1.peek() : null);
          e1 = in.readString();
          a1.add(e1);
        }
      }

    } else {
      for (int i = 0; i < 7; i++) {
        switch (fieldOrder[i].pos()) {
        case 0:
          long size0 = in.readArrayStart();
          java.util.List<java.lang.String> a0 = this.filePath;
          if (a0 == null) {
            a0 = new SpecificData.Array<java.lang.String>((int)size0, SCHEMA$.getField("filePath").schema());
            this.filePath = a0;
          } else a0.clear();
          SpecificData.Array<java.lang.String> ga0 = (a0 instanceof SpecificData.Array ? (SpecificData.Array<java.lang.String>)a0 : null);
          for ( ; 0 < size0; size0 = in.arrayNext()) {
            for ( ; size0 != 0; size0--) {
              java.lang.String e0 = (ga0 != null ? ga0.peek() : null);
              e0 = in.readString();
              a0.add(e0);
            }
          }
          break;

        case 1:
          this.jobName = in.readString();
          break;

        case 2:
          this.metaDataFileUrl = in.readString();
          break;

        case 3:
          this.parentJobId = in.readString();
          break;

        case 4:
          this.subJobId = in.readString();
          break;

        case 5:
          this.subJobType = com.xperi.schema.subjobevent.AssetCategory.values()[in.readEnum()];
          break;

        case 6:
          long size1 = in.readArrayStart();
          java.util.List<java.lang.String> a1 = this.userRoles;
          if (a1 == null) {
            a1 = new SpecificData.Array<java.lang.String>((int)size1, SCHEMA$.getField("userRoles").schema());
            this.userRoles = a1;
          } else a1.clear();
          SpecificData.Array<java.lang.String> ga1 = (a1 instanceof SpecificData.Array ? (SpecificData.Array<java.lang.String>)a1 : null);
          for ( ; 0 < size1; size1 = in.arrayNext()) {
            for ( ; size1 != 0; size1--) {
              java.lang.String e1 = (ga1 != null ? ga1.peek() : null);
              e1 = in.readString();
              a1.add(e1);
            }
          }
          break;

        default:
          throw new java.io.IOException("Corrupt ResolvingDecoder.");
        }
      }
    }
  }
}










