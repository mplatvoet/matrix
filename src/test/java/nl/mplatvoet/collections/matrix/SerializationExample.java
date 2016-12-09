package nl.mplatvoet.collections.matrix;

import nl.mplatvoet.collections.map.ArrayMap;
import org.apache.commons.lang3.SerializationUtils;

import java.util.Map;

public class SerializationExample {

    public static void main(String[] args) {
        ArrayMap<String> source = new ArrayMap<>();
        source.put(0, null);
        source.put(100, "Hello");
        source.put(1000, "World");
        source.remove(1000);

        final byte[] bytes = SerializationUtils.serialize(source);
        final ArrayMap<String> clone = SerializationUtils.deserialize(bytes);

        for (Map.Entry<Integer, String> entry : clone.entrySet()) {
            System.out.println("key: " + entry.getKey() + ", value: " + entry.getValue());
        }
    }
}
