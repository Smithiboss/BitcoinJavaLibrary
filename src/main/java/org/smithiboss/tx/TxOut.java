package org.smithiboss.tx;

import org.smithiboss.utils.Bytes;
import org.smithiboss.ecc.Hex;
import org.smithiboss.ecc.Int;
import org.smithiboss.script.Script;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public record TxOut(Int amount, Script scriptPubkey) {

    /**
     * Parses a {@link ByteArrayInputStream} to create a {@link TxOut} object.
     *
     * @param s the {@link ByteArrayInputStream} to parse, containing the serialized transaction output
     *          with the amount and the scriptPubkey.
     * @return a {@link TxOut} object constructed from the parsed data.
     */
    public static TxOut parse(ByteArrayInputStream s) {
        // read the amount - 8 bytes
        Int amount = Hex.parse(Bytes.reverseOrder(Bytes.read(s, 8)));
        // parse the scriptPubkey
        Script scriptPubkey = Script.parse(s);
        return new TxOut(amount, scriptPubkey);
    }

    /**
     * Returns the byte serialization of the transaction output
     *
     * @return a {@code byte} array
     */
    public byte[] serialize() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        // write the amount - 8 bytes
        result.writeBytes(amount().toBytesLittleEndian(8));
        // write the serialized scriptPubkey
        result.writeBytes(scriptPubkey.serialize());
        return result.toByteArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return amount + ":" + scriptPubkey;
    }
}
