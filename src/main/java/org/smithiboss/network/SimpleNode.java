package org.smithiboss.network;

import org.smithiboss.utils.Bytes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
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

        System.out.println("Waiting for data...");

        do {
            try {
                int bytesRead = socket.read(buffer);
                if (bytesRead > 0) {
                    currentLength += bytesRead;
                    System.out.println("Read " + bytesRead + " bytes, total: " + currentLength);
                } else if (bytesRead == -1) {
                    System.out.println("Connection closed by peer");
                    return null;
                }
            } catch (IOException e) {
                System.out.println("Read error: " + e.getMessage());
                throw new RuntimeException(e);
            }

            if (currentLength > 0) {
                expectedLength = NetworkEnvelope.parseLength(
                        new ByteArrayInputStream(buffer.array(), 0, currentLength)
                );
                System.out.println("Expected length: " + expectedLength + ", current: " + currentLength);
            }

        } while (currentLength < expectedLength);

        if (currentLength > 0) {
            System.out.println("Received complete message, length: " + currentLength);
            byte[] receivedData = new byte[currentLength];
            buffer.rewind();
            buffer.get(receivedData, 0, currentLength);
            System.out.println("Raw data: " + Bytes.byteArrayToHexString(receivedData));

            try {
                var envelope = NetworkEnvelope.parse(
                        new ByteArrayInputStream(receivedData), testnet
                );
                System.out.println("Parsed envelope: " + envelope);
                return envelope;
            } catch (Exception e) {
                System.out.println("Parse error: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        }

        return null;
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
