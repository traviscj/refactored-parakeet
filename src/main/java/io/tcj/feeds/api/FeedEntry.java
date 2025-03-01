package io.tcj.feeds.api;

import org.jooq.UpdatableRecord;

/**
 * envelope for a feed-published record.
 *
 * Rec: the contents of the envelope -- the record your app feed publishes.
 */
public interface FeedEntry<Rec extends UpdatableRecord<Rec>> {
  UpdatableRecord<Rec> record();

  long feedSyncId();

  FeedEntry<Rec> setFeedSyncId(long feedSyncId);
}
