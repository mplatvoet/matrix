package nl.mplatvoet.collections.matrix;

import static nl.mplatvoet.collections.matrix.Matrices.mutableCopyOf;

public class SortByRowExample {
    public static void main(String[] args) {
        MutableMatrix<String> matrix = mutableCopyOf(new String[][] {
                {"c","d","a","e","b"},
                {"5","4","3","2","1"}
        });

        System.out.println("==unsorted==");
        ExampleUtil.printMatrix(matrix);

        Matrices.sortBy(matrix.getRow(0));
        System.out.println("==sorted==");
        ExampleUtil.printMatrix(matrix);
    }
}
