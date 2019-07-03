package opcua.util;

import opcua.context.Context;
import opcua.message.ConnectionProtocolMessage;
import opcua.message.Message;
import opcua.message.OpenSecureChannelRequest;
import opcua.message.parts.*;
import opcua.security.CertificateUtil;
import opcua.security.CryptoUtility;
import opcua.security.MessageSecurityMode;
import opcua.security.SecurityPolicy;
import transport.Connection;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

public class MessageSender {

    public static void sendMessage(ConnectionProtocolMessage message, Context context) throws IOException {
        byte[] body = message.toBinary();
        byte[] header = new ConnectionProtocolMessageHeader(message.getMessageType(), body.length + 8).toBinary();
        byte[] payload = new BinarySerializer().putBytes(header).putBytes(body).get();
        context.getConnection().sendData(payload);
    }

    public static void sendMessage(OpenSecureChannelRequest message, Context context) throws IOException {
        SequenceHeader sequenceHeader = new SequenceHeader(1, 1); //TODO: SeqNum generator
        byte[] body = message.toBinary();

        if(context.getMessageSecurityMode() == MessageSecurityMode.NONE) {
            AsymmetricSecurityHeader securityHeader = new AsymmetricSecurityHeader(SecurityPolicy.NONE, null, null);
            byte[] securityHeaderBytes;
            try {
                securityHeaderBytes = securityHeader.toBinary();
            } catch (CertificateEncodingException e) {
                throw new Error(e);
            }

            byte[] sequenceHeaderBytes = sequenceHeader.toBinary();

            int messageSize = 12 + securityHeaderBytes.length + sequenceHeaderBytes.length + body.length;
            SecureConversationMessageHeader messageHeader = new SecureConversationMessageHeader(MessageType.OPN, IsFinal.FINAL_CHUNK, messageSize, 0);

            byte[] payload = new BinarySerializer()
                    .putBytes(messageHeader.toBinary())
                    .putBytes(securityHeaderBytes)
                    .putBytes(sequenceHeaderBytes)
                    .putBytes(body)
                    .get();

            context.getConnection().sendData(payload);
        }
        else if(context.getMessageSecurityMode() == MessageSecurityMode.SIGN) {
            //TODO
        }
        else {
            try {
                context.getConnection().sendData(MessageUtility.getSignedEncrypted(message, context));
            } catch (CertificateEncodingException e) {
                throw new Error(e);
            }
        }
    }

    public static void sendBytes(byte[] payload, Context context) throws IOException {
        context.getConnection().sendData(payload);
    }
}
