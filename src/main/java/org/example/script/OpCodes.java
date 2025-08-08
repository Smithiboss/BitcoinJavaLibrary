package org.example.script;

import org.example.ecc.Hex;
import org.example.ecc.Int;

public enum OpCodes {

    /** op 0 */
    OP_0_0(Hex.parse("00"), "OP_0", "op0"),
    /** op 81 */
    OP_81_1(Hex.parse("51"), "OP_1", "op1"),
    /** op 82 */
    OP_82_2(Hex.parse("52"), "OP_2", "op2"),
    /** op 86 */
    OP_86_6(Hex.parse("56"), "OP_6", "op6"),
    /** op 91 */
    OP_91_11(Hex.parse("5b"), "OP_11", "op11"),
    /** op 99 */
    OP_99_IF(Hex.parse("63"), "OP_IF", "opIf"),
    /** op 100 */
    OP_100_NOTIF(Hex.parse("64"), "OP_NOTIF", "opNotIf"),
    /** op 103 */
    OP_103_ELSE(Hex.parse("67"), "OP_ELSE", "opElse"),
    /** op 105 */
    OP_105_VERIFY(Hex.parse("69"), "OP_VERIFY", "opVerify"),
    /** op 106 */
    OP_106_RETURN(Hex.parse("6a"), "OP_RETURN", "opReturn"),
    /** op 107 */
    OP_107_TOALTSTACK(Hex.parse("6b"), "OP_TOALTSTACK", "opToAltStack"),
    /** op 108 */
    OP_108_FROMALTSTACK(Hex.parse("6c"), "OP_FROMALTSTACK", "opFromAltStack"),
    /** op 109 */
    OP_109_2DROP(Hex.parse("6d"), "OP_2DROP", "op2Drop"),
    /** op 110 */
    OP_110_2DUP(Hex.parse("6e"), "OP_2DUP", "op2Dup"),
    /** op 118 */
    OP_118_DUP(Hex.parse("76"), "OP_DUP", "opDup"),
    /** op 124 */
    OP_124_SWAP(Hex.parse("7c"), "OP_SWAP", "opSwap"),
    /** op 135 */
    OP_135_EQUAL(Hex.parse("87"), "OP_EQUAL", "opEqual"),
    /** op 136 */
    OP_136_EQUALVERIFY(Hex.parse("88"), "OP_EQUALVERIFY", "opEqualVerify"),
    /** op 142 */
    OP_142_8E(Hex.parse("8e"), "OP_8E", "op8E"),
    /** op 145 */
    OP_145_NOT(Hex.parse("91"), "OP_NOT", "opNot"),
    /** op 147 */
    OP_147_ADD(Hex.parse("93"), "OP_ADD", "opAdd"),
    /** op 167 */
    OP_167_SHA1(Hex.parse("a7"), "OP_SHA1", "opSha1"),
    /** op 169 */
    OP_169_HASH160(Hex.parse("a9"), "OP_HASH160", "opHash160"),
    /** op 170 */
    OP_170_HASH256(Hex.parse("aa"), "OP_HASH256", "opHash256"),
    /** op 172 */
    OP_172_CHECKSIG(Hex.parse("ac"), "OP_CHECKSIG", "opCheckSig"),
    /** op 173 */
    OP_173_CHECKSIGVERIFY(Hex.parse("ad"), "OP_CHECKSIGVERIFY", "opCheckSigVerify"),
    /** op 174 */
    OP_174_CHECKMULTISIG(Hex.parse("ae"), "OP_CHECKMULTISIG", "opCheckMultiSig"),
    /** op 175 */
    OP_175_CHECKMULTISIGVERIFY(Hex.parse("af"), "OP_CHECKMULTISIGVERIFY", "opCheckMultiSigVerify"),
    /** op 184 */
    OP_184_B8(Hex.parse("b8"), "OP_B8", "opB8"),
    /** op 190 */
    OP_190_BE(Hex.parse("be"), "OP_BE", "opBe"),
    /** op 232 */
    OP_232_E8(Hex.parse("e8"), "OP_E8", "opE8"),
    /** op 250 */
    OP_250_FA(Hex.parse("fa"), "OP_FA", "opFa"),
    /** op 254 */
    OP_254_FE(Hex.parse("fe"), "OP_FE", "opFe"),;

    private final Int code;

    private final String codeName;

    private final String codeFunc;

    OpCodes(Int code, String codeName, String codeFunc) {
        this.code = code;
        this.codeName = codeName;
        this.codeFunc = codeFunc;
    }

    public static OpCodes findByCode(Int code) {
        for (OpCodes op : values()) {
            if (op.code.equals(code)) {
                return op;
            }
        }
        throw new IllegalArgumentException("No OpCodes found for " + code);
    }

    public Cmd toCmd() {
        return new Cmd(this);
    }

    public Int getCode() {
        return code;
    }

    public String getCodeName() {
        return codeName;
    }

    public String getCodeFunc() {
        return codeFunc;
    }
}
