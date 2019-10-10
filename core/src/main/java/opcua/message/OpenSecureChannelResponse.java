package opcua.message;

import opcua.message.parts.MessageType;
import opcua.message.parts.ResponseHeader;
import opcua.model.type.SecurityToken;
import opcua.encoding.BinarySerializer;
import opcua.encoding.MessageInputStream;

import java.io.IOException;

/**
 * OpenSecureChannel Response (OPC UA Part 4, p.22)
 */
public class OpenSecureChannelResponse extends SecureConversationMessage {
    private ResponseHeader responseHeader;
    private long serverProtocolVersion;
    private SecurityToken securityToken;
    private byte[] serverNonce;

    public OpenSecureChannelResponse() { }

    public OpenSecureChannelResponse(ResponseHeader responseHeader, long serverProtocolVersion, SecurityToken securityToken, byte[] serverNonce) {
        this.responseHeader = responseHeader;
        this.securityToken = securityToken;
        this.serverNonce = serverNonce;
        this.serverProtocolVersion = serverProtocolVersion;
    }

    @Override
    public byte[] toBinary() {
        return new BinarySerializer()
                .putBytes(responseHeader.toBinary())
                .putBytes(securityToken.toBinary())
                .putByteArray(serverNonce)
                .get();
    }

    public static OpenSecureChannelResponse constructFromBinary(MessageInputStream stream) throws IOException {
        stream.readNodeId(); //Skip
        ResponseHeader responseHeader = ResponseHeader.constructFromBinary(stream);
        long serverProtocolVersion = stream.readUInt32();
        SecurityToken securityToken = SecurityToken.constructFromBinary(stream);
        byte[] serverNonce = stream.readByteArray();
        return new OpenSecureChannelResponse(responseHeader, serverProtocolVersion, securityToken, serverNonce);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.OPN;
    }

    public ResponseHeader getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(ResponseHeader responseHeader) {
        this.responseHeader = responseHeader;
    }

    public long getServerProtocolVersion() {
        return serverProtocolVersion;
    }

    public void setServerProtocolVersion(long serverProtocolVersion) {
        this.serverProtocolVersion = serverProtocolVersion;
    }

    public SecurityToken getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(SecurityToken securityToken) {
        this.securityToken = securityToken;
    }

    public byte[] getServerNonce() {
        return serverNonce;
    }

    public void setServerNonce(byte[] serverNonce) {
        this.serverNonce = serverNonce;
    }
}
