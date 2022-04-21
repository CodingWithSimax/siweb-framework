package net.simax_dev.siweb.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Utils to escape bytes; needed for data communication between client server
 */
public class ByteUtils {
    private static byte[] toPrimitive(Byte[] data) {
        byte[] res = new byte[data.length];
        for (int i = 0; i < data.length; i++) res[i] = (byte) data[i];
        return res;
    }
    private static Byte[] toObject(byte[] data) {
        Byte[] res = new Byte[data.length];
        for (int i = 0; i < data.length; i++) res[i] = (Byte) data[i];
        return res;
    }

    public static byte[] escapeBytes(byte[] data) {
        return escapeBytes(toObject(data));
    }
    public static byte[] escapeBytes(Byte[] data) {
        List<Byte> escaped = escapeBytes(Arrays.asList(data));
        Byte[] res = new Byte[escaped.size()];
        escaped.toArray(res);
        return toPrimitive(res);
    }

    public static List<Byte> escapeBytes(List<Byte> data) {
        byte escapeByte = -128;
        List<Byte> result = new ArrayList<>();
        for (Byte datum : data) {
            result.add(datum);
            if (datum.equals(escapeByte)) {
                result.add(escapeByte);
            }
        }
        return result;
    }
}