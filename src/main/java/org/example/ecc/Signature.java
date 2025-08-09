package org.example.ecc;

import org.example.Utils.Bytes;

import java.io.ByteArrayInputStream;

public record Signature(Int r, Int s) {

    /**
     * Serializes the {@link Signature} in DER format
     *
     * @return a {@code byte} array
     */
    public byte[] der() {
        byte[] rBin = toUnsignedDer(r);
        byte[] sBin = toUnsignedDer(s);

        return Bytes.concat(new byte[]{0x30}, new byte[]{(byte) (rBin.length + sBin.length)}, rBin, sBin);
    }

    /**
     * Parse
     *
     * @param sigBin a {@code byte} array
     * @return a {@link Signature} object
     */
    public Signature parse(byte[] sigBin) {
        var sigStream = new ByteArrayInputStream(sigBin);
        var compound = Hex.parse(sigStream.read());
        // check if first byte is 0x30
        if (compound.ne(Hex.parse("30"))) {
            throw new IllegalArgumentException("Bad signature");
        }
        var length = Hex.parse(sigStream.read());
        // check if overall length matches
        if (length.add(Int.parse(2)).ne(Int.parse(sigBin.length))) {
            throw new IllegalArgumentException("Bad signature length");
        }
        var marker = Hex.parse(sigStream.read());
        // check if marker is 0x02
        if (marker.ne(Hex.parse("02"))) {
            throw new IllegalArgumentException("Bad signature");
        }
        var rLength = Hex.parse(sigStream.read());
        // read r
        var r = Hex.parse(Bytes.read(sigStream, rLength.intValue()));
        marker = Hex.parse(sigStream.read());
        // check if marker is 0x02
        if (marker.ne(Hex.parse("02"))) {
            throw new IllegalArgumentException("Bad signature");
        }
        var sLength = Hex.parse(sigStream.read());
        // read s
        var s = Hex.parse(Bytes.read(sigStream, sLength.intValue()));
        // check if length of input byte array equals length of r + s + 6
        if (sigBin.length != rLength.intValue() + sLength.intValue() + 6) {
            throw new IllegalArgumentException("Signature too long");
        }
        return new Signature(r, s);
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
        // Add marker 0x00 if raw is signed
        if ((raw[0] & 0x80) != 0) {
            raw = Bytes.concat(new byte[]{0x00}, raw);
        }
        // Add marker byte 0x02 and length of raw
        var markers = new byte[]{0x02, (byte) raw.length};
        raw = Bytes.concat(markers, raw);

        return raw;
    }
}
