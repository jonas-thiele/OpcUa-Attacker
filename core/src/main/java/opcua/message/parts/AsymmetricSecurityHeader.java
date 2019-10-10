package opcua.message.parts;

import opcua.encoding.EncodingException;
import opcua.security.CertificateUtility;
import opcua.security.SecurityPolicy;
import opcua.encoding.BinarySerializer;
import opcua.encoding.MessageInputStream;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Asymmetric Security Header (OPC UA Part 6, p. 48)
 */
public class AsymmetricSecurityHeader implements SecurityHeader {
    private SecurityPolicy securityPolicy;
    private X509Certificate senderCertificate;
    private byte[] receiverCertificateThumbprint;

    public AsymmetricSecurityHeader(SecurityPolicy securityPolicy, X509Certificate senderCertificate, byte[] receiverCertificateThumbprint) {
        this.securityPolicy = securityPolicy;
        this.senderCertificate = senderCertificate;
        this.receiverCertificateThumbprint = receiverCertificateThumbprint;
    }

    public AsymmetricSecurityHeader() { }

    public byte[] toBinary() throws EncodingException {
        byte[] derEncodedCertificate = new byte[0];
        try {
            derEncodedCertificate = senderCertificate != null ? senderCertificate.getEncoded() : null;
        } catch (CertificateEncodingException e) {
            throw new EncodingException("Unable to encode AsymmetricSecurityHeader", e);
        }

        return new BinarySerializer()
                .putByteArray(securityPolicy.getUriBytes())
                .putByteArray(derEncodedCertificate)
                .putByteArray(receiverCertificateThumbprint)
                .get();
    }

    public static AsymmetricSecurityHeader constructFromBinary(MessageInputStream stream) throws IOException, CertificateException {
        AsymmetricSecurityHeader securityHeader = new AsymmetricSecurityHeader();
        securityHeader.setSecurityPolicy(SecurityPolicy.fromUri(new String(stream.readByteArray())));
        byte[] encodedCertificate = stream.readByteArray();
        if(encodedCertificate != null) {
            securityHeader.setSenderCertificate(CertificateUtility.decodeX509FromDer(stream.readByteArray()));
        }
        securityHeader.setReceiverCertificateThumbprint(stream.readByteArray());
        return securityHeader;
    }


    public SecurityPolicy getSecurityPolicy() {
        return securityPolicy;
    }

    public void setSecurityPolicy(SecurityPolicy securityPolicy) {
        this.securityPolicy = securityPolicy;
    }

    public X509Certificate getSenderCertificate() {
        return senderCertificate;
    }

    public void setSenderCertificate(X509Certificate senderCertificate) {
        this.senderCertificate = senderCertificate;
    }

    public byte[] getReceiverCertificateThumbprint() {
        return receiverCertificateThumbprint;
    }

    public void setReceiverCertificateThumbprint(byte[] receiverCertificateThumbprint) {
        this.receiverCertificateThumbprint = receiverCertificateThumbprint;
    }
}
