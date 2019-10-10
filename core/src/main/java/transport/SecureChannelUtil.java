package transport;

import opcua.context.Endpoint;
import opcua.context.LocalKeyPair;
import opcua.context.StaticConfig;
import opcua.encoding.EncodingException;
import opcua.message.ErrorMessage;
import opcua.message.Message;
import opcua.message.OpenSecureChannelRequest;
import opcua.message.OpenSecureChannelResponse;
import opcua.message.parts.MessageType;
import opcua.model.type.SecurityTokenRequestType;
import opcua.security.MessageSecurityMode;
import opcua.security.SecurityPolicy;
import opcua.util.MessageUtility;

import java.security.SecureRandom;

/**
 * Utility functions for secure channels
 */
public class SecureChannelUtil {

    /**
     * Establishes a secure channel on a transport connection
     * @param transportContext Context of transport conection
     * @return The secure channel context
     * @throws TransportException
     */
    public static SecureChannelContext establishSecureChannel(TransportContext transportContext) throws TransportException {
        OpenSecureChannelRequest opnRequest = new OpenSecureChannelRequest(StaticConfig.PROTOCOL_VERSION, SecurityTokenRequestType.ISSUE,
                MessageSecurityMode.NONE, null, StaticConfig.REQUESTED_LIFETIME);
        MessageSender.sendMessage(opnRequest, transportContext);
        Message response = MessageReceiver.receiveMessage(transportContext.getConnection(), MessageSecurityMode.NONE);
        if(response.getMessageType().isError()) {
            throw new TransportException("Unable to establish secure channel", (ErrorMessage)response);
        }
        if(response.getMessageType() != MessageType.OPN) {
            throw new TransportException("Unable to establish secure channel");
        }

        OpenSecureChannelResponse opnResponse = (OpenSecureChannelResponse)response;

        return new SecureChannelContext(transportContext, opnResponse.getServerProtocolVersion(), MessageSecurityMode.NONE,
                SecurityPolicy.NONE, opnResponse.getSecurityToken(), null, opnResponse.getServerNonce());
    }

    /**
     * Generates a new random nonce according to a security policy
     * @param securityPolicy Security policy that defines the nonce length
     * @return A random nonce
     */
    public static byte[] generateRandomNonce(SecurityPolicy securityPolicy) {
        return generateRandomNonce(securityPolicy.getSymmetricEncryptionNonceLength());
    }

    /**
     * Generates a random nonce
     * @param nonceLength Length of the nonce (in bytes)
     * @return A random nonce
     */
    public static byte[] generateRandomNonce(int nonceLength) {
        byte[] nonce = new byte[SecurityPolicy.BASIC256.getSymmetricEncryptionNonceLength()];
        SecureRandom random = new SecureRandom();
        random.nextBytes(nonce);
        return nonce;
    }

    /**
     * Generates the ciphertext of a new OpenSecureChannelRequest
     * @param endpoint The endpoint that the request is addressed to
     * @param localKeyPair Local certificate
     * @return The ciphertext
     * @throws EncodingException
     */
    public static byte[] generateEncryptedOpnRequest(Endpoint endpoint, LocalKeyPair localKeyPair) throws EncodingException {
        OpenSecureChannelRequest opnRequest = new OpenSecureChannelRequest(
                StaticConfig.PROTOCOL_VERSION,
                SecurityTokenRequestType.ISSUE,
                endpoint.getMessageSecurityMode(),
                generateRandomNonce(endpoint.getSecurityPolicy()),
                StaticConfig.REQUESTED_LIFETIME
        );
        return MessageUtility.getSignedEncrypted(opnRequest, endpoint, localKeyPair);
    }
}
