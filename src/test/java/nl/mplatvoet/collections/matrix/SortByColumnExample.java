package nl.mplatvoet.collections.matrix;

import static nl.mplatvoet.collections.matrix.Matrices.mutableCopyOf;

public class SortByColumnExample {
    public static void main(String[] args) {
        MutableMatrix<String> matrix = mutableCopyOf(new String[][] {
                {"c", "5"},
                {"d", "4"},
                {"a", "3"},
                {"e", "2"},
                {"b", "1"}
        });

        System.out.println("==unsorted==");
        ExampleUtil.printMatrix(matrix);

        Matrices.sortBy(matrix.getColumn(0), String.CASE_INSENSITIVE_ORDER);
        System.out.println("==sorted==");
        ExampleUtil.printMatrix(matrix);
    }
}
