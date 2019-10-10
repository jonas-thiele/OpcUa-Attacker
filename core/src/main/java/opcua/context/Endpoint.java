package opcua.context;

import opcua.security.MessageSecurityMode;
import opcua.security.SecurityPolicy;

import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

/**
 * Represents a remote OPC UA endpoint
 */
public class Endpoint {
    private final String hostName;
    private final int port;
    private final String endpointUrl;
    private X509Certificate certificate;
    private SecurityPolicy securityPolicy;
    private MessageSecurityMode messageSecurityMode;

    /**
     * Constructor
     * @param hostName The hostname of server
     * @param port Port of the server
     * @param endpointUrl Url of the endpoint
     * @param certificate Certificate of the endpoint
     * @param securityPolicy SecurityPolicy used for communication with endpoint
     * @param messageSecurityMode Security mode used for communication with endpoint
     */
    public Endpoint(String hostName, int port, String endpointUrl, X509Certificate certificate, SecurityPolicy securityPolicy, MessageSecurityMode messageSecurityMode) {
        this.hostName = hostName;
        this.port = port;
        this.endpointUrl = endpointUrl;
        this.certificate = certificate;
        this.securityPolicy = securityPolicy;
        this.messageSecurityMode = messageSecurityMode;
    }

    /**
     * Constructs an Endpoint without security configuration
     * @param hostName The hostname of server
     * @param port Port of the server
     * @param endpointUrl Url of the endpoint
     */
    public Endpoint(String hostName, int port, String endpointUrl) {
        this.hostName = hostName;
        this.port = port;
        this.endpointUrl = endpointUrl;
        this.securityPolicy = SecurityPolicy.NONE;
        this.messageSecurityMode = MessageSecurityMode.NONE;
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public X509Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public SecurityPolicy getSecurityPolicy() {
        return securityPolicy;
    }

    public void setSecurityPolicy(SecurityPolicy securityPolicy) {
        this.securityPolicy = securityPolicy;
    }

    public MessageSecurityMode getMessageSecurityMode() {
        return messageSecurityMode;
    }

    public void setMessageSecurityMode(MessageSecurityMode messageSecurityMode) {
        this.messageSecurityMode = messageSecurityMode;
    }

    public RSAPublicKey getPublicKey() {
        return (RSAPublicKey)certificate.getPublicKey();
    }
}
