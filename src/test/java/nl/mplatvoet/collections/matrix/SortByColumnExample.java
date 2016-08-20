package nl.mplatvoet.collections.matrix;

public class SortByColumnExample {
    public static void main(String[] args) {
        MutableMatrix<String> matrix = Matrices.mutableOf(2, 5);
        matrix.getRow(0).put(0, "c");
        matrix.getRow(0).put(1, "d");
        matrix.getRow(0).put(2, "a");
        matrix.getRow(0).put(3, "e");
        matrix.getRow(0).put(4, "b");
        matrix.getRow(1).put(0, "5");
        matrix.getRow(1).put(1, "4");
        matrix.getRow(1).put(2, "3");
        matrix.getRow(1).put(3, "2");
        matrix.getRow(1).put(4, "1");

        System.out.println("==unsorted==");
        ExampleUtil.printMatrix(matrix);

        Matrices.sortBy(matrix.getRow(0));
        System.out.println("==sorted==");
        ExampleUtil.printMatrix(matrix);
    }
}
