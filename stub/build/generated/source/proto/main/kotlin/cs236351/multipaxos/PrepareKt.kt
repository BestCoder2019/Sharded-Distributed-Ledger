//Generated by the protocol buffer compiler. DO NOT EDIT!
// source: multipaxos/paxos_messages.proto

package cs236351.multipaxos;

@kotlin.jvm.JvmSynthetic
inline fun prepare(block: cs236351.multipaxos.PrepareKt.Dsl.() -> kotlin.Unit): cs236351.multipaxos.Prepare =
  cs236351.multipaxos.PrepareKt.Dsl._create(cs236351.multipaxos.Prepare.newBuilder()).apply { block() }._build()
object PrepareKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  class Dsl private constructor(
    private val _builder: cs236351.multipaxos.Prepare.Builder
  ) {
    companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: cs236351.multipaxos.Prepare.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): cs236351.multipaxos.Prepare = _builder.build()

    /**
     * <code>.cs236351.multipaxos.RoundNo round_no = 1;</code>
     */
    var roundNo: cs236351.multipaxos.RoundNo
      @JvmName("getRoundNo")
      get() = _builder.getRoundNo()
      @JvmName("setRoundNo")
      set(value) {
        _builder.setRoundNo(value)
      }
    /**
     * <code>.cs236351.multipaxos.RoundNo round_no = 1;</code>
     */
    fun clearRoundNo() {
      _builder.clearRoundNo()
    }
    /**
     * <code>.cs236351.multipaxos.RoundNo round_no = 1;</code>
     * @return Whether the roundNo field is set.
     */
    fun hasRoundNo(): kotlin.Boolean {
      return _builder.hasRoundNo()
    }

    /**
     * <code>int32 instance_no = 2;</code>
     */
    var instanceNo: kotlin.Int
      @JvmName("getInstanceNo")
      get() = _builder.getInstanceNo()
      @JvmName("setInstanceNo")
      set(value) {
        _builder.setInstanceNo(value)
      }
    /**
     * <code>int32 instance_no = 2;</code>
     */
    fun clearInstanceNo() {
      _builder.clearInstanceNo()
    }

    /**
     * <code>bytes value = 3;</code>
     */
    var value: com.google.protobuf.ByteString
      @JvmName("getValue")
      get() = _builder.getValue()
      @JvmName("setValue")
      set(value) {
        _builder.setValue(value)
      }
    /**
     * <code>bytes value = 3;</code>
     */
    fun clearValue() {
      _builder.clearValue()
    }
  }
}
@kotlin.jvm.JvmSynthetic
inline fun cs236351.multipaxos.Prepare.copy(block: cs236351.multipaxos.PrepareKt.Dsl.() -> kotlin.Unit): cs236351.multipaxos.Prepare =
  cs236351.multipaxos.PrepareKt.Dsl._create(this.toBuilder()).apply { block() }._build()
