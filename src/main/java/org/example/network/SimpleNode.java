package org.example.network;

import org.example.script.Op;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Objects;
import java.util.logging.Logger;

public class SimpleNode {

    private static final Logger log = Logger.getLogger(Op.class.getSimpleName());

    private final String host;
    private final Integer port;
    private final Boolean testnet;
    private final Boolean logging;
    private final SocketChannel socket;

    public SimpleNode(String host, Integer port, Boolean testnet, Boolean logging) {
        this.host = host;
        this.port = Objects.requireNonNullElseGet(port, () -> {
            if (testnet) {
                return 18333;
            } else {
                return 8333;
            }
        });
        this.testnet = Objects.requireNonNullElse(testnet, false);
        this.logging = Objects.requireNonNullElse(logging, false);
        try {
            this.socket = SocketChannel.open();
            socket.configureBlocking(true);
            socket.connect(new InetSocketAddress(host, port));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends a message to the connected node
     * @param message an object implementing {@link Message}
     */
    public void send(Message message) {
        var envelope = new NetworkEnvelope(message.getCommand(), message.serialize(), testnet);

        if (logging) {
            log.fine("Sending %s".formatted(envelope));
        }

        try {
            socket.write(ByteBuffer.wrap(envelope.serialize()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads a message from the socket
     * @return a {@link NetworkEnvelope} object
     */
    public NetworkEnvelope read() {
        var buffer = ByteBuffer.allocate(256 * 1024);
        int currentLength = 0;
        int expectedLength = -1;

        do {
            try {
                currentLength += socket.read(buffer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (currentLength > 0)
                expectedLength = NetworkEnvelope.parseLength(new ByteArrayInputStream(buffer.array()));

            if (currentLength < 0) return null;

        } while (currentLength < expectedLength);

        var envelope = NetworkEnvelope.parse(new ByteArrayInputStream(buffer.array()), testnet);

        if (logging) {
            log.fine("Received %s".formatted(envelope));
        }
        return envelope;
    }
}
