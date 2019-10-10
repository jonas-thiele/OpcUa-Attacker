package attacks.manger.oracle;

import attacks.manger.oracle.timing.DecisionRule;

/**
 * Oracle for the exploitation of timing differences
 */
public class TimingOracle extends Oracle {
    private final DecisionRule decisionRule;

    /**
     * Constructor
     * @param decisionRule Decision rule
     * @throws OracleException
     */
    public TimingOracle(DecisionRule decisionRule) throws OracleException {
        this.decisionRule = decisionRule;
    }

    @Override
    public boolean checkValidity(byte[] message) throws OracleException {
        incrementQueryCount();
        return decisionRule.predict(message);
    }

    @Override
    public VictimProxy getVictimProxy() throws OracleException {
        return decisionRule.getVictimProxy();
    }

    @Override
    public void setVictimProxy(VictimProxy victimProxy) {
        decisionRule.setVictimProxy(victimProxy);
    }
}
