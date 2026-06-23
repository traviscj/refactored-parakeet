package io.tcj.keyvalue;

import com.google.inject.AbstractModule;
import io.tcj.feeds.FeedPublisher;
import io.tcj.feeds.api.FeedCursorStore;
import io.tcj.feeds.api.SequenceStore;
import io.tcj.feeds.impl.FeedCursorStoreImpl;
import io.tcj.feeds.impl.PrintFeedListener;
import io.tcj.feeds.impl.SequenceStoreImpl;
import io.tcj.feeds.model.FeedConsumerDef;
import io.tcj.feeds.model.FeedTableDef;
import io.tcj.feeds.model.PublishedFeedDef;
import io.tcj.feeds.module.ExposedFeedModule;
import io.tcj.feeds.module.FeedConsumerModule;
import io.tcj.feeds.module.FeedPublisherModule;
import io.tcj.feeds.module.FeedSourceModule;
import io.tcj.jooq.tables.records.KvRecord;
import io.tcj.protos.kv.Kv;
import misk.web.WebActionModule;

import static io.tcj.jooq.tables.Kv.KV;

/**
 * Composes the "kv" feed from the generic publish/expose/consume framework modules.
 * The only kv-specific knowledge -- the table, its columns, and the row-&gt;Kv mapping
 * -- lives in the {@link FeedTableDef} below; the framework modules are table-agnostic.
 */
public class KvFeedModule extends AbstractModule {
    @Override
    protected void configure() {
        // feed-agnostic, kv-backed stores (no dedicated sequences/feed_cursors tables yet).
        bind(FeedCursorStore.class).to(FeedCursorStoreImpl.class);
        bind(SequenceStore.class).to(SequenceStoreImpl.class);

        FeedTableDef<KvRecord, Kv> kvTableDef = new FeedTableDef<>(
                KV, KV.FEED_SYNC_ID, KV.ID, KV.SHARD,
                record -> new Kv.Builder()
                        .ns(record.getNs())
                        .k(record.getK())
                        .v(record.getV())
                        .build());

        // the single generic /io.tcj.FeedSourceService/Fetch endpoint + feed registry:
        install(new FeedSourceModule());

        // publish -> expose -> consume, composed for the "kv" feed:
        install(new FeedPublisherModule<>(KvRecord.class, Kv.class, kvTableDef, new PublishedFeedDef("kv", 5)));
        install(new ExposedFeedModule<>("kv", KvRecord.class, Kv.class, ExposedFeedModule.Exposure.WIRE_PROTO));
        install(new FeedConsumerModule<>(KvRecord.class, Kv.class,
                new FeedConsumerDef("kv", 0, 50, -1), PrintFeedListener.class));

        // publish trigger (still kv-specific; TODO: genericize like the fetch side):
        install(WebActionModule.create(FeedPublisher.FeedPublishWebAction.class));
    }
}
