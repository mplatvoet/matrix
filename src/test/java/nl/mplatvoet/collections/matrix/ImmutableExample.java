package nl.mplatvoet.collections.matrix;

import static nl.mplatvoet.collections.matrix.ExampleUtil.randomMatrix;
import static nl.mplatvoet.collections.matrix.fn.Functions.cellMapFunctionOf;
import static nl.mplatvoet.collections.matrix.fn.Functions.noBlanks;

public class ImmutableExample {
    public static void main(String[] args) {
        Matrix<String> matrix = randomMatrix(25, 50);
        Matrix<String> copy = matrix.map(noBlanks(cellMapFunctionOf(value -> "0")));

        ExampleUtil.printMatrix(matrix);
        ExampleUtil.printMatrix(copy);
    }
}
