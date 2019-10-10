package opcua.message;

import opcua.context.StaticConfig;
import opcua.message.parts.MessageType;
import opcua.message.parts.RequestHeader;
import opcua.model.type.SecurityTokenRequestType;
import opcua.model.type.ObjectIds;
import opcua.security.MessageSecurityMode;
import opcua.encoding.BinarySerializer;

/**
 * OpenSecureChannel Request (OPC UA Part 4, p.21)
 */
public class OpenSecureChannelRequest extends SecureConversationMessage {
    private static RequestHeader DEFAULT_REQUEST_HEADER = new RequestHeader(ObjectIds.OPEN_SECURE_CHANNEL_REQUEST, null, 0, 0, "", StaticConfig.TIMEOUT);

    private RequestHeader requestHeader;
    private long clientProtocolVersion;
    private SecurityTokenRequestType requestType;
    private MessageSecurityMode securityMode;
    private byte[] clientNonce;     //Needs to be parsed as ByteString
    private long requestedLifetime;

    public OpenSecureChannelRequest(RequestHeader requestHeader, long clientProtocolVersion, SecurityTokenRequestType requestType,
                                    MessageSecurityMode securityMode, byte[] clientNonce, long requestedLifetime) {
        this.requestHeader = requestHeader;
        this.clientProtocolVersion = clientProtocolVersion;
        this.requestType = requestType;
        this.securityMode = securityMode;
        this.clientNonce = clientNonce;
        this.requestedLifetime = requestedLifetime;
    }

    public OpenSecureChannelRequest(long clientProtocolVersion, SecurityTokenRequestType requestType,
                                    MessageSecurityMode securityMode, byte[] clientNonce, long requestedLifetime) {
        this.requestHeader = DEFAULT_REQUEST_HEADER;
        this.clientProtocolVersion = clientProtocolVersion;
        this.requestType = requestType;
        this.securityMode = securityMode;
        this.clientNonce = clientNonce;
        this.requestedLifetime = requestedLifetime;
    }

    public OpenSecureChannelRequest() { }

    @Override
    public byte[] toBinary() {
        BinarySerializer serializer = new BinarySerializer();
        if(requestHeader != null) {
            serializer.putBytes(requestHeader.toBinary());
        }
        return serializer.putUInt32(clientProtocolVersion)
                .putEnumeration(requestType)
                .putEnumeration(securityMode)
                .putByteArray(clientNonce)
                .putUInt32(requestedLifetime)
                .get();
    }


    @Override
    public MessageType getMessageType() {
        return MessageType.OPN;
    }


    public RequestHeader getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(RequestHeader requestHeader) {
        this.requestHeader = requestHeader;
    }

    public long getClientProtocolVersion() {
        return clientProtocolVersion;
    }

    public void setClientProtocolVersion(long clientProtocolVersion) {
        this.clientProtocolVersion = clientProtocolVersion;
    }

    public SecurityTokenRequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(SecurityTokenRequestType requestType) {
        this.requestType = requestType;
    }

    public MessageSecurityMode getSecurityMode() {
        return securityMode;
    }

    public void setSecurityMode(MessageSecurityMode securityMode) {
        this.securityMode = securityMode;
    }

    public byte[] getClientNonce() {
        return clientNonce;
    }

    public void setClientNonce(byte[] clientNonce) {
        this.clientNonce = clientNonce;
    }

    public long getRequestedLifetime() {
        return requestedLifetime;
    }

    public void setRequestedLifetime(long requestedLifetime) {
        this.requestedLifetime = requestedLifetime;
    }
}
