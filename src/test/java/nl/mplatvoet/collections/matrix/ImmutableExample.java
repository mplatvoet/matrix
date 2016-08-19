package nl.mplatvoet.collections.matrix;

import nl.mplatvoet.collections.matrix.fn.Function;

public class ImmutableExample {
    public static void main(String[] args) {
        MutableMatrix<String> matrix = Matrices.mutableOf(25, 50);

        Matrix<String> copy = ImmutableMatrix.copyOf(matrix, new Function<String, String>() {
            @Override
            public String apply(int row, int column, String value) {
                return "+";
            }
        });
        ExampleUtil.printMatrix(matrix);
        ExampleUtil.printMatrix(copy);


    }
}
