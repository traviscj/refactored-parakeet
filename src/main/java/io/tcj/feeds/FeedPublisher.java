package io.tcj.feeds;

import io.tcj.feeds.api.DbFeedFetcher;
import io.tcj.feeds.api.FeedEntry;
import io.tcj.feeds.api.SequenceStore;
import io.tcj.feeds.model.PublishedFeedDef;
import io.tcj.feeds.model.Sequence;
import io.tcj.jooq.tables.records.KvRecord;
import io.tcj.protos.kv.Kv;
import misk.web.Post;
import misk.web.actions.WebAction;
import org.jooq.UpdatableRecord;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.stream.Collectors.toList;

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

public class FeedPublisher<Rec extends UpdatableRecord<Rec>>  {
  @Inject SequenceStore sequenceStore;
  @Inject PublishedFeedDef publishedFeedDef;
  @Inject DbFeedFetcher<Rec, Kv> dbFeedFetcher;

  public void processBatch() {
    Sequence sequence = sequenceStore.lookup(publishedFeedDef.name());
    List<? extends FeedEntry<Rec>> feedEntries = dbFeedFetcher.fetchUnpublished(
            publishedFeedDef.maxBatchSize());

    AtomicLong atomicLong = new AtomicLong(sequence.value());
    List<FeedEntry<Rec>> collect = feedEntries.stream()
            .limit(publishedFeedDef.maxBatchSize()) // technically redundant...
            .map(entry -> entry.setFeedSyncId(atomicLong.getAndIncrement()))
            .collect(toList());
    int numRecords = dbFeedFetcher.publish(collect, publishedFeedDef.maxBatchSize());
    sequenceStore.put(publishedFeedDef.name(), sequence.value() + numRecords);
  }


  public static class FeedPublishWebAction implements WebAction {
    @Inject FeedPublisher<KvRecord> publisher;

    @Post(pathPattern = "/_api/feeds/publish")
    public String run() {
      publisher.processBatch();
      return "OK";
    }
  }
}
