package opcua.context;

import opcua.security.MessageSecurityMode;
import opcua.security.SecurityPolicy;
import transport.Connection;

import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;

public class Context {
    private Connection connection;
    private X509Certificate localCertificate;
    private RSAPrivateKey localPrivateKey;
    private X509Certificate remoteCertificate;
    private SecurityPolicy securityPolicy;
    private MessageSecurityMode messageSecurityMode;
    private String endpointUrl;
    private String remoteHostname;
    private int remotePort;

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public X509Certificate getLocalCertificate() {
        return localCertificate;
    }

    public void setLocalCertificate(X509Certificate localCertificate) {
        this.localCertificate = localCertificate;
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

    public X509Certificate getRemoteCertificate() {
        return remoteCertificate;
    }

    public void setRemoteCertificate(X509Certificate remoteCertificate) {
        this.remoteCertificate = remoteCertificate;
    }

    public RSAPrivateKey getLocalPrivateKey() {
        return localPrivateKey;
    }

    public void setLocalPrivateKey(RSAPrivateKey localPrivateKey) {
        this.localPrivateKey = localPrivateKey;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public String getRemoteHostname() {
        return remoteHostname;
    }

    public void setRemoteHostname(String remoteHostname) {
        this.remoteHostname = remoteHostname;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
}
