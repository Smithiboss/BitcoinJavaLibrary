package org.smithiboss.network;

public enum DataTypes {

    TX(1),
    BLOCK(2),
    MERKLE_BLOCK(3),
    COMPACT_BLOCK(4);

    private final int code;

    DataTypes(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static DataTypes fromCode(int code) {
        for (DataTypes t : values()) {
            if (t.code == code) return t;
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }

}
