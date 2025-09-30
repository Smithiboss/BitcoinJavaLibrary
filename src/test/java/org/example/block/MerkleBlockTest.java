package org.example.block;

import org.example.ecc.Hex;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

public class MerkleBlockTest {

    @Test
    public void testParse() {
        var MerkleBlockHex = Hex.parse("00000020df3b053dc46f162a9b00c7f0d5124e2676d47bbe7c5d0793a500000000000000ef445fef2ed495c275892206ca533e7411907971013ab83e3b47bd0d692d14d4dc7c835b67d8001ac157e670bf0d00000aba412a0d1480e370173072c9562becffe87aa661c1e4a6dbc305d38ec5dc088a7cf92e6458aca7b32edae818f9c2c98c37e06bf72ae0ce80649a38655ee1e27d34d9421d940b16732f24b94023e9d572a7f9ab8023434a4feb532d2adfc8c2c2158785d1bd04eb99df2e86c54bc13e139862897217400def5d72c280222c4cbaee7261831e1550dbb8fa82853e9fe506fc5fda3f7b919d8fe74b6282f92763cef8e625f977af7c8619c32a369b832bc2d051ecd9c73c51e76370ceabd4f25097c256597fa898d404ed53425de608ac6bfe426f6e2bb457f1c554866eb69dcb8d6bf6f880e9a59b3cd053e6c7060eeacaacf4dac6697dac20e4bd3f38a2ea2543d1ab7953e3430790a9f81e1c67f5b58c825acf46bd02848384eebe9af917274cdfbb1a28a5d58a23a17977def0de10d644258d9c54f886d47d293a411cb6226103b55635");
        var mb = MerkleBlock.parse(new ByteArrayInputStream(MerkleBlockHex.toBytes()));
        var version = Hex.parse("20000000");
        assertEquals(version, mb.getVersion());
        var merkleRootHex = Hex.parse("ef445fef2ed495c275892206ca533e7411907971013ab83e3b47bd0d692d14d4");
        var merkleRoot = merkleRootHex.toBytesLittleEndian();
        assertArrayEquals(merkleRoot, mb.getMerkleRoot());
        var prevBlockHex = Hex.parse("df3b053dc46f162a9b00c7f0d5124e2676d47bbe7c5d0793a500000000000000");
        var prevBlock = prevBlockHex.toBytesLittleEndian();
        assertArrayEquals(mb.getPrevBlock(), prevBlock);
        var timestamp = Hex.parse(Hex.parse("dc7c835b").toBytesLittleEndian());
        assertEquals(mb.getTimestamp(), timestamp);
        var bits = Hex.parse("67d8001a").toBytes();
        assertArrayEquals(mb.getBits(), bits);
        var nonce = Hex.parse("c157e670").toBytes();
        assertArrayEquals(mb.getNonce(), nonce);
        var total = Hex.parse(Hex.parse("bf0d0000").toBytesLittleEndian()).intValue();
        assertEquals(mb.getTotal(), total);
        var hexHashes = new ArrayList<byte[]>();
        hexHashes.add(Hex.parse("ba412a0d1480e370173072c9562becffe87aa661c1e4a6dbc305d38ec5dc088a").toBytesLittleEndian());
        hexHashes.add(Hex.parse("7cf92e6458aca7b32edae818f9c2c98c37e06bf72ae0ce80649a38655ee1e27d").toBytesLittleEndian());
        hexHashes.add(Hex.parse("34d9421d940b16732f24b94023e9d572a7f9ab8023434a4feb532d2adfc8c2c2").toBytesLittleEndian());
        hexHashes.add(Hex.parse("158785d1bd04eb99df2e86c54bc13e139862897217400def5d72c280222c4cba").toBytesLittleEndian());
        hexHashes.add(Hex.parse("ee7261831e1550dbb8fa82853e9fe506fc5fda3f7b919d8fe74b6282f92763ce").toBytesLittleEndian());
        hexHashes.add(Hex.parse("f8e625f977af7c8619c32a369b832bc2d051ecd9c73c51e76370ceabd4f25097").toBytesLittleEndian());
        hexHashes.add(Hex.parse("c256597fa898d404ed53425de608ac6bfe426f6e2bb457f1c554866eb69dcb8d").toBytesLittleEndian());
        hexHashes.add(Hex.parse("6bf6f880e9a59b3cd053e6c7060eeacaacf4dac6697dac20e4bd3f38a2ea2543").toBytesLittleEndian());
        hexHashes.add(Hex.parse("d1ab7953e3430790a9f81e1c67f5b58c825acf46bd02848384eebe9af917274c").toBytesLittleEndian());
        hexHashes.add(Hex.parse("dfbb1a28a5d58a23a17977def0de10d644258d9c54f886d47d293a411cb62261").toBytesLittleEndian());
        assertArrayEquals(mb.getHashes().toArray(), hexHashes.toArray());
        var flags = Hex.parse("b55635").toBytes();
        assertArrayEquals(mb.getFlags(), flags);
    }

    @Test
    public void testIsValid() {
        var MerkleBlockHex = Hex.parse("00000020df3b053dc46f162a9b00c7f0d5124e2676d47bbe7c5d0793a500000000000000ef445fef2ed495c275892206ca533e7411907971013ab83e3b47bd0d692d14d4dc7c835b67d8001ac157e670bf0d00000aba412a0d1480e370173072c9562becffe87aa661c1e4a6dbc305d38ec5dc088a7cf92e6458aca7b32edae818f9c2c98c37e06bf72ae0ce80649a38655ee1e27d34d9421d940b16732f24b94023e9d572a7f9ab8023434a4feb532d2adfc8c2c2158785d1bd04eb99df2e86c54bc13e139862897217400def5d72c280222c4cbaee7261831e1550dbb8fa82853e9fe506fc5fda3f7b919d8fe74b6282f92763cef8e625f977af7c8619c32a369b832bc2d051ecd9c73c51e76370ceabd4f25097c256597fa898d404ed53425de608ac6bfe426f6e2bb457f1c554866eb69dcb8d6bf6f880e9a59b3cd053e6c7060eeacaacf4dac6697dac20e4bd3f38a2ea2543d1ab7953e3430790a9f81e1c67f5b58c825acf46bd02848384eebe9af917274cdfbb1a28a5d58a23a17977def0de10d644258d9c54f886d47d293a411cb6226103b55635");
        var mb = MerkleBlock.parse(new ByteArrayInputStream(MerkleBlockHex.toBytes()));
        assertTrue(mb.isValid());
    }

}
