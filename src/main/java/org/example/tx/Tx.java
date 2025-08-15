package org.example.tx;

import org.example.Utils.Bytes;
import org.example.Utils.Hash;
import org.example.Utils.Helper;
import org.example.ecc.Hex;
import org.example.ecc.Int;
import org.example.ecc.PrivateKey;
import org.example.script.Cmd;
import org.example.script.Script;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
        for (TxIn txIn : txIns) {
            txInsStr.append(txIn.toString()).append("\n");
        }

        StringBuilder txOutsStr = new StringBuilder();
        for (TxOut txOut : txOuts) {
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
     * Takes a raw string and parses the transaction at the start
     * @param raw a {@link String} object
     * @param testnet a {@code boolean}
     * @return a {@link Tx} object
     */
    public static Tx parseLegacy(String raw, boolean testnet) {
        return parseLegacy(Bytes.hexStringToByteArray(raw), testnet);
    }

    /**
     * Takes a byte array and parses the transaction at the start
     * @param bytes a {@code byte} array
     * @param testnet a {@code boolean}
     * @return a {@link Tx} object
     */
    public static Tx parseLegacy(byte[] bytes, boolean testnet) {
        return parseLegacy(new ByteArrayInputStream(bytes), testnet);
    }

    /**
     * Takes a byte stream and parses the transaction at the start
     * @param s a {@link ByteArrayInputStream}
     * @param testnet a {@code boolean}
     * @return a {@link Tx} object
     */
    public static Tx parseLegacy(ByteArrayInputStream s, boolean testnet) {
        // s.read(n) will return n bytes
        // version is an integer in 4 bytes, little-endian
        var version = Helper.littleEndianToInt(Bytes.read(s, 4));
        // inputNum is a varint, use readVarint(s)
        var inputNum = Helper.readVarint(s).intValue();
        System.out.println("inputNum: " + inputNum);
        // parseLegacy inputNum number of TxIns
        List<TxIn> inputs = new ArrayList<>();
        for (int i = 0; i < inputNum; i++) {
            inputs.add(TxIn.parse(s));
        }
        // outputNum is a varint, use readVarint(s)
        var outputNum = Helper.readVarint(s).intValue();
        System.out.println("outputNum: " + outputNum);
        // parseLegacy outputNum number of TxOuts
        List<TxOut> outputs = new ArrayList<>();
        for (int i = 0; i < outputNum; i++) {
            outputs.add(TxOut.parse(s));
        }
        // lockTime is an integer in 4 bytes, little-endian
        var lockTime = Hex.parse(Bytes.reverseOrder(Bytes.read(s, 4)));
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
     * @return a {@link Int} object
     */
    public Int sigHash(int inputIndex) {
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
                scriptSig = txIn.scriptPubkey(testnet);
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
     * Validates the signature of the input at inputIndex
     * @param inputIndex a {@code int}
     * @return a {@code boolean}
     */
    public boolean verifyInput(int inputIndex) {
        var txIn = txIns.get(inputIndex);
        // get the scriptPubkey of previous output
        var scriptPubKey = txIn.scriptPubkey(testnet);
        // calculate z
        var z = sigHash(inputIndex);
        // combine Script Signature and Script PubKey
        var combined = txIn.getScriptSig().add(scriptPubKey);
        // evaluate the combined script
        return combined.evaluate(z);
    }

    /**
     * Verify this transaction
     * @return a {@code boolean}
     */
    public boolean verify() {
        // check that the transaction is not creating coins
        if (fee(this.testnet).lt(Int.parse(0))) {
            return false;
        }
        // check that every input has a valid scriptSig
        for (int i = 0; i < txIns.size(); i++) {
            if (!this.verifyInput(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Signs input with the provided private key
     * @param inputIndex a {@code int}
     * @param privateKey a {@link PrivateKey} object
     * @return a {@code boolean}
     */
    public boolean signInput(int inputIndex, PrivateKey privateKey) {
        var z = sigHash(inputIndex);
        // create signature of z and serialize with DER
        var der = privateKey.sign(z).der();
        // append SIGHASH_ALL to der
        var sig = Bytes.concat(der, Hash.SIGHASH_ALL.toBytes(1));
        // get sec
        var sec = privateKey.getPublicKey().sec(true);
        // create new script with [sig, sec] as cmds
        var script = new Script(List.of(new Cmd(sig), new Cmd(sec)));
        // set inputs scriptSig to script
        this.txIns.get(inputIndex).setScriptSig(script);
        // validate signature
        return verifyInput(inputIndex);
    }

    /**
     * Calculates the fee
     * @param testnet a {@code boolean}
     * @return a {@link Int} object
     */
    public Int fee(boolean testnet) {
        var inputSum = Int.parse(0);
        var outputSum = Int.parse(0);
        for (TxIn txIn : txIns) {
            inputSum.add(txIn.value(testnet));
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
