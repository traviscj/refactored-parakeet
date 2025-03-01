package io.tcj.feeds.impl;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import io.tcj.feeds.FeedEntryWebAction;
import io.tcj.feeds.FeedPublisher;
import io.tcj.feeds.api.DbFeedFetcher;
import io.tcj.feeds.api.FeedCursorStore;
import io.tcj.feeds.api.FeedListener;
import io.tcj.feeds.api.SequenceStore;
import io.tcj.feeds.model.PublishedFeedDef;
import io.tcj.jooq.tables.records.KvRecord;
import io.tcj.protos.kv.Kv;
import misk.web.WebActionModule;

public class KvFeedModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(FeedCursorStore.class)
                .to(FeedCursorStoreImpl.class);
        bind(SequenceStore.class)
                .to(SequenceStoreImpl.class);
        install(WebActionModule.create(FeedEntryWebAction.class));
        install(WebActionModule.create(FeedPublisher.FeedPublishWebAction.class));

        bind(new TypeLiteral<DbFeedFetcher<KvRecord, Kv>>() {})
                .to(DbFeedFetcherImpl.class);
//        bind(new TypeLiteral<DbFeedFetcher>() {})
//                .to(DbFeedFetcherImpl.class);
        bind(new TypeLiteral<FeedListener>() {})
                .to(PrintFeedListener.class);
        bind(new TypeLiteral<FeedPublisher<KvRecord>>() {});
        bind(PublishedFeedDef.class)
                .toInstance(new PublishedFeedDef("kv", 5));
    }
}
