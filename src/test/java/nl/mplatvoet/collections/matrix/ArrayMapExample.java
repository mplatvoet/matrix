package nl.mplatvoet.collections.matrix;

import nl.mplatvoet.collections.map.ArrayMap;
import nl.mplatvoet.collections.map.IntKeyMap;

import java.util.Collection;

public class ArrayMapExample {
    public static void main(String[] args) {
        ArrayMap<String> map = new ArrayMap<>();

        Collection<String> values = map.values();

        map.put(9, "World");
        map.put(3, "Hello");
        print(values);

        map.put(4, "Crewl");
        print(values);

        map.put(4, "Jolly");
        print(values);

        map.remove(4);
        map.remove(9);
        map.put(5, "Mark");

        print(values);


        final IntKeyMap<String> firstSub = map.subMap(4, 10);
        final Collection<String> firstValues = firstSub.values();
        print(firstValues);


        final IntKeyMap<String> secondSub = firstSub.subMap(4, 8);
        final Collection<String> secondValues = secondSub.values();
        print(secondValues);

        secondSub.put(7, "Platvoet");

        print("First values:");
        print(firstValues);

        print("Second values:");
        print(secondValues);


        System.out.println(firstValues.equals(secondValues));
    }

    private static void print(String s) {
        System.out.println(s);
    }

    private static void print(Collection<String> c) {
        StringBuilder sb = new StringBuilder();
        for (String s : c) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(s);
        }
        System.out.println(sb.toString());
    }
}
