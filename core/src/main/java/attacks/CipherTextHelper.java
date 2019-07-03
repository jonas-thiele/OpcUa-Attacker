package attacks;

import opcua.message.parts.MessageType;
import opcua.security.CryptoUtility;
import opcua.security.SecurityPolicy;
import opcua.util.MessageInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.interfaces.RSAPublicKey;

public class CipherTextHelper {

    public static int getOpnBlockOffset(byte[] cipherText, RSAPublicKey publicKey) throws IOException {
        MessageInputStream messageStream = new MessageInputStream(new ByteArrayInputStream(cipherText));
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

    public static byte[] getCipherBlock(byte[] cipherText, int encryptedOffset, int cipherBlockSize, int blockNumber) {
        byte[] block = new byte[cipherBlockSize];
        System.arraycopy(cipherText, encryptedOffset + blockNumber * cipherBlockSize, block, 0, cipherBlockSize);
        return block;
    }

    public static void insertCipherBlock(byte[] originalCipherText, byte[] cipherBlock, int encryptedOffset, int cipherBlockSize, int blockNumber) {
        System.arraycopy(cipherBlock, 0, originalCipherText, encryptedOffset + blockNumber * cipherBlockSize, cipherBlockSize);
    }
}
