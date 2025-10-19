package org.smithiboss.network;

import org.smithiboss.ecc.Int;
import org.smithiboss.utils.Bytes;
import org.smithiboss.utils.Helper;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class GetDataMessage implements Message {

    public static final String COMMAND = "getdata";

    private final List<DataType> data = new ArrayList<>();

    /**
     * Adds a DataType object to the internal data list.
     *
     * @param dataType the DataType object to be added, which contains type information
     *                 and a unique identifier.
     */
    public void addData(DataType dataType) {
        data.add(dataType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] serialize() {
        var result = new ByteArrayOutputStream();
        // encode the length of data as varint
        result.writeBytes(Helper.encodeVarInt(Int.parse(data.size())));
        for (DataType dataType : data) {
            // encode the datatype
            result.writeBytes(Int.parse(dataType.getType()).toBytesLittleEndian(4));
            // add the data
            result.writeBytes(Bytes.reverseOrder(dataType.getIdentifier()));
        }
        return result.toByteArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getCommand() {
        return COMMAND.getBytes();
    }
}
