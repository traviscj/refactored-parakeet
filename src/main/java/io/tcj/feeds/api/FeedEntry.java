package io.tcj.feeds.api;

import org.jooq.UpdatableRecord;

/**
 * Envelope around a single feed-published database record.
 *
 * <p>Wraps a jOOQ record so the generic feed engine can read and assign its
 * {@code feed_sync_id} without knowing the concrete table type. The publisher
 * stamps each entry with the next sync id before the row is written back.
 *
 * @param <Rec> the jOOQ record type backing this feed (e.g. {@code KvRecord}).
 */
public interface FeedEntry<Rec extends UpdatableRecord<Rec>> {
  /** @return the underlying jOOQ record. */
  Rec record();

  /** @return the sync id currently on the record -- its position in the feed. */
  long feedSyncId();

  /**
   * Stamps the next sync id onto the wrapped record (mutates it).
   *
   * @param feedSyncId the monotonically increasing position to assign.
   * @return {@code this}, for fluent use in a stream.
   */
  FeedEntry<Rec> setFeedSyncId(long feedSyncId);
}
