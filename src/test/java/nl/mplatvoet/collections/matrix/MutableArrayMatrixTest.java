package nl.mplatvoet.collections.matrix;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MutableArrayMatrixTest {

    @Test
    public void testConsolidateRows_skipRowsAtCreation() {
        MutableMatrix<String> matrix = MutableArrayMatrix.of();
        matrix.put(0,0, "A");
        matrix.put(2,0, "B");

        assertThat("initial size row size must be greater", matrix.getRowSize(), is(3));
        matrix.consolidateRows();

        assertThat("Max index must have changed", matrix.getRowSize(), is(2));
        assertThat("Index must have shifted", matrix.get(1,0), is("B"));
    }

    @Test
    public void testConsolidateRows_clearAll() {
        MutableMatrix<String> matrix = MutableArrayMatrix.of(10, 10);

        assertThat("initial size row size must be greater", matrix.getRowSize(), is(10));
        matrix.consolidateRows();

        assertThat("Rows must be empty", matrix.getRowSize(), is(0));
    }

    @Test
    public void testConsolidateRows_blankHead() {
        MutableMatrix<String> matrix = MutableArrayMatrix.of();
        matrix.put(9,0, "A");

        assertThat("initial size row size must be greater", matrix.getRowSize(), is(10));
        matrix.consolidateRows();

        assertThat("Rows must be empty", matrix.getRowSize(), is(1));
        assertThat("Index must have shifted", matrix.get(0,0), is("A"));
    }

    @Test
    public void testConsolidateRows_blankTail() {
        MutableMatrix<String> matrix = MutableArrayMatrix.of(10, 10);
        matrix.put(0,0, "A");

        assertThat("initial size row size must be greater", matrix.getRowSize(), is(10));
        matrix.consolidateRows();

        assertThat("Rows must be empty", matrix.getRowSize(), is(1));
        assertThat("Index must have shifted", matrix.get(0,0), is("A"));
    }

    @Test
    public void testConsolidateRows_blankRow() {
        MutableMatrix<String> matrix = MutableArrayMatrix.of();
        matrix.put(0,0, "A");
        matrix.put(1,0, "B");
        assertThat("initial size row size must be greater", matrix.getRowSize(), is(2));
        matrix.getCell(1,0).clear();

        matrix.consolidateRows();

        assertThat("Max index must have changed", matrix.getRowSize(), is(1));
        assertThat("Index must have shifted", matrix.get(0,0), is("A"));
    }


    @Test
    public void testConsolidateColumns_skipColumnsAtCreation() {
        MutableMatrix<String> matrix = MutableArrayMatrix.of();
        matrix.put(0,0, "A");
        matrix.put(0,2, "B");

        assertThat("initial size column size must be greater", matrix.getColumnSize(), is(3));
        matrix.consolidateColumns();

        assertThat("Max index must have changed", matrix.getColumnSize(), is(2));
        assertThat("Index must have shifted", matrix.get(0,1), is("B"));
    }

    @Test
    public void testConsolidateColumns_clearAll() {
        MutableMatrix<String> matrix = MutableArrayMatrix.of(10, 10);

        assertThat("initial size column size must be greater", matrix.getColumnSize(), is(10));
        matrix.consolidateColumns();

        assertThat("Rows must be empty", matrix.getColumnSize(), is(0));
    }

    @Test
    public void testConsolidateColumns_blankHead() {
        MutableMatrix<String> matrix = MutableArrayMatrix.of();
        matrix.put(0,9, "A");

        assertThat("initial size column size must be greater", matrix.getColumnSize(), is(10));
        matrix.consolidateColumns();

        assertThat("Rows must be empty", matrix.getColumnSize(), is(1));
        assertThat("Index must have shifted", matrix.get(0,0), is("A"));
    }

    @Test
    public void testConsolidateColumns_blankTail() {
        MutableMatrix<String> matrix = MutableArrayMatrix.of(10, 10);
        matrix.put(0,0, "A");

        assertThat("initial size column size must be greater", matrix.getColumnSize(), is(10));
        matrix.consolidateColumns();

        assertThat("Rows must be empty", matrix.getColumnSize(), is(1));
        assertThat("Index must have shifted", matrix.get(0,0), is("A"));
    }

    @Test
    public void testConsolidateColumns_blankColumn() {
        MutableMatrix<String> matrix = MutableArrayMatrix.of();
        matrix.put(0,0, "A");
        matrix.put(0,1, "B");
        assertThat("initial size column size must be greater", matrix.getColumnSize(), is(2));
        matrix.getCell(0,1).clear();

        matrix.consolidateColumns();

        assertThat("Max index must have changed", matrix.getColumnSize(), is(1));
        assertThat("Index must have shifted", matrix.get(0,0), is("A"));
    }

}