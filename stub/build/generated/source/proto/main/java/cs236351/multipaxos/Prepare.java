// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: multipaxos/paxos_messages.proto

package cs236351.multipaxos;

/**
 * Protobuf type {@code cs236351.multipaxos.Prepare}
 */
public final class Prepare extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:cs236351.multipaxos.Prepare)
    PrepareOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Prepare.newBuilder() to construct.
  private Prepare(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Prepare() {
    value_ = com.google.protobuf.ByteString.EMPTY;
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Prepare();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private Prepare(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          case 10: {
            cs236351.multipaxos.RoundNo.Builder subBuilder = null;
            if (roundNo_ != null) {
              subBuilder = roundNo_.toBuilder();
            }
            roundNo_ = input.readMessage(cs236351.multipaxos.RoundNo.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(roundNo_);
              roundNo_ = subBuilder.buildPartial();
            }

            break;
          }
          case 16: {

            instanceNo_ = input.readInt32();
            break;
          }
          case 26: {

            value_ = input.readBytes();
            break;
          }
          default: {
            if (!parseUnknownField(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return cs236351.multipaxos.PaxosMessages.internal_static_cs236351_multipaxos_Prepare_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return cs236351.multipaxos.PaxosMessages.internal_static_cs236351_multipaxos_Prepare_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            cs236351.multipaxos.Prepare.class, cs236351.multipaxos.Prepare.Builder.class);
  }

  public static final int ROUND_NO_FIELD_NUMBER = 1;
  private cs236351.multipaxos.RoundNo roundNo_;
  /**
   * <code>.cs236351.multipaxos.RoundNo round_no = 1;</code>
   * @return Whether the roundNo field is set.
   */
  @java.lang.Override
  public boolean hasRoundNo() {
    return roundNo_ != null;
  }
  /**
   * <code>.cs236351.multipaxos.RoundNo round_no = 1;</code>
   * @return The roundNo.
   */
  @java.lang.Override
  public cs236351.multipaxos.RoundNo getRoundNo() {
    return roundNo_ == null ? cs236351.multipaxos.RoundNo.getDefaultInstance() : roundNo_;
  }
  /**
   * <code>.cs236351.multipaxos.RoundNo round_no = 1;</code>
   */
  @java.lang.Override
  public cs236351.multipaxos.RoundNoOrBuilder getRoundNoOrBuilder() {
    return getRoundNo();
  }

  public static final int INSTANCE_NO_FIELD_NUMBER = 2;
  private int instanceNo_;
  /**
   * <code>int32 instance_no = 2;</code>
   * @return The instanceNo.
   */
  @java.lang.Override
  public int getInstanceNo() {
    return instanceNo_;
  }

  public static final int VALUE_FIELD_NUMBER = 3;
  private com.google.protobuf.ByteString value_;
  /**
   * <code>bytes value = 3;</code>
   * @return The value.
   */
  @java.lang.Override
  public com.google.protobuf.ByteString getValue() {
    return value_;
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (roundNo_ != null) {
      output.writeMessage(1, getRoundNo());
    }
    if (instanceNo_ != 0) {
      output.writeInt32(2, instanceNo_);
    }
    if (!value_.isEmpty()) {
      output.writeBytes(3, value_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (roundNo_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getRoundNo());
    }
    if (instanceNo_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(2, instanceNo_);
    }
    if (!value_.isEmpty()) {
      size += com.google.protobuf.CodedOutputStream
        .computeBytesSize(3, value_);
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof cs236351.multipaxos.Prepare)) {
      return super.equals(obj);
    }
    cs236351.multipaxos.Prepare other = (cs236351.multipaxos.Prepare) obj;

    if (hasRoundNo() != other.hasRoundNo()) return false;
    if (hasRoundNo()) {
      if (!getRoundNo()
          .equals(other.getRoundNo())) return false;
    }
    if (getInstanceNo()
        != other.getInstanceNo()) return false;
    if (!getValue()
        .equals(other.getValue())) return false;
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    if (hasRoundNo()) {
      hash = (37 * hash) + ROUND_NO_FIELD_NUMBER;
      hash = (53 * hash) + getRoundNo().hashCode();
    }
    hash = (37 * hash) + INSTANCE_NO_FIELD_NUMBER;
    hash = (53 * hash) + getInstanceNo();
    hash = (37 * hash) + VALUE_FIELD_NUMBER;
    hash = (53 * hash) + getValue().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static cs236351.multipaxos.Prepare parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static cs236351.multipaxos.Prepare parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static cs236351.multipaxos.Prepare parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static cs236351.multipaxos.Prepare parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static cs236351.multipaxos.Prepare parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static cs236351.multipaxos.Prepare parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static cs236351.multipaxos.Prepare parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static cs236351.multipaxos.Prepare parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static cs236351.multipaxos.Prepare parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static cs236351.multipaxos.Prepare parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static cs236351.multipaxos.Prepare parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static cs236351.multipaxos.Prepare parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(cs236351.multipaxos.Prepare prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code cs236351.multipaxos.Prepare}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:cs236351.multipaxos.Prepare)
      cs236351.multipaxos.PrepareOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return cs236351.multipaxos.PaxosMessages.internal_static_cs236351_multipaxos_Prepare_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return cs236351.multipaxos.PaxosMessages.internal_static_cs236351_multipaxos_Prepare_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              cs236351.multipaxos.Prepare.class, cs236351.multipaxos.Prepare.Builder.class);
    }

    // Construct using cs236351.multipaxos.Prepare.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      if (roundNoBuilder_ == null) {
        roundNo_ = null;
      } else {
        roundNo_ = null;
        roundNoBuilder_ = null;
      }
      instanceNo_ = 0;

      value_ = com.google.protobuf.ByteString.EMPTY;

      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return cs236351.multipaxos.PaxosMessages.internal_static_cs236351_multipaxos_Prepare_descriptor;
    }

    @java.lang.Override
    public cs236351.multipaxos.Prepare getDefaultInstanceForType() {
      return cs236351.multipaxos.Prepare.getDefaultInstance();
    }

    @java.lang.Override
    public cs236351.multipaxos.Prepare build() {
      cs236351.multipaxos.Prepare result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public cs236351.multipaxos.Prepare buildPartial() {
      cs236351.multipaxos.Prepare result = new cs236351.multipaxos.Prepare(this);
      if (roundNoBuilder_ == null) {
        result.roundNo_ = roundNo_;
      } else {
        result.roundNo_ = roundNoBuilder_.build();
      }
      result.instanceNo_ = instanceNo_;
      result.value_ = value_;
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof cs236351.multipaxos.Prepare) {
        return mergeFrom((cs236351.multipaxos.Prepare)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(cs236351.multipaxos.Prepare other) {
      if (other == cs236351.multipaxos.Prepare.getDefaultInstance()) return this;
      if (other.hasRoundNo()) {
        mergeRoundNo(other.getRoundNo());
      }
      if (other.getInstanceNo() != 0) {
        setInstanceNo(other.getInstanceNo());
      }
      if (other.getValue() != com.google.protobuf.ByteString.EMPTY) {
        setValue(other.getValue());
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      cs236351.multipaxos.Prepare parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (cs236351.multipaxos.Prepare) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private cs236351.multipaxos.RoundNo roundNo_;
    private com.google.protobuf.SingleFieldBuilderV3<
        cs236351.multipaxos.RoundNo, cs236351.multipaxos.RoundNo.Builder, cs236351.multipaxos.RoundNoOrBuilder> roundNoBuilder_;
    /**
     * <code>.cs236351.multipaxos.RoundNo round_no = 1;</code>
     * @return Whether the roundNo field is set.
     */
    public boolean hasRoundNo() {
      return roundNoBuilder_ != null || roundNo_ != null;
    }
    /**
     * <code>.cs236351.multipaxos.RoundNo round_no = 1;</code>
     * @return The roundNo.
     */
    public cs236351.multipaxos.RoundNo getRoundNo() {
      if (roundNoBuilder_ == null) {
        return roundNo_ == null ? cs236351.multipaxos.RoundNo.getDefaultInstance() : roundNo_;
      } else {
        return roundNoBuilder_.getMessage();
      }
    }
    /**
     * <code>.cs236351.multipaxos.RoundNo round_no = 1;</code>
     */
    public Builder setRoundNo(cs236351.multipaxos.RoundNo value) {
      if (roundNoBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        roundNo_ = value;
        onChanged();
      } else {
        roundNoBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>.cs236351.multipaxos.RoundNo round_no = 1;</code>
     */
    public Builder setRoundNo(
        cs236351.multipaxos.RoundNo.Builder builderForValue) {
      if (roundNoBuilder_ == null) {
        roundNo_ = builderForValue.build();
        onChanged();
      } else {
        roundNoBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.cs236351.multipaxos.RoundNo round_no = 1;</code>
     */
    public Builder mergeRoundNo(cs236351.multipaxos.RoundNo value) {
      if (roundNoBuilder_ == null) {
        if (roundNo_ != null) {
          roundNo_ =
            cs236351.multipaxos.RoundNo.newBuilder(roundNo_).mergeFrom(value).buildPartial();
        } else {
          roundNo_ = value;
        }
        onChanged();
      } else {
        roundNoBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>.cs236351.multipaxos.RoundNo round_no = 1;</code>
     */
    public Builder clearRoundNo() {
      if (roundNoBuilder_ == null) {
        roundNo_ = null;
        onChanged();
      } else {
        roundNo_ = null;
        roundNoBuilder_ = null;
      }

      return this;
    }
    /**
     * <code>.cs236351.multipaxos.RoundNo round_no = 1;</code>
     */
    public cs236351.multipaxos.RoundNo.Builder getRoundNoBuilder() {
      
      onChanged();
      return getRoundNoFieldBuilder().getBuilder();
    }
    /**
     * <code>.cs236351.multipaxos.RoundNo round_no = 1;</code>
     */
    public cs236351.multipaxos.RoundNoOrBuilder getRoundNoOrBuilder() {
      if (roundNoBuilder_ != null) {
        return roundNoBuilder_.getMessageOrBuilder();
      } else {
        return roundNo_ == null ?
            cs236351.multipaxos.RoundNo.getDefaultInstance() : roundNo_;
      }
    }
    /**
     * <code>.cs236351.multipaxos.RoundNo round_no = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        cs236351.multipaxos.RoundNo, cs236351.multipaxos.RoundNo.Builder, cs236351.multipaxos.RoundNoOrBuilder> 
        getRoundNoFieldBuilder() {
      if (roundNoBuilder_ == null) {
        roundNoBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            cs236351.multipaxos.RoundNo, cs236351.multipaxos.RoundNo.Builder, cs236351.multipaxos.RoundNoOrBuilder>(
                getRoundNo(),
                getParentForChildren(),
                isClean());
        roundNo_ = null;
      }
      return roundNoBuilder_;
    }

    private int instanceNo_ ;
    /**
     * <code>int32 instance_no = 2;</code>
     * @return The instanceNo.
     */
    @java.lang.Override
    public int getInstanceNo() {
      return instanceNo_;
    }
    /**
     * <code>int32 instance_no = 2;</code>
     * @param value The instanceNo to set.
     * @return This builder for chaining.
     */
    public Builder setInstanceNo(int value) {
      
      instanceNo_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 instance_no = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearInstanceNo() {
      
      instanceNo_ = 0;
      onChanged();
      return this;
    }

    private com.google.protobuf.ByteString value_ = com.google.protobuf.ByteString.EMPTY;
    /**
     * <code>bytes value = 3;</code>
     * @return The value.
     */
    @java.lang.Override
    public com.google.protobuf.ByteString getValue() {
      return value_;
    }
    /**
     * <code>bytes value = 3;</code>
     * @param value The value to set.
     * @return This builder for chaining.
     */
    public Builder setValue(com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      value_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>bytes value = 3;</code>
     * @return This builder for chaining.
     */
    public Builder clearValue() {
      
      value_ = getDefaultInstance().getValue();
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:cs236351.multipaxos.Prepare)
  }

  // @@protoc_insertion_point(class_scope:cs236351.multipaxos.Prepare)
  private static final cs236351.multipaxos.Prepare DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new cs236351.multipaxos.Prepare();
  }

  public static cs236351.multipaxos.Prepare getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Prepare>
      PARSER = new com.google.protobuf.AbstractParser<Prepare>() {
    @java.lang.Override
    public Prepare parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new Prepare(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<Prepare> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Prepare> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public cs236351.multipaxos.Prepare getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
