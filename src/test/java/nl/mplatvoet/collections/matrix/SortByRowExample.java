package nl.mplatvoet.collections.matrix;

public class SortByRowExample {
    public static void main(String[] args) {
        MutableMatrix<String> matrix = Matrices.mutableOf(5, 2);
        matrix.getColumn(0).put(0, "c");
        matrix.getColumn(0).put(1, "d");
        matrix.getColumn(0).put(2, "a");
        matrix.getColumn(0).put(3, "e");
        matrix.getColumn(0).put(4, "b");
        matrix.getColumn(1).put(0, "5");
        matrix.getColumn(1).put(1, "4");
        matrix.getColumn(1).put(2, "3");
        matrix.getColumn(1).put(3, "2");
        matrix.getColumn(1).put(4, "1");

        System.out.println("==unsorted==");
        ExampleUtil.printMatrix(matrix);

        Matrices.sortBy(matrix.getColumn(0), String.CASE_INSENSITIVE_ORDER);
        System.out.println("==sorted==");
        ExampleUtil.printMatrix(matrix);
    }
}
