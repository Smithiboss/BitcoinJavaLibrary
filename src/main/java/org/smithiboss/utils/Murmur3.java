package org.smithiboss.utils;

public class Murmur3 {

    private Murmur3() {}

    /**
     * Implementation of Murmur3 x86_32
     *
     * @param data input bytes
     * @param seed 32-bit seed (unsigned behavior is achieved by & 0xffffffffL when needed)
     * @return 32-bit hash (as Java signed int)
     */
    public static int murmur3(byte[] data, int seed) {
        final int c1 = 0xcc9e2d51;
        final int c2 = 0x1b873593;

        int length = data.length;
        int h1 = seed;
        int roundedEnd = length & 0xfffffffc; // round down to 4 byte block

        // body - process 4 bytes at a time, little-endian load
        for (int i = 0; i < roundedEnd; i += 4) {
            int k1 = (data[i] & 0xff)
                    | ((data[i + 1] & 0xff) << 8)
                    | ((data[i + 2] & 0xff) << 16)
                    | ((data[i + 3] & 0xff) << 24);

            k1 *= c1;
            k1 = Integer.rotateLeft(k1, 15);
            k1 *= c2;

            h1 ^= k1;
            h1 = Integer.rotateLeft(h1, 13);
            h1 = h1 * 5 + 0xe6546b64;
        }

        // tail
        int k1 = 0;
        int tail = length & 0x03;
        if (tail == 3) {
            k1 = (data[roundedEnd + 2] & 0xff) << 16;
        }
        if (tail == 2 || tail == 3) {
            k1 |= (data[roundedEnd + 1] & 0xff) << 8;
        }
        if (tail == 1 || tail == 2 || tail == 3) {
            k1 |= (data[roundedEnd] & 0xff);
            k1 *= c1;
            k1 = Integer.rotateLeft(k1, 15);
            k1 *= c2;
            h1 ^= k1;
        }

        // finalization (fmix)
        h1 ^= length;
        h1 ^= (h1 >>> 16);
        h1 *= 0x85ebca6b;
        h1 ^= (h1 >>> 13);
        h1 *= 0xc2b2ae35;
        h1 ^= (h1 >>> 16);

        return h1;
    }
}