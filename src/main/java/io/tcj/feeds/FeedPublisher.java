package io.tcj.feeds;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * finds a selection of unpublished records and assigns sequential feed_sync_id to them.
 *
 * <p>We accomplish this by:
 * <ul>
 *   <li>calling out to our {@link SequenceStore} to obtain the current sequence values</li>
 *   <li>calling our database to fetch up to {@literal MAX_BATCH} records with {@literal feed_sync_id IS NULL}.</li>
 *   <li>assigning monotonically increasing (strictly) feed_sync_id to each record in the batch</li>
 *   <li>Saving our new {@link Sequence} value.</li>
 * </ul>
 */
public class FeedPublisher {
  @Inject SequenceStore sequenceStore;
  @Inject PublishedFeedDef publishedFeedDef;
  @Inject DbFeedFetcher dbFeedFetcher;

  public void processBatch() {
    Sequence sequence = sequenceStore.lookup(publishedFeedDef.name());
    List<FeedEntry<?>> feedEntries = dbFeedFetcher.fetchUnpublished(sequence.value(),
        publishedFeedDef.maxBatchSize());

    AtomicLong atomicLong = new AtomicLong(sequence.value());
    feedEntries.stream().map(entry -> {
      // TODO: assign feedSyncId & increment atomicLong!
        return null;
    });
    int numRecords = dbFeedFetcher.publish(publishedFeedDef.maxBatchSize());
    sequenceStore.put(publishedFeedDef.name(), sequence.value() + numRecords);
  }

}
