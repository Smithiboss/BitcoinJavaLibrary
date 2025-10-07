package org.smithiboss.network;

public class DataType {

    private final int type;
    private final byte[] identifier;

    public DataType(int type, byte[] identifier) {
        if (type < 1 || type > 4) {
            throw new IllegalArgumentException("Type must be between 1 and 4");
        }
        this.type = type;
        this.identifier = identifier;
    }

    public DataType(DataTypes type, byte[] identifier) {
        this(type.getCode(), identifier);
    }

    public int getType() {
        return type;
    }

    public byte[] getIdentifier() {
        return identifier;
    }
}
