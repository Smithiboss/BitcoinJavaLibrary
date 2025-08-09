package org.example.Chapter5;

import org.example.Utils.Bytes;
import org.example.Utils.Hash;
import org.example.Utils.Helper;
import org.example.ecc.Int;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Tx {

    private final Int version;
    private final List<TxIn> txIns;
    private final List<TxOut> txOuts;
    private Int lockTime;
    private boolean testnet;

    public Tx(Int version, List<TxIn> txIns, List<TxOut> txOuts, Int lockTime, boolean testnet) {
        this.version = version;
        this.txIns = txIns;
        this.txOuts = txOuts;
        this.lockTime = lockTime;
        this.testnet = testnet;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder txInsStr = new StringBuilder();
        for (Object txIn : txIns) {
            txInsStr.append(txIn.toString()).append("\n");
        }

        StringBuilder txOutsStr = new StringBuilder();
        for (Object txOut : txOuts) {
            txOutsStr.append(txOut.toString()).append("\n");
        }

        return String.format("tx: %s\nversion: %s\ntx_ins:\n%stx_outs:\n%slocktime: %s",
                this.getId(),
                version,
                txInsStr,
                txOutsStr,
                lockTime
        );
    }

    /**
     * Takes a byte stream and parses the transaction at the start
     * @param s {@link ByteArrayInputStream}
     * @param testnet {@code boolean}
     * @return a {@link Tx} object
     * @throws IOException Stream exception
     */
    public static Tx parse(ByteArrayInputStream s, boolean testnet) throws IOException {
        // s.read(n) will return n bytes
        // version is an integer in 4 bytes, little-endian
        var version = Helper.littleEndianToInt(s.readNBytes(4));
        // inputNum is a varint, use readVarint(s)
        var inputNum = Helper.readVarint(s).longValue();
        // parse inputNum number of TxIns
        List<TxIn> inputs = new ArrayList<>();
        for (int i = 0; i < inputNum; i++) {
            inputs.add(TxIn.parse(s));
        }
        // outputNum is a varint, use readVarint(s)
        var outputNum = Helper.readVarint(s).longValue();
        // parse outputNum number of TxOuts
        List<TxOut> outputs = new ArrayList<>();
        for (int i = 0; i < outputNum; i++) {
            outputs.add(TxOut.parse(s));
        }
        // lockTime is an integer in 4 bytes, little-endian
        var lockTime = Helper.littleEndianToInt(s.readNBytes(4));
        return new Tx(version, inputs, outputs, lockTime, testnet);
    }

    /**
     * Returns the byte serialization of the transaction
     * @return a {@code byte} array
     */
    public byte[] serialize() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        result.writeBytes(version.toBytesLittleEndian(4));

        result.writeBytes(Helper.encodeVarInt(Int.parse(txIns.size())));

        for (TxIn txIn : txIns) {
            result.writeBytes(txIn.serialize());
        }

        result.writeBytes(Helper.encodeVarInt(Int.parse(txOuts.size())));

        for (TxOut txOut : txOuts) {
            result.writeBytes(txOut.serialize());
        }

        result.writeBytes(lockTime.toBytesLittleEndian(4));

        return result.toByteArray();
    }

    public BigInteger fee(boolean testnet, boolean fresh) throws IOException, InterruptedException {
        BigInteger inputSum = BigInteger.ZERO;
        BigInteger outputSum = BigInteger.ZERO;
        for (TxIn txIn : txIns) {
            inputSum.add(txIn.value(testnet, fresh));
        }
        for (TxOut txOut : txOuts) {
            outputSum.add(txOut.amount());
        }
        return inputSum.subtract(outputSum);
    }

    /**
     * Returns a human-readable hexadecimal of the transaction hash
     * @return a {@link String} object
     */
    public String getId() {
        return Bytes.byteArrayToHexString(hash());
    }

    /**
     * Returns a binary hash of the legacy serialization
     * @return a {@code byte} array
     */
    private byte[] hash() {
        return Bytes.reverseOrder(Hash.hash256(serialize()));
    }

    /**
     * Returns version
     * @return a {@link Int} object
     */
    public Int getVersion() {return version;}

    /**
     * Returns transaction inputs
     * @return a {@link List}
     */
    public List<TxIn> getTxIns() {return txIns;}

    /**
     * Returns transaction outputs
     * @return a {@link List}
     */
    public List<TxOut> getTxOuts() {return txOuts;}

    /**
     * Returns locktime
     * @return a {@link Int} object
     */
    public Int getLockTime() {return lockTime;}

    /**
     * Returns testnet value
     * @return a {@code boolean}
     */
    public boolean isTestnet() {return testnet;}

    /**
     * Sets locktime
     */
    public void setLockTime(Int lockTime) {this.lockTime = lockTime;}

    /**
     * Sets testnet
     */
    public void setTestnet(boolean testnet) {this.testnet = testnet;}
}
