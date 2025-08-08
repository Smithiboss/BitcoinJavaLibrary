package org.example.Chapter5;

import org.example.Utils.Bytes;
import org.example.Utils.Helper;
import org.example.ecc.Hex;
import org.example.ecc.Int;
import org.example.script.Script;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HexFormat;

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
     * @throws IOException Exception
     */
    public static TxIn parse(ByteArrayInputStream s) throws Exception {
        // Get previous transaction ID
        Int prevTx = Hex.parse(Bytes.changeOrder(s.readNBytes(32)));
        // Get previous transaction index
        Int prevIndex = Helper.littleEndianToInt(s.readNBytes(4));
        // Get ScriptSig
        Script scriptSig = Script.parse(s);
        // Get sequence
        Int sequence = Helper.littleEndianToInt(s.readNBytes(4));

        return new TxIn(prevTx, prevIndex, scriptSig, sequence);
    }

    /**
     * Returns the byte serialization of the transaction input
     * @return a {@code byte} array
     */
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        result.writeBytes(prevTx.toBytesLittleEndian(32));
        result.writeBytes(prevIndex.toBytesLittleEndian(4));
        result.write(scriptSig.serialize());
        result.writeBytes(sequence.toBytesLittleEndian(4));

        return result.toByteArray();
    }

    /**
     *
     * @param testnet a {@code boolean}
     * @param fresh ???
     * @return a {@link Tx} object
     * @throws Exception Exception
     */
    public Tx fetch(boolean testnet, boolean fresh) throws Exception {
        return TxFetcher.fetch(HexFormat.of().formatHex(prevTx), testnet, fresh);
    }

    /**
     * Get the output value by looking up the tx hash. Returns the amount in satoshi.
     * @param testnet a {@code boolean}
     * @param fresh ???
     * @return a {@link Int} object
     * @throws Exception Exception
     */
    public Int value(boolean testnet, boolean fresh) throws Exception {
        Tx tx = fetch(testnet, fresh);
        return tx.getTxOuts().get((prevIndex.intValue())).amount();
    }

    /**
     * Get the ScriptPubKey by looking up the tx hash. Returns a Script object.
     * @param testnet a {@code boolean}
     * @param fresh ???
     * @return a {@link Object} object
     * @throws IOException Exception
     */
    public Object scriptPubkey(boolean testnet, boolean fresh) throws Exception {
        Tx tx = fetch(testnet, fresh);
        return tx.getTxOuts().get(prevIndex.intValue()).scriptPubkey();
    }


}
