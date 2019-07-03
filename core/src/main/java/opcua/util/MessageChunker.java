package opcua.util;

import opcua.security.MessageSecurityMode;

public class MessageChunker {

    private static final int MESSAGE_HEADER_SIZE = 12;
    private static final int SEQUENCE_HEADER_SIZE = 8;

    private final long maxChunkSize;
    private final long messageHeaderSize;
    private final long securityHeaderSize;
    private final long sequenceHeaderSize;
    private final long signatureSize;
    private final MessageSecurityMode messageSecurityMode;

    public MessageChunker(long maxChunkSize, long messageHeaderSize, long securityHeaderSize, long sequenceHeaderSize, long signatureSize, MessageSecurityMode messageSecurityMode) {
        this.maxChunkSize = maxChunkSize;
        this.messageHeaderSize = messageHeaderSize;
        this.securityHeaderSize = securityHeaderSize;
        this.sequenceHeaderSize = sequenceHeaderSize;
        this.signatureSize = signatureSize;
        this.messageSecurityMode = messageSecurityMode;
    }
}
