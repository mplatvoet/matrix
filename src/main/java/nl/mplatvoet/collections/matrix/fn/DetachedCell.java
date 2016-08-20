package nl.mplatvoet.collections.matrix.fn;

import nl.mplatvoet.collections.matrix.Cell;
import nl.mplatvoet.collections.matrix.MutableCell;
import nl.mplatvoet.collections.matrix.args.Arguments;

import static nl.mplatvoet.collections.matrix.args.Arguments.checkArgument;

public final class DetachedCell<T> implements MutableCell<T> {
    private static final Object BLANK = new Object();

    private T value = blank();
    private int rowIndex = -1;
    private int columnIndex = -1;

    @SuppressWarnings("unchecked")
    private static <T> T blank() {
        return (T) BLANK;
    }

    @Override
    public void clear() {
        value = blank();
    }

    public void reset() {
        value = blank();
        rowIndex = -1;
        columnIndex = -1;
    }

    @Override
    public T getValue() {
        return isBlank() ? null : value;
    }

    @Override
    public int getColumnIndex() {
        return columnIndex;
    }

    @Override
    public int getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }

    @Override
    public boolean isBlank() {
        return value == BLANK;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    public void apply(int row, int column) {
        apply(row, column, DetachedCell.<T>blank());
    }

    public void apply(int row, int column, T value) {
        this.value = value;
        rowIndex = row;
        columnIndex = column;
    }

    public void apply(Cell<? extends T> cell) {
        checkArgument(cell == null, "cell cannot be null");
        apply(cell.getRowIndex(), cell.getColumnIndex(), cell.getValue());
    }
}
