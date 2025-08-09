package org.example.tx;

import org.example.Utils.Helper;
import org.example.ecc.Int;
import org.example.script.Script;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public record TxOut(Int amount, Script scriptPubkey) {

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return amount + ":" + scriptPubkey;
    }

    /**
     * Parse
     * @param s a {@link ByteArrayInputStream}
     * @return a {@link TxOut} object
     * @throws IOException Stream Exception
     */
    public static TxOut parse(ByteArrayInputStream s) throws IOException {
        Int amount = Helper.littleEndianToInt(s.readNBytes(8));

        Script scriptPubkey = Script.parse(s);

        return new TxOut(amount, scriptPubkey);
    }

    /**
     * Returns the byte serialization of the transaction output
     * @return a {@code byte} array
     */
    public byte[] serialize() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        result.writeBytes(amount().toBytesLittleEndian(8));

        result.writeBytes(scriptPubkey.serialize());

        return result.toByteArray();
    }
}
