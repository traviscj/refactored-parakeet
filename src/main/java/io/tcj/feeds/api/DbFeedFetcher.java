package io.tcj.feeds.api;

import com.squareup.wire.Message;
import io.tcj.feeds.model.FeedCursor;
import io.tcj.feeds.model.FetchResult;
import org.jooq.UpdatableRecord;

import java.util.List;

/**
 * All database interactions for one feed-published table, parameterized over the
 * jOOQ record type {@code Rec} and the wire DTO type {@code Dto}.
 *
 * <p>This is the seam between the table-agnostic feed engine and a concrete feed:
 * a single implementation ({@code GenericDbFeedFetcher}) drives any table given a
 * {@code FeedTableDef}. The operations cover both feed roles --
 * {@link #fetchUnpublished} / {@link #publish} for the <em>publisher</em>, and
 * {@link #fetchPublished} for the <em>source</em> that consumers pull from.
 *
 * @param <Rec> the jOOQ record type backing this feed.
 * @param <Dto> the wire message type emitted to consumers.
 */
public interface DbFeedFetcher<Rec extends UpdatableRecord<Rec>, Dto extends Message<?, ?>> {
    /** @return the concrete jOOQ record class this feed reads and writes. */
    Class<Rec> recordType();

    /**
     * Writes each entry's assigned {@code feed_sync_id} back onto its row.
     *
     * @param feedEntries entries already stamped with a sync id by the publisher.
     * @param limit       maximum number of entries to write.
     * @return the number of rows published.
     */
    int publish(List<? extends FeedEntry<Rec>> feedEntries, int limit);

    /**
     * Fetches rows not yet assigned to the feed ({@code feed_sync_id IS NULL}).
     *
     * @param limit maximum number of rows to return.
     * @return unpublished rows wrapped as {@link FeedEntry} envelopes.
     */
    List<? extends FeedEntry<Rec>> fetchUnpublished(int limit);

    /**
     * Fetches published entries strictly after the consumer's cursor, honoring the
     * cursor's shard selection when set.
     *
     * @param feedCursor the consumer's current position (and optional shard filter).
     * @param limit      maximum number of entries to return.
     * @return the next batch of DTOs plus the new cursor ({@code next_after}).
     */
    FetchResult<Dto> fetchPublished(FeedCursor feedCursor, int limit);
}
