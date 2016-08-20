package nl.mplatvoet.collections.matrix;

import nl.mplatvoet.collections.matrix.fn.CellMapFunction;
import nl.mplatvoet.collections.matrix.fn.Function;
import nl.mplatvoet.collections.matrix.fn.DetachedCell;

import static nl.mplatvoet.collections.matrix.ExampleUtil.randomMatrix;

public class ImmutableExample {
    public static void main(String[] args) {
        Matrix<String> matrix = randomMatrix(25, 50);

        Matrix<String> copy = matrix.map(new CellMapFunction<String, String>() {
            @Override
            public void apply(MatrixCell<String> source, MutableCell<String> dest) {
                if (!source.isBlank()) {
                    dest.setValue("0");
                }
            }
        });

        ExampleUtil.printMatrix(matrix);
        ExampleUtil.printMatrix(copy);
    }
}
