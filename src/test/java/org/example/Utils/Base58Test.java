package org.example.Utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class Base58Test {

    @Test
    public void testBase58Encoder() {
        String firstHex = "7c076ff316692a3d7eb3c3bb0f8b1488cf72e1afcd929e29307032997a838a3d";
        String secondHex = "eff69ef2b1bd93a66ed5219add4fb51e11a840f404876325a1e8ffe0529a2c";
        String thirdHex = "c7207fee197d27c618aea621406f6bf5ef6fca38681d82b2f06fddbdce6feab6";

        assertEquals("9MA8fRQrT4u8Zj8ZRd6MAiiyaxb2Y1CMpvVkHQu5hVM6", Base58.encode(Base58.hexStringToBytes(firstHex)));
        assertEquals("4fE3H2E6XMp4SsxtwinF7w9a34ooUrwWe4WsW1458Pd", Base58.encode(Base58.hexStringToBytes(secondHex)));
        assertEquals("EQJsjkd6JaGwxrjEhfeqPenqHwrBmPQZjJGNSCHBkcF7", Base58.encode(Base58.hexStringToBytes(thirdHex)));
    }

}
