package dsekercioglu.mega.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VariableDimensionArray {

    final int[] DIMS;
    final List<GuessFactor>[] DATA;
    final int SIZE;
    final int CAPACITY;

    public VariableDimensionArray(int[] dims, int capacity) {
        int size = 1;
        for (int i = 0; i < dims.length; i++) {
            size *= dims[i];
        }
        CAPACITY = capacity;
        SIZE = size;
        DIMS = dims.clone();
        DATA = new List[SIZE];
        Arrays.setAll(DATA, ArrayList::new);
    }

    private int n21Dim(int[] indices) {
        int n = 1;
        int index = 0;
        for (int i = 0; i < indices.length; i++) {
            index += indices[i] * n;
            n *= DIMS[i];
        }
        return index;
    }

    public void addDataPoint(int[] indices, GuessFactor guessFactor) {
        DATA[n21Dim(indices)].add(guessFactor);
    }

    public List<GuessFactor> get(int[] indices) {
        return DATA[n21Dim(indices)];
    }
}
