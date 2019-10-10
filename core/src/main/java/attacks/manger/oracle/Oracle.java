package attacks.manger.oracle;



/**
 * Abstraction for a PKCS#1 padding oracle.
 */
public abstract class Oracle {
    private long queryCount;
    private VictimProxy victimProxy;

    /**
     * Queries the oracle
     * @param cipherBlock The cipher block to query
     * @return Whether cipherBlock is valid
     * @throws OracleException
     */
    public abstract boolean checkValidity(byte[] cipherBlock) throws OracleException;

    void incrementQueryCount() {
        queryCount++;
    }

    public long getQueryCount() {
        return queryCount;
    }

    public VictimProxy getVictimProxy() throws OracleException {
        if(victimProxy == null) {
            throw new OracleException("No victim proxy was set to query");
        }
        return victimProxy;
    }

    public void setVictimProxy(VictimProxy victimProxy) {
        this.victimProxy = victimProxy;
    }
}
