package org.smithiboss.network;

import org.smithiboss.block.Block;
import org.smithiboss.utils.Bytes;
import org.smithiboss.utils.Helper;

import java.io.ByteArrayInputStream;

public class HeadersMessage {

    private static final String COMMAND = "headers";

    private final Block[] blocks;


    public HeadersMessage(Block[] blocks) {
        this.blocks = blocks;
    }

    public static HeadersMessage parse(String raw) {
        return parse(Bytes.hexStringToByteArray(raw));
    }

    public static HeadersMessage parse(byte[] bytes) {
        return parse(new ByteArrayInputStream(bytes));
    }

    /**
     * Parses a {@link ByteArrayInputStream} and constructs a {@link HeadersMessage} instance.
     * The method reads the number of block headers and parses each block header from the stream.
     * It also verifies that the number of transactions for each block is 0, throwing an exception otherwise.
     *
     * @param s the {@link ByteArrayInputStream} containing the serialized headers data
     * @return a {@link HeadersMessage} instance representing the parsed block headers
     * @throws IllegalStateException if the number of transactions in a block header is not 0
     */
    public static HeadersMessage parse(ByteArrayInputStream s) {
        var numHeaders = Helper.readVarint(s);
        // create an array based on the number of headers read from the stream
        var blocks = new Block[numHeaders.intValue()];
        // iterate over the number of headers read and parse each block header from the stream
        for (int i = 0; i < numHeaders.intValue(); i++) {
            blocks[i] = Block.parse(s);
            // the number of transactions in each block header should be 0, throw an exception otherwise
            var numTxs = Helper.readVarint(s);
            if (numTxs.intValue() != 0) {
                throw new IllegalStateException("Number of transactions is not 0");
            }
        }
        return new HeadersMessage(blocks);
    }

    public Block[] getBlocks() {
        return blocks;
    }
}
