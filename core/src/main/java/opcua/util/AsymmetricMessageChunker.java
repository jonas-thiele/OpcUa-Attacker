package opcua.util;


import opcua.message.parts.MessageType;
import opcua.security.CertificateUtil;
import opcua.security.MessageSecurityMode;
import opcua.security.SecurityPolicy;
import opcua.security.SequenceNumberGenerator;

import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.function.Function;

public class AsymmetricMessageChunker {
    private final long maxChunkSize;
    private final MessageSecurityMode messageSecurityMode;
    private final SecurityPolicy securityPolicy;
    private final X509Certificate ownCertificate;
    private final PrivateKey privateKey;
    private final X509Certificate remoteCertificate;
    private final byte[] messageBody;
    private final byte[] securityHeader;
    private final int messageHeaderSize = 12;
    private final int sequenceHeaderSize = 4;
    private final long requestId;
    private final MessageType messageType;
    private final SequenceNumberGenerator sequenceNumberGenerator;

    private final int maxPlaintextSize;
    private final boolean needExtraPadding;
    private final int minPaddingSize;

    public AsymmetricMessageChunker(long maxChunkSize, MessageSecurityMode messageSecurityMode, SecurityPolicy securityPolicy,
                                    X509Certificate ownCertificate, PrivateKey privateKey, X509Certificate remoteCertificate,
                                    byte[] messageBody, MessageType messageType, long requestId,
                                    SequenceNumberGenerator sequenceNumberGenerator) throws CertificateEncodingException
    {
        this.maxChunkSize = maxChunkSize;
        this.messageSecurityMode = messageSecurityMode;
        this.securityPolicy = securityPolicy;
        this.ownCertificate = ownCertificate;
        this.privateKey = privateKey;
        this.remoteCertificate = remoteCertificate;
        this.messageBody = messageBody;
        this.requestId = requestId;
        this.messageType = messageType;
        this.sequenceNumberGenerator = sequenceNumberGenerator;

        securityHeader = new BinarySerializer()
                .putByteArray(securityPolicy.getUriBytes())
                .putByteArray(ownCertificate != null ? ownCertificate.getEncoded(): null)
                .putByteArray(remoteCertificate != null ? CertificateUtil.createSha1Thumbprint(remoteCertificate) : null)
                .get();

        needExtraPadding = remoteCertificate != null &&  ((RSAPublicKey)remoteCertificate.getPublicKey()).getModulus().bitLength() > 2048;
        minPaddingSize = needExtraPadding ? 2 : 1;


        int signatureSize = ((RSAPublicKey)privateKey).getModulus().bitLength() / 8;
        switch (messageSecurityMode) {
            case SIGN_AND_ENCRYPT:
                int cipherBlockSize = ((RSAPublicKey)remoteCertificate.getPublicKey()).getModulus().bitLength() / 8;
                int maxEncryptedSize = (int)maxChunkSize - messageHeaderSize - securityHeader.length - minPaddingSize;
                maxEncryptedSize = maxEncryptedSize - (maxEncryptedSize % cipherBlockSize);
                maxPlaintextSize = maxEncryptedSize - sequenceHeaderSize - signatureSize - minPaddingSize;
                break;

            case SIGN:
                maxPlaintextSize = (int)maxChunkSize - messageHeaderSize - securityHeader.length - signatureSize;
                break;

            default:
                maxPlaintextSize = (int)maxChunkSize - messageHeaderSize - securityHeader.length - sequenceHeaderSize;
                break;
        }


    }

    byte[][] getChunks() {
        int chunkCount = (messageBody.length + maxPlaintextSize -1) / maxPlaintextSize; //TODO: Check agains maxChunkCount
        byte[][] result = new byte[chunkCount][];

        for(int i = 0; i < chunkCount; i++) {
            // Build plaintext
            byte[] dataToEncrypt = new BinarySerializer()
                    // Sequence header
                    .putUInt32(sequenceNumberGenerator.getNextSequenceNumber())
                    .putUInt32(requestId)
                    // Body
                    .putBytes(messageBody)
                    .get(); //TODO
        }
        return result;
    }
}
