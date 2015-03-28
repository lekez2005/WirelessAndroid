package com.jaykhon.wireless.wireless.authorize;

/**
 * Created by lekez2005 on 3/27/15.
 */
import java.math.BigInteger;
import java.security.SecureRandom;

public final class SessionIdentifierGenerator {
    private SecureRandom random = new SecureRandom();

    public String nextSessionId() {
        return new BigInteger(130, random).toString(32);
    }
}
