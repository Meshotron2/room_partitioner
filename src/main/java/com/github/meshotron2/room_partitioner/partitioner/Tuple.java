package com.github.meshotron2.room_partitioner.partitioner;

public class Tuple<T, E> {
    private final T v1;
    private final E v2;

    public Tuple(T v1, E v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    public T getV1() {
        return v1;
    }

    public E getV2() {
        return v2;
    }
}
