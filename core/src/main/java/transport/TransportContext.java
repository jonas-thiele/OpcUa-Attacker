package transport;

/**
 * Represents a transport connection
 */
public class TransportContext {
    private final Connection connection;
    private final long protocolVersion;
    private final long receiveBufferSize;
    private final long sendBufferSize;
    private final long maxMessageSize;
    private final long maxChunkCount;
    private long sequenceNumber;
    private long requestId;

    public TransportContext(Connection connection, long protocolVersion, long receiveBufferSize, long sendBufferSize, long maxMessageSize, long maxChunkCount) {
        this.connection = connection;
        this.protocolVersion = protocolVersion;
        this.receiveBufferSize = receiveBufferSize;
        this.sendBufferSize = sendBufferSize;
        this.maxMessageSize = maxMessageSize;
        this.maxChunkCount = maxChunkCount;
        this.sequenceNumber = 1;
        this.requestId = 1;
    }

    public long getAndIncrementSequenceNumber() {
        sequenceNumber++;
        return sequenceNumber-1;
    }
    public long getAndIncrementRequestId() {
        requestId++;
        return requestId-1;
    }

    public Connection getConnection() {
        return connection;
    }

    public long getProtocolVersion() {
        return protocolVersion;
    }

    public long getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public long getSendBufferSize() {
        return sendBufferSize;
    }

    public long getMaxMessageSize() {
        return maxMessageSize;
    }

    public long getMaxChunkCount() {
        return maxChunkCount;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public long getRequestId() {
        return requestId;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }
}
