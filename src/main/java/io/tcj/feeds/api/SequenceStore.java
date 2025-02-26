package io.tcj.feeds.api;

import io.tcj.feeds.model.Sequence;

public interface SequenceStore {
    Sequence lookup(String sequenceName);

    void put(String sequenceName, long nextSequenceValue);
}
