package io.tcj.feeds.impl;

import com.squareup.wire.Message;
import io.tcj.feeds.ExposedFeed;
import io.tcj.feeds.api.DbFeedFetcher;
import io.tcj.feeds.model.FeedCursor;
import io.tcj.feeds.model.FetchResult;
import io.tcj.protos.feeds.FeedFetchRequest;
import io.tcj.protos.feeds.FeedFetchResponse;
import okio.ByteString;
import org.jooq.UpdatableRecord;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapts one feed's typed {@link DbFeedFetcher} to the type-erased {@link ExposedFeed}
 * boundary: reads published DTOs from the DB and encodes each to bytes for the wire
 * response. This is the only place a feed's {@code Dto} type meets the generic
 * {@code FeedSourceService.Fetch} endpoint.
 *
 * @param <Rec> the jOOQ record type backing the feed.
 * @param <Dto> the wire message type the feed is exposed as.
 */
public class DbExposedFeed<Rec extends UpdatableRecord<Rec>, Dto extends Message<?, ?>> implements ExposedFeed {
    @Inject
    DbFeedFetcher<Rec, Dto> dbFeedFetcher;

    @Override
    public FeedFetchResponse fetch(FeedFetchRequest request) {
        long after = request.after != null ? Long.parseLong(request.after) : 0L;
        int limit = request.limit != null ? request.limit : FeedFetchRequest.DEFAULT_LIMIT;
        int shard = request.shard != null ? request.shard : 0;
        int numShards = request.num_shards != null ? request.num_shards : FeedFetchRequest.DEFAULT_NUM_SHARDS;

        FetchResult<Dto> result = dbFeedFetcher.fetchPublished(
                new FeedCursor(request.feed_name, after, shard, numShards), limit);

        List<ByteString> entries = result.entries().stream()
                .map(Message::encodeByteString)
                .collect(Collectors.toList());

        return new FeedFetchResponse.Builder()
                .next_after(result.nextAfter())
                .entries(entries)
                .build();
    }
}
