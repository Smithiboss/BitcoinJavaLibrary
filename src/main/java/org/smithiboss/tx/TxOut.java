package org.smithiboss.tx;

import org.smithiboss.utils.Bytes;
import org.smithiboss.ecc.Hex;
import org.smithiboss.ecc.Int;
import org.smithiboss.script.Script;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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
     */
    public static TxOut parse(ByteArrayInputStream s) {
        Int amount = Hex.parse(Bytes.reverseOrder(Bytes.read(s, 8)));

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
