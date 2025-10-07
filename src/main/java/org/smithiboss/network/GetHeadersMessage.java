package org.smithiboss.network;

import org.smithiboss.ecc.Int;
import org.smithiboss.utils.Bytes;
import org.smithiboss.utils.Helper;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

public class GetHeadersMessage {

    public static final String COMMAND = "getheaders";

    private final Int version;
    private final Int numHashes;
    private final byte[] startBlock;
    private final byte[] endBlock;

    public GetHeadersMessage(Int version, Int numHashes, byte[] startBlock, byte[] endBlock) {
        this.version = Objects.requireNonNullElse(version, Int.parse(70015));
        this.numHashes = Objects.requireNonNullElse(numHashes, Int.parse(1));
        this.startBlock = Objects.requireNonNullElseGet(startBlock, () -> {
            throw new IllegalArgumentException("A start block is required");
        });
        this.endBlock = Objects.requireNonNullElseGet(endBlock, () -> Bytes.initFill(32, (byte) 0x00));
    }

    /**
     * Serialize
     * @return a {@code byte} array
     */
    public byte[] serialize() {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        result.writeBytes(version.toBytesLittleEndian(4));

        result.writeBytes(Helper.encodeVarInt(numHashes));

        result.writeBytes(Bytes.reverseOrder(startBlock));

        result.writeBytes(Bytes.reverseOrder(endBlock));
        return result.toByteArray();
    }

}
