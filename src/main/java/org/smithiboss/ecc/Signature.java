package org.smithiboss.ecc;

import org.smithiboss.utils.Bytes;

import java.io.ByteArrayInputStream;

public record Signature(Int r, Int s) {

    /**
     * Constructs a DER-encoded byte array representing the cryptographic signature,
     * combining the DER-encoded values of r and s along with the appropriate prefix structure.
     *
     * @return a byte array containing the DER-encoded representation of the signature.
     */
    public byte[] der() {
        byte[] rBin = toUnsignedDer(r);
        byte[] sBin = toUnsignedDer(s);
        return Bytes.concat(new byte[]{0x30}, new byte[]{(byte) (rBin.length + sBin.length)}, rBin, sBin);
    }

    /**
     * Parses a DER-formatted byte array representing a cryptographic signature
     * and returns a {@link Signature} object.
     *
     * @param sigBin the byte array containing the DER-encoded signature.
     * @return a {@link Signature} object containing the parsed r and s components.
     * @throws IllegalArgumentException if the byte array does not conform to the
     *                                  expected format, contains invalid markers,
     *                                  or has an incorrect length.
     */
    public static Signature parse(byte[] sigBin) {
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
     * Converts an integer value to its DER-encoded unsigned representation.
     * The result is a byte array where the integer is formatted according to DER encoding rules,
     * including the addition of necessary markers and handling of sign extension.
     *
     * @param value the {@link Int} value to be encoded as an unsigned DER byte array.
     * @return a byte array representing the unsigned DER-encoded value.
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
