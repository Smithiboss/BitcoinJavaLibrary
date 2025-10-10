package org.smithiboss.tx;

import org.smithiboss.script.OpCodes;
import org.smithiboss.utils.Bytes;
import org.smithiboss.utils.Hash;
import org.smithiboss.utils.Helper;
import org.smithiboss.ecc.Hex;
import org.smithiboss.ecc.Int;
import org.smithiboss.ecc.PrivateKey;
import org.smithiboss.script.Cmd;
import org.smithiboss.script.Script;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Tx {

    private final Int version;
    private final List<TxIn> txIns;
    private final List<TxOut> txOuts;
    private Int lockTime;
    private Boolean testnet;
    private Boolean segwit;
    private byte[] _hashPrevouts = null;
    private byte[] _hashSequence = null;
    private byte[] _hashOutputs = null;

    public Tx(Int version, List<TxIn> txIns, List<TxOut> txOuts, Int lockTime, Boolean testnet, Boolean segwit) {
        this.version = version;
        this.txIns = txIns;
        this.txOuts = txOuts;
        this.lockTime = lockTime;
        this.testnet = Objects.requireNonNullElse(testnet, false);
        this.segwit = Objects.requireNonNullElse(segwit, false);
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
     * Parses a transaction from a byte stream and determines if it's a legacy or segwit transaction.
     *
     * @param raw a {@link String} representing the hex string of the transaction
     * @param testnet a {@code Boolean} indicating whether the transaction is for the testnet
     * @return a {@code Tx} object representing the parsed transaction
     */
    public static Tx parse(String raw, Boolean testnet) {
        return parse(Bytes.hexStringToByteArray(raw), testnet);
    }

    /**
     * Parses a transaction from a byte stream and determines if it's a legacy or segwit transaction.
     *
     * @param bytes a {@code byte} array representing the input bytes of the transaction
     * @param testnet a {@code Boolean} indicating whether the transaction is for the testnet
     * @return a {@code Tx} object representing the parsed transaction
     */
    public static Tx parse(byte[] bytes, Boolean testnet) {
        return parse(new ByteArrayInputStream(bytes), testnet);
    }

    /**
     * Parses a transaction from a byte stream and determines if it's a legacy or segwit transaction.
     *
     * @param s a {@code ByteArrayInputStream} representing the input byte stream of the transaction
     * @param testnet a {@code Boolean} indicating whether the transaction is for the testnet
     * @return a {@code Tx} object representing the parsed transaction
     */
    public static Tx parse(ByteArrayInputStream s, Boolean testnet) {
        Bytes.read(s, 4);
        var marker = Hex.parse(Bytes.read(s, 1));
        s.reset();
        if (marker.eq(Hex.parse("00"))) {
            return parseSegwit(s, testnet);
        } else {
            return parseLegacy(s, testnet);
        }
    }

    /**
     * Parses a legacy Bitcoin transaction from the given input stream.
     *
     * @param s a {@code ByteArrayInputStream} representing the input byte stream of the transaction
     * @param testnet a {@code Boolean} indicating whether the transaction belongs to the testnet
     * @return a {@code Tx} object representing the parsed legacy transaction
     */
    private static Tx parseLegacy(ByteArrayInputStream s, Boolean testnet) {
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
        return new Tx(version, inputs, outputs, lockTime, testnet, false);
    }

    /**
     * Parses a SegWit Bitcoin transaction from the given input stream.
     *
     * @param s a {@code ByteArrayInputStream} representing the input byte stream of the transaction
     * @param testnet a {@code Boolean} indicating whether the transaction belongs to the testnet
     * @return a {@code Tx} object representing the parsed SegWit transaction
     * @throws IllegalStateException if the provided transaction is not a valid SegWit transaction
     */
    private static Tx parseSegwit(ByteArrayInputStream s, Boolean testnet) {
        var version = Helper.littleEndianToInt(Bytes.read(s, 4));
        var markerFlag = Hex.parse(Bytes.read(s, 2));
        if (markerFlag.ne(Hex.parse("0001"))) {
            throw new IllegalStateException("Not a segwit transaction");
        }
        var inputNum = Helper.readVarint(s).intValue();
        List<TxIn> inputs = new ArrayList<>();
        for (int i = 0; i < inputNum; i++) {
            inputs.add(TxIn.parse(s));
        }
        var outputNum = Helper.readVarint(s).intValue();
        List<TxOut> outputs = new ArrayList<>();
        for (int i = 0; i < outputNum; i++) {
            outputs.add(TxOut.parse(s));
        }
        for (TxIn txIn : inputs) {
            var itemNum = Helper.readVarint(s).intValue();
            List<Cmd> items = new ArrayList<>();
            for (int i = 0; i < itemNum; i++) {
                var itemLen = Helper.readVarint(s).intValue();
                if (itemLen == 0) {
                    items.add(OpCodes.OP_0_0.toCmd());
                } else {
                    items.add(new Cmd(Bytes.read(s, itemLen)));
                }
            }
            txIn.setWitness(new Script(items));
        }
        var lockTime = Hex.parse(Bytes.reverseOrder(Bytes.read(s, 4)));
        return new Tx(version, inputs, outputs, lockTime, testnet, true);
    }

    /**
     * Returns the byte serialization of the transaction
     * @return a {@code byte} array
     */
    public byte[] serialize() {
        if (segwit) {
            return serializeSegwit();
        } else {
            return serializeLegacy();
        }
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
     * Returns the byte serialization of the transaction in segwit format.
     * @return a {@code byte} array
     */
    public byte[] serializeSegwit() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        result.writeBytes(version.toBytesLittleEndian(4));
        result.writeBytes(new byte[]{0x00, 0x01});
        result.writeBytes(Helper.encodeVarInt(Int.parse(txIns.size())));
        for (TxIn txIn : txIns) {
            result.writeBytes(txIn.serialize());
        }
        result.writeBytes(Helper.encodeVarInt(Int.parse(txOuts.size())));
        for (TxOut txOut : txOuts) {
            result.writeBytes(txOut.serialize());
        }
        for (TxIn txIn : txIns) {
            result.writeBytes(Int.parse(txIn.getWitness().getCmds().size()).toBytesLittleEndian(1));
            for (Cmd item : txIn.getWitness().getCmds()) {
                if (item.isOpCode()) {
                    result.writeBytes(item.getOpCode().getCode().toBytesLittleEndian(1));
                } else {
                    result.writeBytes(Helper.encodeVarInt(Int.parse(item.getElement().length)));
                    result.writeBytes(item.getElement());
                }
            }
        }
        result.writeBytes(lockTime.toBytesLittleEndian(4));
        return result.toByteArray();
    }

    /**
     * Returns the hash that needs to be signed for given index as an integer
     * @param inputIndex a {@code int}
     * @return a {@link Int} object
     */
    public Int sigHash(int inputIndex, Script redeemScript) {
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
                if (redeemScript != null) {
                    // p2sh, replace scriptSig with the redeem script
                    scriptSig = redeemScript;
                } else {
                    scriptSig = txIn.scriptPubkey(testnet);
                }
            } else {
                // remove scriptSig
                scriptSig = null;
            }
            // add the serialization of the input with the correct scriptSig
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
     * Computes and returns the hash of all the previous transaction outputs (Prevouts)
     * used as inputs in the current transaction. The hash is calculated using the
     * double SHA-256 (hash256) function on the concatenated data of previous transaction hashes
     * and their respective indices in little-endian format.
     * <p>
     * If the hash is already computed (cached), it returns the cached value directly.
     * Otherwise, it processes all the transaction inputs to compute the hash,
     * caches it, and then returns the result.
     *
     * @return a {@code byte} array containing the double SHA-256 hash of all previous inputs.
     */
    private byte[] hashPrevouts() {
        if (_hashPrevouts == null) {
            var allPrevouts = new byte[0];
            var allSequence = new byte[0];
            for (TxIn txIn : txIns) {
                allPrevouts = Bytes.concat(allPrevouts, Bytes.reverseOrder(txIn.getPrevTx().toBytes()), txIn.getPrevIndex().toBytesLittleEndian(4));
                allSequence = Bytes.concat(allSequence, txIn.getSequence().toBytesLittleEndian(4));
            }
            _hashPrevouts = Hash.hash256(allPrevouts);
            _hashSequence = Hash.hash256(allSequence);
        }
        return _hashPrevouts;
    }

    private byte[] hashSequence() {
        if (_hashSequence == null) {
            hashPrevouts();
        }
        return _hashSequence;
    }

    private byte[] hashOutputs() {
        if (_hashOutputs == null) {
            var allOutputs = new byte[0];
            for (TxOut txOut : txOuts) {
                allOutputs = Bytes.concat(allOutputs, txOut.serialize());
            }
            _hashOutputs = Hash.hash256(allOutputs);
        }
        return _hashOutputs;
    }

    /**
     * Computes the SigHash (signature hash) for a given input index using the BIP-143 hashing scheme.
     * This method handles scenarios involving SegWit, redeem scripts, or witness scripts.
     *
     * @param inputIndex the index of the input for which the SigHash is being calculated
     * @param redeemScript the redeem script used in the transaction, can be null if not applicable
     * @param witnessScript the witness script used in the transaction, can be null if not applicable
     * @return a {@link Int} object representing the computed SigHash as an integer
     */
    public Int sigHashBip143(int inputIndex, Script redeemScript, Script witnessScript) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        var txIn = txIns.get(inputIndex);

        result.writeBytes(version.toBytesLittleEndian(4));
        result.writeBytes(hashPrevouts());
        result.writeBytes(hashSequence());
        result.writeBytes(txIn.getPrevTx().toBytesLittleEndian());
        result.writeBytes(txIn.getPrevIndex().toBytesLittleEndian(4));
        byte[] scriptCode;
        if (witnessScript != null) {
            scriptCode = witnessScript.serialize();
        } else if (redeemScript != null) {
            scriptCode = Script.p2pkhScript(redeemScript.getCmds().getLast().getElement()).serialize();
        } else {
            scriptCode = Script.p2pkhScript(txIn.scriptPubkey(testnet).getCmds().get(1).getElement()).serialize();
        }
        result.writeBytes(scriptCode);
        result.writeBytes(txIn.value(testnet).toBytesLittleEndian(8));
        result.writeBytes(txIn.getSequence().toBytesLittleEndian(4));
        result.writeBytes(hashOutputs());
        result.writeBytes(lockTime.toBytesLittleEndian(4));
        result.writeBytes(Hash.SIGHASH_ALL.toBytesLittleEndian(4));
        return Hex.parse(Hash.hash256(result.toByteArray()));
    }

    /**
     * Validates the signature of the input at inputIndex
     * @param inputIndex a {@code int}
     * @return a {@code boolean}
     */
    public boolean verifyInput(int inputIndex) {
        var txIn = txIns.get(inputIndex);
        // get the scriptPubkey of previous output
        var scriptPubKey = txIn.scriptPubkey(this.testnet);
        Script redeemScript = null;
        Int z = null;
        Script witness = null;
        // check if ScriptPubKey is p2sh
        if (scriptPubKey.isP2shScriptPubkey()) {
            // get redeem script
            var cmd = txIn.getScriptSig().getCmds().getLast();
            // add varint
            var rawRedeem = Bytes.concat(Helper.encodeVarInt(Int.parse(cmd.getElement().length)), cmd.getElement());
            // parse redeem script
            redeemScript = Script.parse(new ByteArrayInputStream(rawRedeem));

            if (redeemScript.isP2wpkhScriptPubkey()) {
                z = sigHashBip143(inputIndex, redeemScript, null);
                witness = txIn.getWitness();
            } else if (redeemScript.isP2wshScriptPubkey()) {
                cmd = txIn.getWitness().getCmds().getLast();
                var rawWitness = Bytes.concat(Helper.encodeVarInt(Int.parse(cmd.getElement().length)), cmd.getElement());
                var witnessScript = Script.parse(new ByteArrayInputStream(rawWitness));
                z = sigHashBip143(inputIndex, null, witnessScript);
                witness = txIn.getWitness();
            } else {
                z = sigHash(inputIndex, redeemScript);
            }
        } else {
            if (scriptPubKey.isP2wpkhScriptPubkey()) {
                z = sigHashBip143(inputIndex, null, null);
                witness = txIn.getWitness();
            } else if (scriptPubKey.isP2wshScriptPubkey()) {
                var cmd = txIn.getWitness().getCmds().getLast();
                var rawWitness = Bytes.concat(Helper.encodeVarInt(Int.parse(cmd.getElement().length)), cmd.getElement());
                var witnessScript = Script.parse(new ByteArrayInputStream(rawWitness));
                z = sigHashBip143(inputIndex, null, witnessScript);
                witness = txIn.getWitness();
            } else {
                z = sigHash(inputIndex, null);
            }
        }
        // combine Script Signature and Script PubKey
        var combined = txIn.getScriptSig().add(scriptPubKey);
        // evaluate the combined script
        return combined.evaluate(z, witness);
    }

    /**
     * Verify this transaction
     * @return a {@code boolean}
     */
    public boolean verify() {
        // check that the transaction is not creating coins
        if (fee().lt(Int.parse(0))) {
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
        var z = sigHash(inputIndex, null);
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
     * @return a {@link Int} object
     */
    public Int fee() {
        var inputSum = Int.parse(0);
        var outputSum = Int.parse(0);
        for (TxIn txIn : txIns) {
            inputSum = inputSum.add(txIn.value(this.testnet));
        }
        for (TxOut txOut : txOuts) {
            outputSum = outputSum.add(txOut.amount());
        }
        return inputSum.sub(outputSum);
    }

    /**
     * Returns whether this transaction is a coinbase transaction
     * @return a {@code boolean}
     */
    public boolean isCoinBase() {
        // transaction can only have one input
        if (txIns.size() != 1) return false;
        var input = txIns.getFirst();
        // the input must have a previous transaction of 32 zero bytes
        if (input.getPrevTx().ne(Hex.parse("0000000000000000000000000000000000000000000000000000000000000000")))
            return false;
        // the input must have a previous index of 0xffffffff
        return !input.getPrevIndex().ne(Hex.parse("ffffffff"));
    }

    /**
     * Returns the height of the block / the height of this coinbase transaction
     * @return a {@link Int} object
     */
    public Int coinbaseHeight() {
        // if the transaction is not a coinbase transaction, it has no coinbase height
        if (!this.isCoinBase()) return null;
        // get the first element of the coinbase ScriptSig as it is the height of the block
        var element = txIns.getFirst().getScriptSig().getCmds().getFirst();
        // the height is interpreted as little endian
        return Hex.parse(Bytes.reverseOrder(element.getElement()));
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
