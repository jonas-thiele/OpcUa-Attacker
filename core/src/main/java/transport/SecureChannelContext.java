package transport;

import opcua.model.type.SecurityToken;
import opcua.security.MessageSecurityMode;
import opcua.security.SecurityPolicy;

/**
 * Represents a secure channel established with the OPC UA OpenSecureChannel service set.
 */
public class SecureChannelContext {
    private final TransportContext transportContext;
    private final long protocolVersion;
    private final MessageSecurityMode securityMode;
    private final SecurityPolicy securityPolicy;
    private final SecurityToken securityToken;
    private final byte[] clientNonce;
    private final byte[] serverNonce;

    /**
     * Constructor
     * @param transportContext Context of the transport connection
     * @param protocolVersion The protocol version
     * @param securityMode Security mode for message security
     * @param securityPolicy Security policy for message security
     * @param securityToken The security token for symmetry cryptography
     * @param clientNonce The client nonce for key derivation
     * @param serverNonce The server nonce for key derivation
     */
    public SecureChannelContext(TransportContext transportContext, long protocolVersion, MessageSecurityMode securityMode, SecurityPolicy securityPolicy, SecurityToken securityToken, byte[] clientNonce, byte[] serverNonce) {
        this.transportContext = transportContext;
        this.protocolVersion = protocolVersion;
        this.securityMode = securityMode;
        this.securityPolicy = securityPolicy;
        this.securityToken = securityToken;
        this.clientNonce = clientNonce;
        this.serverNonce = serverNonce;
    }

    public TransportContext getTransportContext() {
        return transportContext;
    }

    public long getProtocolVersion() {
        return protocolVersion;
    }

    public MessageSecurityMode getSecurityMode() {
        return securityMode;
    }

    public SecurityPolicy getSecurityPolicy() {
        return securityPolicy;
    }

    public SecurityToken getSecurityToken() {
        return securityToken;
    }

    public byte[] getClientNonce() {
        return clientNonce;
    }

    public byte[] getServerNonce() {
        return serverNonce;
    }
}
