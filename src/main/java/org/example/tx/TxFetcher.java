package org.example.tx;

import org.example.Utils.Helper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TxFetcher {

    private static final Map<String, Tx> cache = new HashMap<>();
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    public boolean testnet;

    public static String getUrl(boolean testnet) {
        return testnet ? "https://blockstream.info/testnet/api" : "https://blockstream.info/api";
    }

    public static Tx fetch(String txId, boolean testnet, boolean fresh) {
        if (fresh || !cache.containsKey(txId)) {
            String url = getUrl(testnet) + "/tx/" + txId + "/hex/";
            String hex;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new IOException("HTTP error " + response.statusCode() + ": " + response.body());
                }

                byte[] raw;
                try {
                    raw = Helper.hexStringToByteArray(response.body().trim());
                } catch (IllegalArgumentException e) {
                    throw new IOException("Invalid hex: " + response.body());
                }
                Tx tx;
                if ((raw[4] & 0xFF) == 0) {
                    byte[] trimmed = new byte[raw.length - 2];
                    System.arraycopy(raw, 0, trimmed, 0, 4);
                    System.arraycopy(raw, 6, trimmed, 4, raw.length - 6);

                    tx = Tx.parseLegacy(new ByteArrayInputStream(trimmed), testnet);
                    byte[] lockTimeBytes = Arrays.copyOfRange(raw, raw.length - 4, raw.length);
                    tx.setLockTime(Helper.littleEndianToInt(lockTimeBytes));
                } else {
                    tx = Tx.parseLegacy(new ByteArrayInputStream(raw), testnet);
                }

                if (!tx.getId().equals(txId)) {
                    throw new IOException("Transaction ID mismatch: " + tx.getId() + " vs " + txId);
                }
                cache.put(txId, tx);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Tx result = cache.get(txId);
        result.setTestnet(testnet);
        return result;
    }
}
