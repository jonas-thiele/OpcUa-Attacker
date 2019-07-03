package opcua.message.parts;

import opcua.security.CertificateUtil;
import opcua.security.SecurityPolicy;
import opcua.util.BinarySerializer;
import opcua.util.MessageInputStream;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class AsymmetricSecurityHeader {

    private SecurityPolicy securityPolicy;
    private X509Certificate senderCertificate;
    private byte[] receiverCertificateThumbprint;


    public AsymmetricSecurityHeader(SecurityPolicy securityPolicy, X509Certificate senderCertificate, byte[] receiverCertificateThumbprint) {
        this.securityPolicy = securityPolicy;
        this.senderCertificate = senderCertificate;
        this.receiverCertificateThumbprint = receiverCertificateThumbprint;
    }

    public AsymmetricSecurityHeader() { }


    public byte[] toBinary() throws CertificateEncodingException {
        byte[] derEncodedCertificate = senderCertificate != null ? senderCertificate.getEncoded() : null;
        return new BinarySerializer()
                .putByteArray(securityPolicy.getUriBytes())
                .putByteArray(derEncodedCertificate)
                .putByteArray(receiverCertificateThumbprint)
                .get();
    }

    public static AsymmetricSecurityHeader constructFromBinary(MessageInputStream stream) throws IOException, CertificateException {
        AsymmetricSecurityHeader securityHeader = new AsymmetricSecurityHeader();
        securityHeader.setSecurityPolicy(SecurityPolicy.valueOf(new String(stream.readByteArray())));
        securityHeader.setSenderCertificate(CertificateUtil.decodeX509FromDer(stream.readString().getBytes()));
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
