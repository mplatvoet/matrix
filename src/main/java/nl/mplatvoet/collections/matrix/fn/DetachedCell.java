package nl.mplatvoet.collections.matrix.fn;

import nl.mplatvoet.collections.matrix.MutableCell;

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

    @Override
    public boolean isBlank() {
        return value == BLANK;
    }

    @Override
    public T setValue(T value) {
        T prev = this.value;
        this.value = value;
        return prev == BLANK ? null : prev;
    }

    public void apply(int row, int column) {
        apply(row, column, DetachedCell.blank());
    }

    public void apply(int row, int column, T value) {
        this.value = value;
        rowIndex = row;
        columnIndex = column;
    }
}
