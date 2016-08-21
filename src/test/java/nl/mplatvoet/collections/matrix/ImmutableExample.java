package nl.mplatvoet.collections.matrix;

import nl.mplatvoet.collections.matrix.fn.CellMapFunction;
import nl.mplatvoet.collections.matrix.fn.Functions;
import nl.mplatvoet.collections.matrix.fn.ValueFunction;

import static nl.mplatvoet.collections.matrix.ExampleUtil.randomMatrix;

public class ImmutableExample {
    public static void main(String[] args) {
        Matrix<String> matrix = randomMatrix(25, 50);

        CellMapFunction<String, String> function = Functions.noBlanks(Functions.cellMapFunctionOf(new ValueFunction<String, String>() {
            @Override
            public String apply(int row, int column, String value) {
                return "0";
            }
        }));
        Matrix<String> copy = matrix.map(function);

        ExampleUtil.printMatrix(matrix);
        ExampleUtil.printMatrix(copy);
    }
}
