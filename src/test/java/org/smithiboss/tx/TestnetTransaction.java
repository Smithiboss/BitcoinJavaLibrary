package org.smithiboss.tx;

import org.smithiboss.utils.Base58;
import org.smithiboss.utils.Bytes;
import org.smithiboss.utils.Hash;
import org.smithiboss.utils.Helper;
import org.smithiboss.ecc.Hex;
import org.smithiboss.ecc.Int;
import org.smithiboss.ecc.PrivateKey;
import org.smithiboss.script.Cmd;
import org.smithiboss.script.Op;
import org.smithiboss.script.Script;
import org.junit.Test;

import java.util.List;
import java.util.logging.Logger;

public class TestnetTransaction {

    private static final Logger log = Logger.getLogger(Op.class.getSimpleName());

    @Test
    public void createAddress() {
        var privateKey = new PrivateKey(Helper.littleEndianToInt("donttrustverify".getBytes()));
        var address = privateKey.getPublicKey().address(true, true);
        log.info(address);
        var privateKey2 = new PrivateKey(Helper.littleEndianToInt("notyourkeysnotyourcoins".getBytes()));
        var address2 = privateKey2.getPublicKey().address(true, true);
        log.info(address2);
    }

    @Test
    public void createTransaction() {
        var prevTx = Bytes.hexStringToByteArray("7b5bb0a5e89aa1424ae2a1c94ec56f0f4908dfb254d7046b44d59a3ecc68e701");
        var prevIndex = 0;
        var txIn = new TxIn(Hex.parse(prevTx), Int.parse(prevIndex), null,
                Helper.littleEndianToInt(Bytes.hexStringToByteArray("ffffffff")));
        var changeAmount = Int.parse(5000);
        var changeH160 = Base58.decodeAddress("mow7jpNuUp32sdPEszybtpHYJeiaMErAZk");
        var changeScript = Script.p2pkhScript(changeH160);
        var changeOutput = new TxOut(changeAmount, changeScript);
        var targetAmount = Int.parse(5000);
        var targetH160 = Base58.decodeAddress("myWZQE4VcprNzAd455kDP3EKPJnxHR5UFj");
        var targetScript = Script.p2pkhScript(targetH160);
        var targetOutput = new TxOut(targetAmount, targetScript);
        var txObject = new Tx(Int.parse(1), List.of(txIn), List.of(changeOutput, targetOutput), Int.parse(0), true, null);
        signTransaction(txObject);
    }

    public void signTransaction(Tx transaction) {
        var z = transaction.sigHash(0, null);
        var privateKey = new PrivateKey(Helper.littleEndianToInt("BeppoIstEinGoofy".getBytes()));
        var der = privateKey.sign(z).der();
        var sig = Bytes.concat(der, Hash.SIGHASH_ALL.toBytes(1));
        var sec = privateKey.getPublicKey().sec(true);
        var scriptSig = new Script(List.of(new Cmd(sec), new Cmd(sig)));
        transaction.getTxIns().getFirst().setScriptSig(scriptSig);


    }

}
