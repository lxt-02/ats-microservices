package com.ats.shared.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecureTokensTest {

    @Test
    void pkceChallengeMatchesRfc7636Example() {
        assertEquals("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM",
                SecureTokens.pkceChallenge("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk"));
    }

    @Test
    void randomTokensAreUrlSafeAndUnique() {
        String a = SecureTokens.randomUrlSafe(32);
        String b = SecureTokens.randomUrlSafe(32);
        assertEquals(43, a.length());
        assertTrue(a.matches("[A-Za-z0-9_-]+"));
        assertNotEquals(a, b);
    }

    @Test
    void sha256HexIsStable() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", SecureTokens.sha256Hex("abc"));
    }

    @Test
    void constantTimeEqualsHandlesNulls() {
        assertTrue(SecureTokens.constantTimeEquals("x", "x"));
        assertFalse(SecureTokens.constantTimeEquals("x", "y"));
        assertFalse(SecureTokens.constantTimeEquals(null, "x"));
        assertFalse(SecureTokens.constantTimeEquals(null, null));
    }
}
