package opcua.util;

import opcua.context.Context;
import opcua.message.OpenSecureChannelRequest;
import opcua.message.parts.*;
import opcua.security.CertificateUtil;
import opcua.security.CryptoUtility;
import opcua.security.SecurityPolicy;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

public class MessageUtility {

    public static byte[] getSignedEncrypted(OpenSecureChannelRequest message, Context context) throws CertificateEncodingException {
        byte[] remoteCertificateThumbprint = CertificateUtil.createSha1Thumbprint(context.getRemoteCertificate());
        return MessageUtility.getSignedEncrypted(
                message,
                context.getSecurityPolicy(),
                context.getLocalCertificate(),
                (RSAPublicKey)context.getRemoteCertificate().getPublicKey(),
                context.getLocalPrivateKey(),
                remoteCertificateThumbprint
        );
    }

    public static byte[] getSignedEncrypted(OpenSecureChannelRequest message, SecurityPolicy securityPolicy, X509Certificate localCertificate,
                                            RSAPublicKey remotePublicKey, RSAPrivateKey localPrivateKey, byte[] remoteCertificateThumbprint)
    {

        /* Build message */

        byte[] body = message.toBinary();
        byte[] sequenceHeaderBytes = new SequenceHeader(1, 1).toBinary(); //TODO: SeqNum generator
        byte[] securityHeaderBytes;
        try {
            securityHeaderBytes = new AsymmetricSecurityHeader(securityPolicy, localCertificate, remoteCertificateThumbprint).toBinary();
        } catch (CertificateEncodingException e) {
            throw new Error(e);
        }

        int signatureSize = localPrivateKey.getModulus().bitLength() / 8;
        int plainBlockSize = CryptoUtility.getAsymPlainTextBlockSize(securityPolicy.getAsymmetricEncryption(), remotePublicKey);
        int cipherBlockSize = remotePublicKey.getModulus().bitLength() / 8;

        //Calculate padding
        int paddingSizeSize = remotePublicKey.getModulus().bitLength() > 2048 ? 2 : 1;   //Literally the size of the PaddingSize field
        int encryptedBytesLength = body.length + sequenceHeaderBytes.length + signatureSize + paddingSizeSize;
        int paddingSize = encryptedBytesLength % plainBlockSize != 0 ? plainBlockSize - (encryptedBytesLength % plainBlockSize) : 0;
        encryptedBytesLength += paddingSize;
        byte[] padding = CryptoUtility.generatePadding(paddingSize, paddingSize == 2);

        //Calculate size of encrypted message (for message header)
        int numBlocks = encryptedBytesLength / plainBlockSize;
        int cipherTextSize = numBlocks * cipherBlockSize;
        int messageSize = 12 + securityHeaderBytes.length + cipherTextSize;

        byte[] messageHeaderBytes = new SecureConversationMessageHeader(MessageType.OPN, IsFinal.FINAL_CHUNK, messageSize, 0).toBinary();


        /* Sign */

        byte[] bytesToSign = new BinarySerializer()
                .putBytes(messageHeaderBytes)
                .putBytes(securityHeaderBytes)
                .putBytes(sequenceHeaderBytes)
                .putBytes(body)
                .putBytes(padding)
                .get();

        byte[] signature;
        try {
            signature = CryptoUtility.sign(localPrivateKey, bytesToSign, securityPolicy);
        } catch (InvalidKeyException | SignatureException e) {
            throw new Error(e);
        }


        /* Encrypt */

        byte[] bytesToEncrypt = new BinarySerializer()
                .putBytes(sequenceHeaderBytes)
                .putBytes(body)
                .putBytes(padding)
                .putBytes(signature)
                .get();

        byte[] cipherText = new byte[cipherTextSize];
        try {
            for(int i = 0; i<numBlocks; i++) {
                byte[] plainBlock = Arrays.copyOfRange(bytesToEncrypt, i*plainBlockSize, (i+1)*plainBlockSize);
                byte[] cipherblock = CryptoUtility.encrypt(remotePublicKey, plainBlock, securityPolicy);
                System.arraycopy(cipherblock, 0, cipherText, i*cipherBlockSize, cipherBlockSize);
            }
        } catch (BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            throw new Error(e);
        }

        return new BinarySerializer()
                .putBytes(messageHeaderBytes)
                .putBytes(securityHeaderBytes)
                .putBytes(cipherText)
                .get();
    }
}
