package opcua.model.type;

import opcua.encoding.BinarySerializer;
import opcua.encoding.MessageInputStream;
import opcua.security.CertificateUtility;
import opcua.security.MessageSecurityMode;
import opcua.security.SecurityPolicy;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * EndpointDesciption (OPC UA Part 4, p.129)
 */
public class EndpointDescription {
    private String endpointUrl;
    private ApplicationDescription applicationDescription;
    private X509Certificate serverCertificate;
    private MessageSecurityMode securityMode;
    private SecurityPolicy securityPolicy;
    private UserTokenPolicy[] userIdentityTokens;
    private String transportProfileUri;
    private int securityLevel;

    public EndpointDescription() { }

    public EndpointDescription(String endpointUrl, ApplicationDescription applicationDescription, X509Certificate serverCertificate, MessageSecurityMode securityMode, SecurityPolicy securityPolicy, UserTokenPolicy[] userIdentityTokens, String transportProfileUri, int securityLevel) {
        this.endpointUrl = endpointUrl;
        this.applicationDescription = applicationDescription;
        this.serverCertificate = serverCertificate;
        this.securityMode = securityMode;
        this.securityPolicy = securityPolicy;
        this.userIdentityTokens = userIdentityTokens;
        this.transportProfileUri = transportProfileUri;
        this.securityLevel = securityLevel;
    }

    public byte[] toBinary() throws CertificateEncodingException {
        BinarySerializer serializer = new BinarySerializer()
                .putString(endpointUrl)
                .putBytes(applicationDescription.toBinary())
                .putBytes(serverCertificate.getEncoded())
                .putEnumeration(securityMode)
                .putString(securityPolicy.getUri())
                .putUInt32(userIdentityTokens.length);
        for(UserTokenPolicy policy : userIdentityTokens) {
            serializer.putBytes(policy.toBinary());
        }
        return serializer
                .putString(transportProfileUri)
                .putUByte(securityLevel)
                .get();
    }

    public static EndpointDescription constructFromBinary(MessageInputStream stream) throws IOException, CertificateException {
        String endpointUrl = stream.readString();
        ApplicationDescription applicationDescription = ApplicationDescription.constructFromBinary(stream);
        X509Certificate serverCertificate = CertificateUtility.decodeX509FromDer(stream.readByteArray());
        MessageSecurityMode securityMode = stream.readEnumeration(MessageSecurityMode.class);
        SecurityPolicy securityPolicy = SecurityPolicy.fromUri(stream.readString());
        int numTokens = (int)stream.readUInt32();
        UserTokenPolicy[] userIdentityTokens = new UserTokenPolicy[numTokens];
        for(int i=0; i<numTokens; i++) {
            userIdentityTokens[i] = UserTokenPolicy.constructFromBinary(stream);
        }
        String transportProfileUri = stream.readString();
        int securityLevel = stream.readUByte();

        return new EndpointDescription(endpointUrl, applicationDescription, serverCertificate, securityMode, securityPolicy, userIdentityTokens, transportProfileUri, securityLevel);
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public ApplicationDescription getApplicationDescription() {
        return applicationDescription;
    }

    public void setApplicationDescription(ApplicationDescription applicationDescription) {
        this.applicationDescription = applicationDescription;
    }

    public X509Certificate getServerCertificate() {
        return serverCertificate;
    }

    public void setServerCertificate(X509Certificate serverCertificate) {
        this.serverCertificate = serverCertificate;
    }

    public MessageSecurityMode getSecurityMode() {
        return securityMode;
    }

    public void setSecurityMode(MessageSecurityMode securityMode) {
        this.securityMode = securityMode;
    }

    public SecurityPolicy getSecurityPolicy() {
        return securityPolicy;
    }

    public void setSecurityPolicy(SecurityPolicy securityPolicy) {
        this.securityPolicy = securityPolicy;
    }

    public UserTokenPolicy[] getUserIdentityTokens() {
        return userIdentityTokens;
    }

    public void setUserIdentityTokens(UserTokenPolicy[] userIdentityTokens) {
        this.userIdentityTokens = userIdentityTokens;
    }

    public String getTransportProfileUri() {
        return transportProfileUri;
    }

    public void setTransportProfileUri(String transportProfileUri) {
        this.transportProfileUri = transportProfileUri;
    }

    public int getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(int securityLevel) {
        this.securityLevel = securityLevel;
    }
}
