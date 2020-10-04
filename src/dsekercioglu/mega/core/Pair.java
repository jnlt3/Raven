package dsekercioglu.mega.core;

public class Pair<T, C extends Comparable> implements Comparable {

    T T;
    C C;

    public Pair(T object, C comparable) {
        T = object;
        C = comparable;
    }

    public T getObject() {
        return T;
    }

    public C getComparable() {
        return C;
    }

    @Override
    public int compareTo(Object o) {
        return C.compareTo(((Pair) o).C);
    }

}
