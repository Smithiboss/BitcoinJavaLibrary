package org.smithiboss.network;

import org.smithiboss.utils.Bytes;
import org.smithiboss.utils.Helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

public class SimpleNode {

    private static final Logger log = Logger.getLogger(SimpleNode.class.getSimpleName());

    private final String host;
    private final Integer port;
    private final Boolean testnet;
    private final Boolean logging;
    private final SocketChannel socket;
    private final ByteArrayOutputStream leftoverBuffer = new ByteArrayOutputStream();

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
            socket.connect(new InetSocketAddress(this.host, this.port));
            if (socket.isConnected()) {
                System.out.println("✓ Successfully connected to " + host + ":" + port);
            } else {
                System.out.println("✗ Connection failed");
            }
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
        System.out.println(Bytes.byteArrayToHexString(envelope.serialize()));

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

        byte[] leftovers = leftoverBuffer.toByteArray();
        System.out.println("Leftovers: " + Bytes.byteArrayToHexString(leftovers));
        leftoverBuffer.reset();
        System.arraycopy(leftovers, 0, buffer.array(), 0, leftovers.length);
        currentLength += leftovers.length;

        if (currentLength >= 24) {
            expectedLength = NetworkEnvelope.parseLength(new ByteArrayInputStream(buffer.array()));
            System.out.println("Expected payload length (from leftovers): " + expectedLength);
        }

        System.out.println("Waiting for data...");

        while (expectedLength == -1 || currentLength < expectedLength + 24) {
            int bytesRead = 0;
            try {
                bytesRead = socket.read(buffer);
            } catch (IOException e) {
                System.out.println("Read error: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (bytesRead < 0) return null;

            currentLength += bytesRead;

            if (expectedLength == -1 && currentLength >= 24) {
                expectedLength = NetworkEnvelope.parseLength(new ByteArrayInputStream(buffer.array()));
                System.out.println("Expected payload length: " + expectedLength);
            }
        }

        ByteArrayInputStream input = new ByteArrayInputStream(buffer.array(), 0, expectedLength + 24);
        NetworkEnvelope envelope = NetworkEnvelope.parse(input, testnet);

        int leftoverStart = expectedLength + 24;
        int leftoverEnd = currentLength;
        byte[] remaining = Arrays.copyOfRange(buffer.array(), leftoverStart, leftoverEnd);
        leftoverBuffer.writeBytes(remaining);

        return envelope;
    }

    /**
     * Waits for one of the messages in the list
     * @param commands a {@link Set} of {@link String} objects
     * @return a {@link NetworkEnvelope} object
     */
    public NetworkEnvelope waitFor(Set<String> commands) {
        String command = "";

        NetworkEnvelope envelope = null;

        while (!commands.contains(command)) {
            envelope = read();
            if (envelope != null) {
                command = new String(envelope.getCommand());
                System.out.println("Received command: " + new String(envelope.getCommand(), StandardCharsets.UTF_8));

                if (command.equals(VerAckMessage.COMMAND)) {
                    send(new VerAckMessage());
                } else if (command.equals(PingMessage.COMMAND)) {
                    send(new PongMessage(envelope.getPayload()));
                }
            } else {
                break;
            }
        }

        return envelope;
    }

    /**
     * Performs a handshake with another node
     * @return a {@code byte} array
     */
    public byte[] handshake() {
        // create a version message
        var version = new VersionMessage();
        // send the command
        this.send(version);
        // wait for a version message
        var envelope = this.waitFor(Set.of(VerAckMessage.COMMAND));
        if (envelope == null) {
            log.severe("no verack or sendcmpct");
            return new byte[0];
        }
        return envelope.getPayload();
    }

    /**
     * Closes the socket connection to the node.
     */
    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
