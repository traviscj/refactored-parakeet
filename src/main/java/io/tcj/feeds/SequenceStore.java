package io.tcj.feeds;

interface SequenceStore {
    Sequence lookup(String sequenceName);

    void put(String sequenceName, long nextSequenceValue);
}
