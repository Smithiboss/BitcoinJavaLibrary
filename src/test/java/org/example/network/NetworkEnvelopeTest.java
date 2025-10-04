package org.example.network;

import org.example.utils.Bytes;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class NetworkEnvelopeTest {

    @Test
    public void testParse() {
        var msg = "f9beb4d976657261636b000000000000000000005df6e0e2";
        var envelope = NetworkEnvelope.parse(msg, false);
        assertArrayEquals("verack".getBytes(), envelope.getCommand());
        assertArrayEquals("".getBytes(), envelope.getPayload());
        msg = "f9beb4d976657273696f6e0000000000650000005f1a69d2721101000100000000000000bc8f5e5400000000010000000000000000000000000000000000ffffc61b6409208d010000000000000000000000000000000000ffffcb0071c0208d128035cbc97953f80f2f5361746f7368693a302e392e332fcf05050001";
        envelope = NetworkEnvelope.parse(msg, false);
        assertArrayEquals("version".getBytes(), envelope.getCommand());
        assertArrayEquals(Arrays.copyOfRange(Bytes.hexStringToByteArray(msg), 24, Bytes.hexStringToByteArray(msg).length), envelope.getPayload());
    }

    @Test
    public void testSerialize() {
        var msg = "f9beb4d976657261636b000000000000000000005df6e0e2";
        var envelope = NetworkEnvelope.parse(msg, false);
        assertArrayEquals(Bytes.hexStringToByteArray(msg), envelope.serialize());
        msg = "f9beb4d976657273696f6e0000000000650000005f1a69d2721101000100000000000000bc8f5e5400000000010000000000000000000000000000000000ffffc61b6409208d010000000000000000000000000000000000ffffcb0071c0208d128035cbc97953f80f2f5361746f7368693a302e392e332fcf05050001";
        envelope = NetworkEnvelope.parse(msg, false);
        assertArrayEquals(Bytes.hexStringToByteArray(msg), envelope.serialize());
    }
}
