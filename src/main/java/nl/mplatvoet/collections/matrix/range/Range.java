package nl.mplatvoet.collections.matrix.range;


public interface Range {
    int getMinRowIndex();
    int getMaxRowIndex();
    int getMinColumnIndex();
    int getMaxColumnIndex();

    boolean contains(int row, int column);
}
