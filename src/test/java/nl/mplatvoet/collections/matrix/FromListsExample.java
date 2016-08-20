package nl.mplatvoet.collections.matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FromListsExample {
    public static void main(String[] args) {
        List<List<String>> lists = generate(10, 20);
        Matrix<String> matrix = Matrices.copyOf(lists);

        System.out.println(matrix);
        ExampleUtil.printMatrix(matrix);
    }

    private static List<List<String>> generate(int r, int c) {
        final Random random = new Random();
        List<List<String>> rows = new ArrayList<>(r);
        for (int i = 0; i < r; i++) {
            List<String> columns = new ArrayList<>(c);
            rows.add(columns);
            for (int j = 0; j < c; j++) {
                columns.add(random.nextInt(3) == 0 ? "*" : null);
            }
        }
        return rows;
    }
}
