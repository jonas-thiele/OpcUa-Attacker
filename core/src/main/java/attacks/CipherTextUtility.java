package attacks;

import opcua.message.parts.MessageType;
import opcua.security.SecurityPolicy;
import opcua.encoding.MessageInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

/**
 * Helper functions for ciphertexts
 */
public class CipherTextUtility {

    /**
     * Returns offset of first cipher block in an encrypted OpenSecureChannelRequest
     * @param ciphertext The cipher text of the message
     * @param publicKey Public key under which the cipher block was encrypted
     * @return Offset of first block in bytes
     * @throws IOException
     */
    public static int getOpnBlockOffset(byte[] ciphertext, RSAPublicKey publicKey) throws IOException {
        MessageInputStream messageStream = new MessageInputStream(new ByteArrayInputStream(ciphertext));
        if (MessageType.fromIdentifier(messageStream.readBytes(3)) != MessageType.OPN) {
            throw new IllegalArgumentException("Invalid message type, expected OPN");
        }
        messageStream.skipBytes(1);
        long messageSize = messageStream.readUInt32();
        messageStream.skipBytes(4); //Skip SecureChannelId

        int securityPolicyLength = (int)messageStream.readUInt32();
        SecurityPolicy securityPolicy = SecurityPolicy.fromUri(new String(messageStream.readBytes(securityPolicyLength)));

        int senderCertificateLength = (int)messageStream.readUInt32();
        messageStream.skipBytes(senderCertificateLength);  //SenderCertificate
        int receiverCertificateThumbprintLength = (int)messageStream.readUInt32();
        messageStream.skipBytes(receiverCertificateThumbprintLength);  //ReceiverCertificateThumbprint

        return 12 + 4 + securityPolicyLength + 4 + senderCertificateLength + 4 + receiverCertificateThumbprintLength;
    }

    /**
     * Inserts a cipher block into a ciphertext
     * @param originalCipherText The ciphertext
     * @param cipherBlock Cipher block to insert
     * @param encryptedOffset Offset of first cipher block
     * @param cipherBlockSize Size of an cipher block
     * @param blockNumber Index of the cipher block to replace
     */
    public static void insertCipherBlock(byte[] originalCipherText, byte[] cipherBlock, int encryptedOffset, int cipherBlockSize, int blockNumber) {
        System.arraycopy(cipherBlock, 0, originalCipherText, encryptedOffset + blockNumber * cipherBlockSize, cipherBlockSize);
    }

    /**
     * Retrieves a specific block from a ciphertext
     * @param ciphertext The ciphertext
     * @param encryptedOffset Offset of first cipher block
     * @param blockSize Size of an cipher block
     * @param blockNumber Index of the block to returned
     * @return The cipher block
     */
    public static byte[] extractCipherBlock(byte[] ciphertext, int encryptedOffset, int blockSize, int blockNumber) {
        return Arrays.copyOfRange(ciphertext, encryptedOffset + blockNumber*blockSize, encryptedOffset + (blockNumber+1)*blockSize);
    }

    /**
     * Inserts a cipher block into a ciphertext
     * @param originalCiphertext The ciphertext
     * @param cipherBlock Cipher block to insert
     * @param blockNumber Index of the cipher block to replace
     * @param publicKey The public key used for encryption
     * @throws IOException
     */
    public static void insertCipherBlock(byte[] originalCiphertext, byte[] cipherBlock, int blockNumber, RSAPublicKey publicKey) throws IOException {
        int encryptedOffset = getOpnBlockOffset(originalCiphertext, publicKey);
        int blockSize = publicKey.getModulus().bitLength() / 8;
        insertCipherBlock(originalCiphertext, cipherBlock, encryptedOffset, blockSize, blockNumber);
    }

    /**
     * Retrieves a specific block from a ciphertext
     * @param ciphertext The ciphertext
     * @param blockNumber Index of the block to returned
     * @param publicKey The public key used for encryption
     * @return The cipher block
     */
    public static byte[] extractCipherBlock(byte[] ciphertext, int blockNumber, RSAPublicKey publicKey) throws IOException {
        int encryptedOffset = getOpnBlockOffset(ciphertext, publicKey);
        int blockSize = publicKey.getModulus().bitLength() / 8;
        return extractCipherBlock(ciphertext, encryptedOffset, blockSize, blockNumber);
    }
}
