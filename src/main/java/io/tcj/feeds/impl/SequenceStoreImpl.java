package io.tcj.feeds.impl;

import io.tcj.feeds.api.SequenceStore;
import io.tcj.feeds.model.Sequence;
import io.tcj.jooq.tables.records.KvRecord;
import io.tcj.keyvalue.KvStore;

import javax.inject.Inject;

public class SequenceStoreImpl implements SequenceStore {
    @Inject KvStore kvStore;

    @Override
    public Sequence lookup(String sequenceName) {
        KvRecord feedSeq = kvStore.get("FEED_SEQ", sequenceName);
        return new Sequence(sequenceName, Long.parseLong(new String(feedSeq.getV())));
    }

    @Override
    public void put(String sequenceName, long nextSequenceValue) {
        kvStore.put("FEED_SEQ", sequenceName, Long.toString(nextSequenceValue));
    }
}
