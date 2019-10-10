package opcua.message.parts;

import opcua.encoding.BinarySerializer;

/**
 * Message Header for the secure conversation protocol (OPC UA Part 6, p.47)
 */
public class SecureConversationMessageHeader extends MessageHeader {
    private long secureChannelId;

    public SecureConversationMessageHeader(MessageType messageType, IsFinal isFinal, long messageSize, long secureChannelId) {
        super(messageType, isFinal, messageSize);
        this.secureChannelId = secureChannelId;
    }

    @Override
    public byte[] toBinary() {
        return new BinarySerializer()
                .putBytes(super.toBinary())
                .putUInt32(secureChannelId)
                .get();
    }

    public long getSecureChannelId() {
        return secureChannelId;
    }

    public void setSecureChannelId(long secureChannelId) {
        this.secureChannelId = secureChannelId;
    }
}
