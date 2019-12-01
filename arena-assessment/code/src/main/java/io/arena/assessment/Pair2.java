package io.arena.assessment;

import java.util.Objects;

public class Pair2<T1, T2> {

    final T1 first;
    final T2 second;

    public Pair2(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "(" + first + "," + second + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair2<?, ?> pair2 = (Pair2<?, ?>) o;
        return Objects.equals(first, pair2.first) &&
                Objects.equals(second, pair2.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
