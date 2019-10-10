package opcua.message;

import opcua.message.parts.MessageType;
import opcua.encoding.BinarySerializer;
import opcua.encoding.MessageInputStream;

import java.io.IOException;

/**
 * Acknowledge message of the OPC UA connection protocol (OPC UA Part 6, p.56)
 */
public class AcknowledgeMessage extends ConnectionProtocolMessage {
    private long protocolVersion;
    private long receiveBufferSize;
    private long sendBufferSize;
    private long maxMessageSize;
    private long maxChunkCount;

    public AcknowledgeMessage(long protocolVersion, long receiveBufferSize, long sendBufferSize, long maxMessageSize, long maxChunkCount) {
        super(MessageType.ACK);
        this.protocolVersion = protocolVersion;
        this.receiveBufferSize = receiveBufferSize;
        this.sendBufferSize = sendBufferSize;
        this.maxMessageSize = maxMessageSize;
        this.maxChunkCount = maxChunkCount;
    }

    public AcknowledgeMessage() {
        super(MessageType.ACK);
    }

    @Override
    public byte[] toBinary() {
        return new BinarySerializer()
                .putUInt32(protocolVersion)
                .putUInt32(receiveBufferSize)
                .putUInt32(sendBufferSize)
                .putUInt32(maxMessageSize)
                .putUInt32(maxChunkCount)
                .get();
    }

    public static AcknowledgeMessage constructFromBinary(MessageInputStream stream) throws IOException {
        AcknowledgeMessage msg = new AcknowledgeMessage();
        msg.setProtocolVersion(stream.readUInt32());
        msg.setReceiveBufferSize(stream.readUInt32());
        msg.setSendBufferSize(stream.readUInt32());
        msg.setMaxMessageSize(stream.readUInt32());
        msg.setMaxChunkCount(stream.readUInt32());
        return msg;
    }

    public long getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(long protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public long getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public void setReceiveBufferSize(long receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
    }

    public long getSendBufferSize() {
        return sendBufferSize;
    }

    public void setSendBufferSize(long sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
    }

    public long getMaxMessageSize() {
        return maxMessageSize;
    }

    public void setMaxMessageSize(long maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }

    public long getMaxChunkCount() {
        return maxChunkCount;
    }

    public void setMaxChunkCount(long maxChunkCount) {
        this.maxChunkCount = maxChunkCount;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.ACK;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("AcknowledgeMessage:")
                .append("\n   ProtocolVersion: ").append(protocolVersion)
                .append("\n   ReceiveBufferSize: ").append(receiveBufferSize)
                .append("\n   SendBufferSize: ").append(sendBufferSize)
                .append("\n   MaxMessageSize: ").append(maxMessageSize)
                .append("\n   MaxChunkCount: ").append(maxChunkCount)
                .toString();
    }
}
