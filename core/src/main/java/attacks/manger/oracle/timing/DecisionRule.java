package attacks.manger.oracle.timing;

import attacks.manger.oracle.OracleException;
import attacks.manger.oracle.VictimProxy;

/**
 * Represents a decision rule to predict ciphertext validity based on response times.
 */
public interface DecisionRule {
    /**
     * Initial calibration phase
     * @throws OracleException
     */
    void learn() throws OracleException;

    /**
     * Predicts validity of an cipher block
     * @param cipherBlock Queried cipher block
     * @return The prediction
     * @throws OracleException
     */
    boolean predict(byte[] cipherBlock) throws OracleException;

    void setVictimProxy(VictimProxy victimProxy);
    VictimProxy getVictimProxy();
}
