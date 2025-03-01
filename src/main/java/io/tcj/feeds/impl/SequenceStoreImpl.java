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
        KvRecord feedSeq = kvStore.getOptional("FEED_SEQ", sequenceName)
                .orElseGet(() -> {
                    KvRecord kvRecord = new KvRecord();
                    kvRecord.setNs("FEED_SEQ");
                    kvRecord.setK(sequenceName);
                    kvRecord.setV("0");
                    return kvRecord;

                });
        return new Sequence(sequenceName, Long.parseLong(feedSeq.getV()));
    }

    @Override
    public void put(String sequenceName, long nextSequenceValue) {
        kvStore.putOrUpdate("FEED_SEQ", sequenceName, Long.toString(nextSequenceValue));
    }
}
