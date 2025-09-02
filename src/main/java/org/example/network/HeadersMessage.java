package org.example.network;

import org.example.block.Block;
import org.example.utils.Helper;

import java.io.ByteArrayInputStream;

public class HeadersMessage {

    private static final String COMMAND = "headers";

    private final Block[] blocks;


    public HeadersMessage(Block[] blocks) {
        this.blocks = blocks;
    }

    /**
     * Parse
     * @param s a {@link ByteArrayInputStream} object
     * @return a {@link HeadersMessage} object
     */
    public static HeadersMessage parse(ByteArrayInputStream s) {
        var numHeaders = Helper.readVarint(s);

        var blocks = new Block[numHeaders.intValue()];

        for (int i = 0; i < numHeaders.intValue(); i++) {
            blocks[i] = Block.parse(s);

            var numTxs = Helper.readVarint(s);
            if (numTxs.intValue() != 0) {
                throw new IllegalStateException("Number of transactions is not 0");
            }
        }
        return new HeadersMessage(blocks);
    }
}
