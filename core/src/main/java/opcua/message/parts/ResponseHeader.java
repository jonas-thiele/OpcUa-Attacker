package opcua.message.parts;

import opcua.encoding.BinarySerializer;
import opcua.encoding.MessageInputStream;

import java.io.IOException;
import java.time.Instant;

/**
 * Response Header (OPC UA Part 4, p. 147)
 */
public class ResponseHeader {
    private Instant timestamp;
    private long requestHandle;
    private long serviceResult;

    public ResponseHeader(Instant timestamp, long requestHandle, long serviceResult) {
        this.timestamp = timestamp;
        this.requestHandle = requestHandle;
        this.serviceResult = serviceResult;
    }

    public ResponseHeader() {}

    public byte[] toBinary() {
        return new BinarySerializer()
                .putDateTime(timestamp)
                .putUInt32(requestHandle)
                .putUInt32(serviceResult)
                .get();
    }

    public static ResponseHeader constructFromBinary(MessageInputStream stream) throws IOException {
        ResponseHeader header = new ResponseHeader();
        header.setTimestamp(stream.readDateTime());
        header.setRequestHandle(stream.readUInt32());
        header.setServiceResult(stream.readUInt32());

        // Skip ServiceDiagnostics, StringTable and AdditionalHeader
        stream.skipBytes(8);
        return header;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public long getRequestHandle() {
        return requestHandle;
    }

    public void setRequestHandle(long requestHandle) {
        this.requestHandle = requestHandle;
    }

    public long getServiceResult() {
        return serviceResult;
    }

    public void setServiceResult(long serviceResult) {
        this.serviceResult = serviceResult;
    }
}
