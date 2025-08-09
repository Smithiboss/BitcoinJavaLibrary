package org.example.tx;

import org.example.Utils.Bytes;
import org.example.Utils.Helper;
import org.example.ecc.Hex;
import org.example.ecc.Int;
import org.example.script.Script;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class TxIn {

    private final Int prevTx;         // 32 bytes
    private final Int prevIndex;         // 4 bytes
    private final Script scriptSig;      // variable
    private final Int sequence;          // 4 bytes

    public TxIn(Int prevTx, Int prevIndex, Script scriptSig, Int sequence) {
        this.prevTx = prevTx;
        this.prevIndex = prevIndex;
        this.scriptSig = scriptSig;
        this.sequence = sequence;
    }

    @Override
    public String toString() {
        return prevTx + ":" + prevIndex;
    }

    /**
     * Takes a byte stream and parses the txInput at the start.
     * @param s a {@link ByteArrayInputStream}
     * @return a {@link TxIn} object
     */
    public static TxIn parse(ByteArrayInputStream s) {
        // Get previous transaction ID
        Int prevTx = Hex.parse(Bytes.reverseOrder(Bytes.read(s, 32)));
        // Get previous transaction index
        Int prevIndex = Helper.littleEndianToInt(Bytes.read(s, 4));
        // Get ScriptSig
        Script scriptSig = Script.parse(s);
        // Get sequence
        Int sequence = Helper.littleEndianToInt(Bytes.read(s, 4));

        return new TxIn(prevTx, prevIndex, scriptSig, sequence);
    }

    /**
     * Returns the byte serialization of the transaction input
     * @return a {@code byte} array
     */
    public byte[] serialize() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        result.writeBytes(prevTx.toBytesLittleEndian(32));
        result.writeBytes(prevIndex.toBytesLittleEndian(4));
        result.writeBytes(scriptSig.serialize());
        result.writeBytes(sequence.toBytesLittleEndian(4));

        return result.toByteArray();
    }

    /**
     *
     * @param testnet a {@code boolean}
     * @param fresh ???
     * @return a {@link Tx} object
     */
    public Tx fetch(boolean testnet, boolean fresh) {
        return TxFetcher.fetch(prevTx.toHex().toString(), testnet, fresh);
    }

    /**
     * Get the output value by looking up the tx hash. Returns the amount in satoshi.
     * @param testnet a {@code boolean}
     * @param fresh ???
     * @return a {@link Int} object
     */
    public Int value(boolean testnet, boolean fresh) {
        Tx tx = fetch(testnet, fresh);
        return tx.getTxOuts().get((prevIndex.intValue())).amount();
    }

    /**
     * Get the ScriptPubKey by looking up the tx hash. Returns a Script object.
     * @param testnet a {@code boolean}
     * @param fresh ???
     * @return a {@link Object} object
     */
    public Script scriptPubkey(boolean testnet, boolean fresh) {
        Tx tx = fetch(testnet, fresh);
        return tx.getTxOuts().get(prevIndex.intValue()).scriptPubkey();
    }

    public Int getPrevTx() {
        return prevTx;
    }

    public Int getPrevIndex() {
        return prevIndex;
    }

    public Script getScriptSig() {
        return scriptSig;
    }

    public Int getSequence() {
        return sequence;
    }
}
