package org.example.tx;

import org.example.Utils.Bytes;
import org.example.Utils.Hash;
import org.example.Utils.Helper;
import org.example.ecc.Hex;
import org.example.ecc.Int;
import org.example.script.Script;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
     * @param s a {@link ByteArrayInputStream}
     * @param testnet a {@code boolean}
     * @return a {@link Tx} object
     * @throws IOException Stream exception
     */
    public static Tx parseLegacy(ByteArrayInputStream s, boolean testnet) throws IOException {
        // s.read(n) will return n bytes
        // version is an integer in 4 bytes, little-endian
        var version = Helper.littleEndianToInt(s.readNBytes(4));
        // inputNum is a varint, use readVarint(s)
        var inputNum = Helper.readVarint(s).longValue();
        // parseLegacy inputNum number of TxIns
        List<TxIn> inputs = new ArrayList<>();
        for (int i = 0; i < inputNum; i++) {
            inputs.add(TxIn.parse(s));
        }
        // outputNum is a varint, use readVarint(s)
        var outputNum = Helper.readVarint(s).longValue();
        // parseLegacy outputNum number of TxOuts
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
    public byte[] serializeLegacy() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        // serializeLegacy version
        result.writeBytes(version.toBytesLittleEndian(4));
        // encode varint on the number of inputs
        result.writeBytes(Helper.encodeVarInt(Int.parse(txIns.size())));
        // iterate over inputs
        for (TxIn txIn : txIns) {
            // serializeLegacy each input
            result.writeBytes(txIn.serialize());
        }
        // encode varint on the number of outputs
        result.writeBytes(Helper.encodeVarInt(Int.parse(txOuts.size())));
        // iterate over outputs
        for (TxOut txOut : txOuts) {
            // serializeLegacy each output
            result.writeBytes(txOut.serialize());
        }
        // serializeLegacy locktime
        result.writeBytes(lockTime.toBytesLittleEndian(4));

        return result.toByteArray();
    }

    /**
     * Returns the hash that needs to be signed for given index as an integer
     * @param inputIndex a {@code int}
     * @param fresh a {@code boolean}
     * @return a {@link Int} object
     */
    public Int sigHash(int inputIndex, boolean fresh) {
        var stream = new ByteArrayOutputStream();
        // serialize version
        stream.writeBytes(version.toBytesLittleEndian(4));
        // encode the number of inputs as a varint
        stream.writeBytes(Helper.encodeVarInt(Int.parse(txIns.size())));
        // iterate over all txInputs
        for (int i = 0; i < txIns.size(); i++) {
            var txIn = txIns.get(i);
            Script scriptSig;
            // check if input index is reached
            if (i == inputIndex) {
                // copy scriptPubKey from output of previous transaction
                scriptSig = txIn.scriptPubkey(testnet, fresh);
            } else {
                // remove scriptSig
                scriptSig = null;
            }
            // add the serialization of the input with correct scriptSig
            stream.writeBytes(new TxIn(txIn.getPrevTx(), txIn.getPrevIndex(), scriptSig, txIn.getSequence()).serialize());
        }
        // encode the number of outputs as a varint
        stream.writeBytes(Helper.encodeVarInt(Int.parse(txOuts.size())));
        // add the serialization of every output
        for (TxOut txOut : txOuts) {
            stream.writeBytes(txOut.serialize());
        }
        // serialize locktime
        stream.writeBytes(lockTime.toBytesLittleEndian(4));
        // add SIGHASH_ALL
        stream.writeBytes(Hash.SIGHASH_ALL.toBytesLittleEndian(4));
        // hash256 the serialized transaction
        return Hex.parse(Hash.hash256(stream.toByteArray()));
    }

    /**
     * Calculates the fee
     * @param testnet a {@code boolean}
     * @param fresh a {@code boolean}
     * @return a {@link Int} object
     */
    public Int fee(boolean testnet, boolean fresh) {
        var inputSum = Int.parse(0);
        var outputSum = Int.parse(0);
        for (TxIn txIn : txIns) {
            inputSum.add(txIn.value(testnet, fresh));
        }
        for (TxOut txOut : txOuts) {
            outputSum.add(txOut.amount());
        }
        return inputSum.sub(outputSum);
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
        return Bytes.reverseOrder(Hash.hash256(serializeLegacy()));
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
