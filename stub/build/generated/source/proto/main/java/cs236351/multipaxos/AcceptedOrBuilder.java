// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: multipaxos/paxos_messages.proto

package cs236351.multipaxos;

public interface AcceptedOrBuilder extends
    // @@protoc_insertion_point(interface_extends:cs236351.multipaxos.Accepted)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.cs236351.multipaxos.RoundNo round_no = 1;</code>
   * @return Whether the roundNo field is set.
   */
  boolean hasRoundNo();
  /**
   * <code>.cs236351.multipaxos.RoundNo round_no = 1;</code>
   * @return The roundNo.
   */
  cs236351.multipaxos.RoundNo getRoundNo();
  /**
   * <code>.cs236351.multipaxos.RoundNo round_no = 1;</code>
   */
  cs236351.multipaxos.RoundNoOrBuilder getRoundNoOrBuilder();

  /**
   * <code>.cs236351.multipaxos.Ack ack = 2;</code>
   * @return The enum numeric value on the wire for ack.
   */
  int getAckValue();
  /**
   * <code>.cs236351.multipaxos.Ack ack = 2;</code>
   * @return The ack.
   */
  cs236351.multipaxos.Ack getAck();

  /**
   * <code>int32 instance_no = 3;</code>
   * @return The instanceNo.
   */
  int getInstanceNo();
}