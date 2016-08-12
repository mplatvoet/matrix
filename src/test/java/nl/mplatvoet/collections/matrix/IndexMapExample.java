package nl.mplatvoet.collections.matrix;

import java.util.Collection;

public class IndexMapExample {
    public static void main(String[] args) {
        IndexMap<String> map = new IndexMap<>();
        Collection<String> values = map.values();

        map.put(9, "World");
        map.put(3, "Hello");
        printValues(values);

        map.put(4, "Crewl");
        printValues(values);

        map.put(4, "Jolly");
        printValues(values);

        map.remove(4);
        map.remove(9);
        map.put(5, "Mark");
        printValues(values);

    }

    private static void printValues(Collection<String> c) {
        StringBuilder sb = new StringBuilder();
        for (String s : c) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(s);
        }
        System.out.println(sb.toString());
    }
}
