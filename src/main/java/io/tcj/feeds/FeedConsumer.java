package io.tcj.feeds;

import io.tcj.feeds.FeedEntryWebAction.FeedFetchRequest;
import io.tcj.feeds.FeedEntryWebAction.FeedFetchResponse;
import io.tcj.feeds.api.FeedCursorStore;
import io.tcj.feeds.api.FeedEntry;
import io.tcj.feeds.api.FeedListener;
import io.tcj.feeds.model.FeedConsumerDef;
import io.tcj.feeds.model.FeedCursor;

import javax.inject.Inject;

/**
 * calls a (perhaps remote) {@link FeedEntryWebAction} to retreive the next set of records.
 *
 * <p>We accomplish this by:
 * <ul>
 *   <li>calling out to our {@link FeedCursorStore} to obtain the current cursor.</li>
 *   <li>calling the remote feed API to get some entries.</li>
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
 */
public class FeedConsumer<Rec extends FeedEntry<Rec>> {
  @Inject FeedCursorStore feedCursorStore;
  @Inject
  FeedConsumerDef feedConsumerDef;
  @Inject FeedListener<Rec> feedListener;
  @Inject FeedEntryFetcher<Rec> feedEntryFetcher;

  public void processBatch() {
    FeedCursor feedCursor = feedCursorStore.get(feedConsumerDef.feedName(),
        feedConsumerDef.shard());

    FeedFetchRequest req = new FeedFetchRequest(feedConsumerDef.feedName(),
        Long.toString(feedCursor.feedSyncId()), feedConsumerDef.limit(), feedConsumerDef.shard());

    FeedFetchResponse<Rec> fetchResponse = feedEntryFetcher.fetch(req);

    feedListener.process(fetchResponse.entries());

    feedCursorStore.put(feedConsumerDef.feedName(), feedConsumerDef.shard(), fetchResponse.next_after());
  }
}
