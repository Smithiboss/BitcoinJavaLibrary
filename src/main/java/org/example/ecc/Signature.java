package org.example.ecc;

import org.example.Utils.Bytes;

public record Signature(Int r, Int s) {

    /**
     * Serializes the {@link Signature} in DER format
     *
     * @return a {@code byte} array
     */
    public byte[] der() {
        byte[] rBin = toUnsignedDer(r);
        byte[] sBin = toUnsignedDer(s);

        byte[] sequence = new byte[6 + rBin.length + sBin.length];
        int pos = 0;

        byte[] der = Bytes.concat(new byte[]{0x03},
                Bytes.concat(new byte[]{(byte) (rBin.length + sBin.length)}, Bytes.concat(rBin, sBin)));

        sequence[pos++] = 0x30;
        sequence[pos++] = (byte) (rBin.length + sBin.length + 4);
        sequence[pos++] = 0x02;
        sequence[pos++] = (byte) rBin.length;
        System.arraycopy(rBin, 0, sequence, pos, rBin.length);
        pos += rBin.length;
        sequence[pos++] = 0x02;
        sequence[pos++] = (byte) sBin.length;
        System.arraycopy(sBin, 0, sequence, pos, sBin.length);

        return der;

    }

    /**
     * Formats r and s for DER format
     *
     * @param value a {@link Int} object
     * @return {@code byte} array
     */
    private byte[] toUnsignedDer(Int value) {
        // Format to 32 bytes and remove leading 0x00 bytes
        byte[] raw = Bytes.lStrip(value.toBytes(32));

        if ((raw[0] & 0x80) != 0) {
            // Add marker 0x00 if raw is signed
            raw = Bytes.concat(new byte[]{0x00}, raw);
        }
        // Add marker 0x02
        var markers = new byte[]{0x02, (byte) raw.length};
        raw = Bytes.concat(markers, raw);

        return raw;
    }
}
