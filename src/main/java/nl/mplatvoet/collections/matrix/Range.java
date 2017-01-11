package nl.mplatvoet.collections.matrix;


import static nl.mplatvoet.collections.matrix.args.Arguments.checkArgument;
import static nl.mplatvoet.collections.matrix.args.Arguments.checkIndex;

public class Range {
    private final int rowBeginIndex;
    private final int rowEndIndex;
    private final int columnBeginIndex;
    private final int columnEndIndex;

    private Range(int rowBeginIndex, int rowEndIndex, int columnBeginIndex, int columnEndIndex) {
        checkIndex(rowBeginIndex < 0, "rowBeginIndex must be >= 0");
        checkIndex(columnBeginIndex < 0, "columnBeginIndex must be >= 0");
        checkIndex(rowEndIndex < rowBeginIndex, "rowEndIndex[%s] must be >= to rowBeginIndex[%s] ", rowEndIndex, rowBeginIndex);
        checkIndex(columnEndIndex < columnBeginIndex, "columnEndIndex[%s] must be >= to columnBeginIndex[%s] ", columnEndIndex, columnBeginIndex);

        this.rowBeginIndex = rowBeginIndex;
        this.rowEndIndex = rowEndIndex;
        this.columnBeginIndex = columnBeginIndex;
        this.columnEndIndex = columnEndIndex;
    }

    public static Range of(int minRowIndex, int maxRowIndex, int minColumnIndex, int maxColumnIndex) {
        return new Range(minRowIndex, maxRowIndex, minColumnIndex, maxColumnIndex);
    }

    public static Range of(Matrix<?> matrix) {
        checkArgument(matrix == null, "matrix cannot be null");
        return new Range(0, matrix.getRowSize(), 0, matrix.getColumnSize());
    }

    public int getRowBeginIndex() {
        return rowBeginIndex;
    }

    public int getRowEndIndex() {
        return rowEndIndex;
    }

    public int getColumnBeginIndex() {
        return columnBeginIndex;
    }

    public int getColumnEndIndex() {
        return columnEndIndex;
    }

    public int getRowSize() {
        return rowEndIndex - rowBeginIndex;
    }

    public int getColumnSize() {
        return columnEndIndex - rowBeginIndex;
    }

    public boolean matches(Matrix<?> matrix) {
        checkArgument(matrix == null, "matrix cannot be null");

        return rowBeginIndex == 0
                && columnBeginIndex == 0
                && getRowSize() == matrix.getRowSize()
                && getColumnSize() == matrix.getColumnSize();
    }

    public boolean fits(Matrix<?> matrix) {
        checkArgument(matrix == null, "matrix cannot be null");
        return rowBeginIndex <= matrix.getRowSize() && rowEndIndex <= matrix.getRowSize()
                && columnBeginIndex <= matrix.getColumnSize() && columnEndIndex <= matrix.getColumnSize();
    }

    public boolean fits(Range range) {
        checkArgument(range == null, "range cannot be null");
        return rowBeginIndex <= range.getRowSize() && rowEndIndex <= range.getRowSize()
                && columnBeginIndex <= range.getColumnSize() && columnEndIndex <= range.getColumnSize();
    }
}
