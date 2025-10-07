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

    public void addData(DataType dataType) {
        data.add(dataType);
    }

    @Override
    public byte[] getCommand() {
        return COMMAND.getBytes();
    }

    @Override
    public byte[] serialize() {
        var result = new ByteArrayOutputStream();

        result.writeBytes(Helper.encodeVarInt(Int.parse(data.size())));

        for (DataType dataType : data) {
            result.writeBytes(Int.parse(dataType.getType()).toBytesLittleEndian(4));

            result.writeBytes(Bytes.reverseOrder(dataType.getIdentifier()));
        }
        return result.toByteArray();
    }
}
