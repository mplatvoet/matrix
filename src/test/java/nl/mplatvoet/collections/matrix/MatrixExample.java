package nl.mplatvoet.collections.matrix;


import nl.mplatvoet.collections.matrix.fn.Function;
import nl.mplatvoet.collections.matrix.fn.Result;
import nl.mplatvoet.collections.matrix.range.Range;

import java.util.Iterator;

import static nl.mplatvoet.collections.matrix.ExampleUtil.printMatrix;
import static nl.mplatvoet.collections.matrix.ExampleUtil.randomMatrix;
import static nl.mplatvoet.collections.matrix.Matrices.mutableCopyOf;

public class MatrixExample {

    private static final Function<String, String> PLUS_FACTORY = new Function<String, String>() {
        @Override
        public void apply(int row, int column, String value, Result<String> result) {
            result.setValue("+");
        }
    };

    public static void main(String[] args) throws Exception {
        Matrix<String> matrix = randomMatrix(10, 20);
        System.out.println("==Generated matrix==");
        printMatrix(matrix);
        System.out.println();

        System.out.println("==Sub matrix==");
        MutableMatrix<String> subMatrix = mutableCopyOf(matrix, Range.of(0, 6, 0, 10));
        printMatrix(subMatrix);
        System.out.println();

        System.out.println("==Insert row==");
        subMatrix.insertRow(3).fillBlanks(PLUS_FACTORY);
        printMatrix(subMatrix);
        System.out.println();

        System.out.println("==Delete row==");
        subMatrix.deleteRow(3);
        printMatrix(subMatrix);
        System.out.println();

        System.out.println("==Insert column==");
        subMatrix.insertColumn(5).fillBlanks(PLUS_FACTORY);
        printMatrix(subMatrix);
        System.out.println();

        System.out.println("==Delete column==");
        subMatrix.deleteColumn(5);
        printMatrix(subMatrix);
        System.out.println();


        System.out.println("==Delete rows - iterator==");
        for (Iterator<Row<String>> iter = subMatrix.rows().iterator(); iter.hasNext(); ) {
            Row<String> row = iter.next();
            if (row.getRowIndex() > 3) iter.remove();
        }
        printMatrix(subMatrix);
        System.out.println();

        System.out.println("==Delete columns - iterator==");
        for (Iterator<Column<String>> iter = subMatrix.columns().iterator(); iter.hasNext(); ) {
            Column<String> column = iter.next();
            if (column.getColumnIndex() > 5) iter.remove();
        }
        printMatrix(subMatrix);
        System.out.println();

        System.out.println("==Fill function==");
        Matrix<Integer> numbers = Matrices.of(5, 5, new Function<Integer, Integer>() {

            @Override
            public void apply(int row, int column, Integer value, Result<Integer> result) {
                result.setValue(++row + column);
            }
        });
        printMatrix(numbers);
        System.out.println();

        System.out.println("==Map function==");
        Matrix<String> strings = numbers.map(new Function<Integer, String>() {

            @Override
            public void apply(int row, int column, Integer value, Result<String> result) {
                result.setValue("<" + value + ">");
            }
        });
        printMatrix(strings);
    }


}
