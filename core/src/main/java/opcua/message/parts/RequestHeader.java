package opcua.message.parts;

import opcua.model.type.NodeId;
import opcua.encoding.BinarySerializer;

import java.time.Instant;

/**
 * Request Header (OPC UA Part 4, p. 146)
 */
public class RequestHeader {

    private NodeId authenticationToken;
    private Instant timestamp;
    private long requestHandle;
    private long returnDiagnostics;
    private String auditEntryId;
    private long timeoutHint;
    /* ExtensionObject not supported at this point */


    public RequestHeader(NodeId authenticationToken, Instant timestamp, long requestHandle, long returnDiagnostics, String auditEntryId, long timeoutHint) {
        this.authenticationToken = authenticationToken;
        this.timestamp = timestamp;
        this.requestHandle = requestHandle;
        this.returnDiagnostics = returnDiagnostics;
        this.auditEntryId = auditEntryId;
        this.timeoutHint = timeoutHint;
    }

    public RequestHeader() { }


    public byte[] toBinary() {
        return new BinarySerializer()
                .putNodeId(authenticationToken)
                .putDateTime(timestamp)
                .putUInt32(requestHandle)
                .putUInt32(returnDiagnostics)
                .putString(auditEntryId)
                .putUInt32(timeoutHint)
                //"Null-ExtensionObject"
                .putNodeId(NodeId.NUMERIC_NULL)
                .putByte((byte)0x0)
                .get();
    }


    public NodeId getAuthenticationToken() {
        return authenticationToken;
    }

    public void setAuthenticationToken(NodeId authenticationToken) {
        this.authenticationToken = authenticationToken;
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

    public long getReturnDiagnostics() {
        return returnDiagnostics;
    }

    public void setReturnDiagnostics(long returnDiagnostics) {
        this.returnDiagnostics = returnDiagnostics;
    }

    public String getAuditEntryId() {
        return auditEntryId;
    }

    public void setAuditEntryId(String auditEntryId) {
        this.auditEntryId = auditEntryId;
    }

    public long getTimeoutHint() {
        return timeoutHint;
    }

    public void setTimeoutHint(long timeoutHint) {
        this.timeoutHint = timeoutHint;
    }
}
