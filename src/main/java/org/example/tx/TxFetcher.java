package org.example.tx;

import org.example.Utils.Bytes;
import org.example.Utils.Helper;
import org.example.ecc.Hex;
import org.example.script.Op;

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

    private static final Logger log = Logger.getLogger(Op.class.getSimpleName());

    private static final Map<String, String> cache = new HashMap<>();
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     *
     * @param testnet
     * @return
     */
    public static String getUrl(boolean testnet) {
        return testnet ? "https://blockstream.info/testnet/api" : "https://blockstream.info/api";
    }

    /**
     *
     * @param txId
     * @param testnet
     * @return
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
                    log.info(rawTx);
                } catch (IllegalArgumentException e) {
                    throw new IOException("Invalid hex: " + response.body());
                }

                Tx tx;
                byte[] rawBytes = Bytes.hexStringToByteArray(rawTx);
                if (rawBytes[4] == 0) {
                    log.info("yes");
                    rawBytes = Bytes.concat(Arrays.copyOfRange(rawBytes, 0, 4), Arrays.copyOfRange(rawBytes, 6, rawBytes.length));

                    tx = Tx.parseLegacy(new ByteArrayInputStream(rawBytes), testnet);
                    var locktime = Hex.parse(Bytes.reverseOrder(Arrays.copyOfRange(rawBytes, rawBytes.length - 4, rawBytes.length)));
                    tx.setLockTime(locktime);
                } else {
                    tx = Tx.parseLegacy(new ByteArrayInputStream(rawBytes), testnet);
                }

                log.info(tx.toString());

                if (tx.getId().equals(txId64)) {
                    throw new IOException("Transaction ID mismatch: " + tx.getId() + " vs " + txId64);
                }
                if (cache != null) {
                    cache.put(txId64, rawTx);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            rawTx = cache.get(txId64);
        }
        var txBytes = Bytes.hexStringToByteArray(rawTx);
        Tx tx = Tx.parseLegacy(txBytes, testnet);
        return tx;
    }

    /**
     * Saves cache to json
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
     * Loads cache from json
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
