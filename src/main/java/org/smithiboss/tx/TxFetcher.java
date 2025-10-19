package org.smithiboss.tx;

import org.smithiboss.utils.Bytes;
import org.smithiboss.utils.Helper;
import org.smithiboss.ecc.Hex;
import org.smithiboss.script.Op;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class TxFetcher {

    // TODO: Finish cache implementation

    private static final Logger log = Logger.getLogger(Op.class.getSimpleName());

    private static final Map<String, String> cache = new HashMap<>();
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Retrieves the API URL for Blockstream based on the network type.
     *
     * @param testnet a boolean indicating whether to use the testnet API URL.
     *                If true, returns the testnet API URL. If false, returns the mainnet API URL.
     * @return a String representing the appropriate API URL for the specified network type.
     */
    public static String getUrl(boolean testnet) {
        return testnet ? "https://blockstream.info/testnet/api" : "https://blockstream.info/api";
    }

    /**
     * Fetches a transaction from the network or cache based on its transaction ID.
     * If the transaction is not found in the local cache, it is retrieved from the appropriate network
     * (testnet or mainnet) using the Blockstream API.
     *
     * @param txId the transaction ID as a String, expected to be in hexadecimal format.
     * @param testnet a boolean indicating whether to fetch the transaction from testnet (true)
     *                or mainnet (false).
     * @return a Tx object representing the parsed transaction. If an error occurs during retrieval
     *         or parsing, an exception may be thrown or the returned result may be null.
     */
    public static Tx fetch(String txId, boolean testnet) {
        String txId64 = Helper.zfill(64, txId);
        String rawTx = null;
        if (!cache.containsKey(txId64)) {
            String url = getUrl(testnet) + "/tx/" + txId64 + "/hex";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new IOException("HTTP error " + response.statusCode() + ": " + response.body());
                }

                try {
                    rawTx = response.body().trim();
                } catch (IllegalArgumentException e) {
                    log.severe("Invalid response body: " + response.body() + "\n" + e.getMessage());
                    return null;
                }

                Tx tx;
                byte[] rawBytes = Bytes.hexStringToByteArray(rawTx);
                if (rawBytes[4] == 0) {
                    rawBytes = Bytes.concat(Arrays.copyOfRange(rawBytes, 0, 4), Arrays.copyOfRange(rawBytes, 6, rawBytes.length));

                    tx = Tx.parse(new ByteArrayInputStream(rawBytes), testnet);
                    var locktime = Hex.parse(Bytes.reverseOrder(Arrays.copyOfRange(rawBytes, rawBytes.length - 4, rawBytes.length)));
                    tx.setLockTime(locktime);
                } else {
                    tx = Tx.parse(new ByteArrayInputStream(rawBytes), testnet);
                }

                if (!tx.getId().equals(txId64)) {
                    log.severe("Transaction ID mismatch: " + tx.getId() + " != " + txId64);
                    return null;
                }
                // cache not yet fully implemented
                if (cache != null) {
                    cache.put(txId64, rawTx);
                }
            } catch (Exception e) {
                log.severe("Error fetching transaction: " + e.getMessage());
                return null;
            }
        } else {
            rawTx = cache.get(txId64);
        }
        var txBytes = Bytes.hexStringToByteArray(rawTx);
        return Tx.parse(txBytes, testnet);
    }

    /**
     * Saves cache to a json file
     *
     * @param filePath a {@link String} object
     */
    public static void dumpCache(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<String, String> entry : cache.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            log.warning(e.getMessage());
        }
    }

    /**
     * Loads cache from a json file
     *
     * @param filePath a {@link String} object
     */
    public static void loadCache(String filePath) {
        cache.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    cache.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            log.warning(e.getMessage());
        }
    }
}
