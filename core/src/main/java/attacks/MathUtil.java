package attacks;

import java.math.BigInteger;

public class MathUtil {
    public static BigInteger divideCeiling(BigInteger nominator, BigInteger divisor) {
        if(nominator.mod(divisor).equals(BigInteger.ZERO)) {
            return nominator.divide(divisor);
        }
        return nominator.divide(divisor).add(BigInteger.ONE);
    }
}
