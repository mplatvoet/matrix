package nl.mplatvoet.collections.matrix;


import nl.mplatvoet.collections.matrix.fn.CellMapFunction;
import nl.mplatvoet.collections.matrix.fn.Functions;

import java.util.Iterator;

import static nl.mplatvoet.collections.matrix.ExampleUtil.printMatrix;
import static nl.mplatvoet.collections.matrix.ExampleUtil.randomMatrix;
import static nl.mplatvoet.collections.matrix.Matrices.mutableCopyOf;

public class MatrixExample {

    private static final CellMapFunction<String, String> FILL_BLANK_FN = Functions.blanks(new CellMapFunction<String, String>() {
        @Override
        public void apply(MatrixCell<String> source, MutableCell<String> dest) {
            dest.setValue("+");
        }
    });

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
        subMatrix.insertRow(3).cells(FILL_BLANK_FN);
        printMatrix(subMatrix);
        System.out.println();

        System.out.println("==Delete row==");
        subMatrix.deleteRow(3);
        printMatrix(subMatrix);
        System.out.println();

        System.out.println("==Insert column==");
        subMatrix.insertColumn(5).cells(FILL_BLANK_FN);
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
        Matrix<Integer> numbers = Matrices.of(5, 5, cell -> cell.setValue(cell.getRowIndex() + cell.getColumnIndex() + 1));
        printMatrix(numbers);
        System.out.println();

        System.out.println("==Map function==");
        Matrix<String> strings = numbers.map((source, dest) -> dest.setValue("<" + source.getValue() + ">"));
        printMatrix(strings);
    }


}
