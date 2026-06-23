package io.tcj.feeds;

import com.squareup.wire.Message;
import io.tcj.feeds.api.FeedCursorStore;
import io.tcj.feeds.api.FeedListener;
import io.tcj.feeds.model.FeedConsumerDef;
import io.tcj.feeds.model.FeedCursor;
import io.tcj.feeds.model.FetchRequest;
import io.tcj.feeds.model.FetchResult;
import org.jooq.UpdatableRecord;

import javax.inject.Inject;

/**
 * Pulls the next batch from a (perhaps remote) feed source and hands it to a local
 * {@link FeedListener}.
 *
 * <p>We accomplish this by:
 * <ul>
 *   <li>calling out to our {@link FeedCursorStore} to obtain the current cursor.</li>
 *   <li>calling the feed source to get some entries (see {@link FeedSourceFetchRpc}).</li>
 *   <li>passing those feed entries to our local {@link FeedListener} for processing.</li>
 *   <li>saving the new cursor back to our {@link FeedCursorStore}.</li>
 * </ul>
 *
 * <p>The {@link #processBatch()} is intended to be called from some scheduling mechanism.
 *
 * TODO: this is still not _really_ complete; would also want/need to:
 * <ul>
 *   <li>wrap {@link FeedCursorStore} calls in a single transaction.</li>
 *   <li>pass that transaction into the FeedListener.</li>
 * </ul>
 *
 * @param <Rec> the jOOQ record type backing the feed.
 * @param <Dto> the wire message type the feed is consumed as.
 */
public class FeedConsumer<Rec extends UpdatableRecord<Rec>, Dto extends Message<?, ?>> {
  @Inject FeedCursorStore feedCursorStore;
  @Inject FeedConsumerDef feedConsumerDef;
  @Inject FeedListener<Dto> feedListener;
  @Inject FeedEntryFetcher<Rec, Dto> feedEntryFetcher;

  public void processBatch() {
    FeedCursor feedCursor = feedCursorStore.get(feedConsumerDef.feedName(),
        feedConsumerDef.shard());

    FetchRequest req = new FetchRequest(feedConsumerDef.feedName(),
        Long.toString(feedCursor.feedSyncId()), feedConsumerDef.limit(), feedConsumerDef.shard(), feedConsumerDef.numShards());

    FetchResult<Dto> result = feedEntryFetcher.fetch(req);

    feedListener.process(result.entries());

    feedCursorStore.put(feedConsumerDef.feedName(), feedConsumerDef.shard(), result.nextAfter());
  }
}
