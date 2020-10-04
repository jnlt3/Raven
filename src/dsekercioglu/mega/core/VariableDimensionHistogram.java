package dsekercioglu.mega.core;

public class VariableDimensionHistogram {

    final int[] DIMS;
    final double[][] BINS;
    final int SIZE;
    
    public VariableDimensionHistogram(int[] dims, int bins) {
        int size = 1;
        for (int i = 0; i < dims.length; i++) {
            size *= dims[i];
        }
        SIZE = size;
        DIMS = dims.clone();
        BINS = new double[SIZE][bins];
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

    public void addDataPoint(int[] indices, int bin, double weight) {
        BINS[n21Dim(indices)][bin] += weight;
    }

    public double[] get(int[] indices) {
        return BINS[n21Dim(indices)];
    }
}
