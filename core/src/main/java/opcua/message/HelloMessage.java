package opcua.message;

import opcua.context.StaticConfig;
import opcua.encoding.EncodingException;
import opcua.message.parts.MessageType;
import opcua.encoding.BinarySerializer;
import opcua.encoding.MessageInputStream;

import java.io.IOException;

/**
 * Hello message of the OPC UA connection protocol (OPC UA Part 6, p.55)
 */

public class HelloMessage extends ConnectionProtocolMessage {
    private long protocolVersion;
    private long receiveBufferSize;
    private long sendBufferSize;
    private long maxMessageSize;
    private long maxChunkCount;
    private String endpointUrl;

    public HelloMessage(long protocolVersion, long receiveBufferSize, long sendBufferSize, long maxMessageSize, long maxChunkCount, String endpointUrl) {
        super(MessageType.HEL);
        this.protocolVersion = protocolVersion;
        this.receiveBufferSize = receiveBufferSize;
        this.sendBufferSize = sendBufferSize;
        this.maxMessageSize = maxMessageSize;
        this.maxChunkCount = maxChunkCount;
        this.endpointUrl = endpointUrl;
    }

    public HelloMessage() {
        super(MessageType.HEL);
    }

    public static HelloMessage fromConfig(String endpointUrl) {
        return new HelloMessage(
                StaticConfig.PROTOCOL_VERSION,
                StaticConfig.RECEIVE_BUFFER_SIZE,
                StaticConfig.SEND_BUFFER_SIZE,
                StaticConfig.MAX_MESSAGE_SIZE,
                StaticConfig.MAX_CHUNK_COUNT,
                endpointUrl
        );
    }

    @Override
    public byte[] toBinary() {
        return new BinarySerializer()
                .putUInt32(protocolVersion)
                .putUInt32(receiveBufferSize)
                .putUInt32(sendBufferSize)
                .putUInt32(maxMessageSize)
                .putUInt32(maxChunkCount)
                .putString(endpointUrl)
                .get();
    }

    public static HelloMessage constructFromBinary(MessageInputStream stream) throws EncodingException {
        try {
            HelloMessage msg = new HelloMessage();
            msg.setProtocolVersion(stream.readUInt32());
            msg.setReceiveBufferSize(stream.readUInt32());
            msg.setSendBufferSize(stream.readUInt32());
            msg.setMaxMessageSize(stream.readUInt32());
            msg.setMaxChunkCount(stream.readUInt32());
            msg.setEndpointUrl(stream.readString());
            return msg;
        } catch (IOException e) {
            throw new EncodingException(e);
        }
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

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.HEL;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("HelloMessage:")
                .append("\n   ProtocolVersion: ").append(protocolVersion)
                .append("\n   ReceiveBufferSize: ").append(receiveBufferSize)
                .append("\n   SendBufferSize: ").append(sendBufferSize)
                .append("\n   MaxMessageSize: ").append(maxMessageSize)
                .append("\n   MaxChunkCount: ").append(maxChunkCount)
                .append("\n   EndpointUrl: ").append(endpointUrl)
                .toString();
    }
}
