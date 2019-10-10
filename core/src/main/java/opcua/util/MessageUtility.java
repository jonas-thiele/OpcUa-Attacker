package opcua.util;

import opcua.context.Endpoint;
import opcua.context.LocalKeyPair;
import opcua.encoding.BinarySerializer;
import opcua.encoding.EncodingException;
import opcua.message.ErrorMessage;
import opcua.message.Message;
import opcua.message.SecureConversationMessage;
import opcua.message.parts.*;
import opcua.security.CertificateUtility;
import opcua.security.AsymCryptoUtility;
import opcua.security.SecurityPolicy;
import transport.TransportException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

/**
 * Provides utility functions for OPC UA messages
 */
public class MessageUtility {

    /**
     * Signs and encrypts a message
     * @param message SecureConversationMessage
     * @param endpoint Receiver endpoint
     * @param localKeyPair Sender keypair
     * @return Ciphertext
     * @throws EncodingException
     */
    public static byte[] getSignedEncrypted(SecureConversationMessage message, Endpoint endpoint, LocalKeyPair localKeyPair) throws EncodingException {
        return getSignedEncrypted(message, endpoint.getSecurityPolicy(), localKeyPair, endpoint.getCertificate());
    }

    /**
     * Signs and encrypts a message
     * @param message SecureConversationMessage
     * @param securityPolicy Security policy to use
     * @param localKeyPair Sender keypair
     * @param remoteCertificate Receiver's certificate
     * @return Ciphertext
     * @throws EncodingException
     */
    public static byte[] getSignedEncrypted(SecureConversationMessage message, SecurityPolicy securityPolicy,
                                            LocalKeyPair localKeyPair, X509Certificate remoteCertificate) throws EncodingException {
        try {
            byte[] remoteCertificateThumbprint = CertificateUtility.createSha1Thumbprint(remoteCertificate);
            return getSignedEncrypted(message, securityPolicy, localKeyPair, (RSAPublicKey)remoteCertificate.getPublicKey(), remoteCertificateThumbprint);
        } catch (CertificateEncodingException e) {
            throw new EncodingException(e);
        }
    }

    /**
     * Signs and encrypts a message
     * @param message SecureConversationMessage
     * @param securityPolicy Security policy to use
     * @param localKeyPair Sender keypair
     * @param remotePublicKey Receiver's public key
     * @param remoteCertificateThumbprint Sha1 thumbprint of receiver's certificate
     * @return Ciphertext
     */
    public static byte[] getSignedEncrypted(SecureConversationMessage message, SecurityPolicy securityPolicy,
                                            LocalKeyPair localKeyPair, RSAPublicKey remotePublicKey, byte[] remoteCertificateThumbprint)
    {
        /* Build message */
        byte[] body = message.toBinary();
        byte[] sequenceHeaderBytes = new SequenceHeader(1, 1).toBinary(); //TODO: SeqNum generator
        byte[] securityHeaderBytes;
        try {
            securityHeaderBytes = new AsymmetricSecurityHeader(securityPolicy, localKeyPair.getCertificate(), remoteCertificateThumbprint).toBinary();
        } catch (EncodingException e) {
            throw new Error(e);
        }

        int signatureSize = localKeyPair.getPrivateKey().getModulus().bitLength() / 8;
        int plainBlockSize = AsymCryptoUtility.getAsymPlainTextBlockSize(securityPolicy.getAsymmetricEncryption(), remotePublicKey);
        int cipherBlockSize = remotePublicKey.getModulus().bitLength() / 8;

        //Calculate padding
        int paddingSizeSize = remotePublicKey.getModulus().bitLength() > 2048 ? 2 : 1;   //Literally the size of the PaddingSize field
        int encryptedBytesLength = body.length + sequenceHeaderBytes.length + signatureSize + paddingSizeSize;
        int paddingSize = encryptedBytesLength % plainBlockSize != 0 ? plainBlockSize - (encryptedBytesLength % plainBlockSize) : 0;
        encryptedBytesLength += paddingSize;
        byte[] padding = AsymCryptoUtility.generatePadding(paddingSize, paddingSize == 2);

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
            signature = AsymCryptoUtility.sign(localKeyPair.getPrivateKey(), bytesToSign, securityPolicy);
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
                byte[] cipherblock = AsymCryptoUtility.encrypt(remotePublicKey, plainBlock, securityPolicy);
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


    /* Functions to simplify message checks */

    public static void throwIfUnexpectedType(Message message, Class<? extends Message> expected) throws TransportException {
        throwIfError(message);
        if(!message.getClass().equals(expected)) {
            throw new TransportException("Expected message type" + expected.getName() + ", got " + message.getClass().getName());
        }
    }

    public static void throwIfUnexpectedType(Message message, Class<? extends Message> expected, String what) throws TransportException {
        throwIfError(message, what);
        if(!message.getClass().equals(expected)) {
            throw new TransportException(what);
        }
    }

    public static void throwIfError(Message message) throws TransportException {
        if(message.getMessageType().isError()) {
            throw new TransportException((ErrorMessage)message);
        }
    }

    public static void throwIfError(Message message, String what) throws TransportException {
        if(message.getMessageType().isError()) {
            throw new TransportException(what, (ErrorMessage)message);
        }
    }
}
